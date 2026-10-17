package nl.vlasje24.service;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.domain.AdminCommentFilter;
import nl.vlasje24.domain.Comment;
import nl.vlasje24.domain.CommentStatus;
import nl.vlasje24.domain.ModerationAction;
import nl.vlasje24.domain.ModerationAudit;
import nl.vlasje24.domain.User;
import nl.vlasje24.domain.UserRole;
import nl.vlasje24.domain.UserStatus;
import nl.vlasje24.dto.AdminCommentDto;
import nl.vlasje24.dto.AdminCommentPageDto;
import nl.vlasje24.dto.AdminUserDto;
import nl.vlasje24.dto.AdminUserPageDto;
import nl.vlasje24.dto.ModerationAuditDto;
import nl.vlasje24.dto.ModerationAuditPageDto;
import nl.vlasje24.exception.CommentAccessDeniedException;
import nl.vlasje24.exception.ModerationConflictException;
import nl.vlasje24.exception.NotFoundException;
import nl.vlasje24.repository.CommentRepository;
import nl.vlasje24.repository.ModerationAuditRepository;
import nl.vlasje24.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminModerationService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ModerationAuditRepository auditRepository;
    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Transactional(readOnly = true)
    public AdminCommentPageDto getComments(AdminCommentFilter filter, int page, int size) {
        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("commentId")));
        Page<Comment> comments = switch (filter) {
            case ALL -> commentRepository.findAll(pageable);
            case VISIBLE -> commentRepository.findByStatusIn(
                    List.of(CommentStatus.VISIBLE), pageable);
            case DELETED -> commentRepository.findByStatusIn(
                    List.of(CommentStatus.DELETED_BY_AUTHOR, CommentStatus.DELETED_BY_MODERATOR),
                    pageable);
        };
        return AdminCommentPageDto.from(comments.map(AdminCommentDto::from));
    }

    @Transactional(readOnly = true)
    public AdminUserPageDto getUsers(String query, int page, int size) {
        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("userId")));
        String normalizedQuery = query == null ? "" : query.trim();
        Page<User> users = normalizedQuery.isEmpty()
                ? userRepository.findAll(pageable)
                : userRepository.findByUsernameContainingIgnoreCase(normalizedQuery, pageable);
        return AdminUserPageDto.from(users.map(AdminUserDto::from));
    }

    @Transactional(readOnly = true)
    public ModerationAuditPageDto getAudit(int page, int size) {
        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("moderationAuditId")));
        return ModerationAuditPageDto.from(
                auditRepository.findAll(pageable).map(ModerationAuditDto::from));
    }

    @Transactional
    public void deleteComment(Long commentId, Integer adminUserId, String reason) {
        User admin = requireActiveAdmin(adminUserId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Reactie niet gevonden"));
        if (!comment.isVisible()) {
            throw new ModerationConflictException("Deze reactie is al verwijderd");
        }

        comment.softDelete(true);
        auditRepository.save(new ModerationAudit(
                admin,
                ModerationAction.COMMENT_DELETED,
                comment,
                comment.getAuthor(),
                reason));
    }

    @Transactional
    public AdminUserDto updateSuspension(
            Integer targetUserId,
            Integer adminUserId,
            boolean suspended,
            String reason) {
        User admin = requireActiveAdmin(adminUserId);
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("Account niet gevonden"));

        if (target.getUserId().equals(admin.getUserId())) {
            throw new ModerationConflictException("Je kunt je eigen account niet blokkeren");
        }
        if (target.getRole() == UserRole.ADMIN) {
            throw new ModerationConflictException("Adminaccounts kunnen hier niet worden geblokkeerd");
        }
        if (target.getStatus() == UserStatus.DELETED) {
            throw new ModerationConflictException("Een verwijderd account kan niet worden gewijzigd");
        }

        if (suspended && target.getStatus() != UserStatus.SUSPENDED) {
            target.suspend();
            deleteSessions(target.getUsername());
            saveUserAudit(admin, target, ModerationAction.USER_SUSPENDED, reason);
        } else if (!suspended && target.getStatus() == UserStatus.SUSPENDED) {
            target.reactivate();
            saveUserAudit(admin, target, ModerationAction.USER_REACTIVATED, reason);
        }

        return AdminUserDto.from(target);
    }

    private User requireActiveAdmin(Integer userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .filter(user -> user.getRole() == UserRole.ADMIN)
                .orElseThrow(() -> new CommentAccessDeniedException("Geen actieve beheerder"));
    }

    private void saveUserAudit(
            User admin,
            User target,
            ModerationAction action,
            String reason) {
        auditRepository.save(new ModerationAudit(admin, action, null, target, reason));
    }

    private void deleteSessions(String username) {
        sessionRepository.findByPrincipalName(username)
                .keySet()
                .forEach(sessionRepository::deleteById);
    }
}
