package nl.vlasje24.service;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.domain.ChartWeek;
import nl.vlasje24.domain.Comment;
import nl.vlasje24.domain.CommentStatus;
import nl.vlasje24.domain.ModerationAction;
import nl.vlasje24.domain.ModerationAudit;
import nl.vlasje24.domain.User;
import nl.vlasje24.domain.UserRole;
import nl.vlasje24.dto.CommentDto;
import nl.vlasje24.dto.CommentPageDto;
import nl.vlasje24.exception.CommentAccessDeniedException;
import nl.vlasje24.exception.EmailNotVerifiedException;
import nl.vlasje24.exception.NotFoundException;
import nl.vlasje24.repository.ChartWeekRepository;
import nl.vlasje24.repository.CommentRepository;
import nl.vlasje24.repository.ModerationAuditRepository;
import nl.vlasje24.repository.UserRepository;
import nl.vlasje24.security.CommentRateLimiter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ChartWeekRepository chartWeekRepository;
    private final UserRepository userRepository;
    private final ModerationAuditRepository moderationAuditRepository;
    private final CommentRateLimiter rateLimiter;

    @Transactional(readOnly = true)
    public CommentPageDto getComments(Integer weekId, int page, int size) {
        if (!chartWeekRepository.existsByWeekId(weekId)) {
            throw new NotFoundException("Chartweek niet gevonden");
        }
        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("commentId")));
        return CommentPageDto.from(commentRepository
                .findByChartWeekWeekIdAndStatus(weekId, CommentStatus.VISIBLE, pageable)
                .map(CommentDto::from));
    }

    @Transactional
    public CommentDto createComment(Integer weekId, Integer userId, String body) {
        User author = requireWriter(userId);
        ChartWeek chartWeek = chartWeekRepository.findById(weekId)
                .orElseThrow(() -> new NotFoundException("Chartweek niet gevonden"));
        rateLimiter.check(userId);
        Comment comment = commentRepository.save(new Comment(chartWeek, author, body.trim()));
        return CommentDto.from(comment);
    }

    @Transactional
    public CommentDto editComment(Long commentId, Integer userId, String body) {
        User actor = requireWriter(userId);
        Comment comment = requireVisibleComment(commentId);
        requireOwnerOrAdmin(comment, actor);
        comment.edit(body.trim());
        return CommentDto.from(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Integer userId) {
        User actor = requireWriter(userId);
        Comment comment = requireVisibleComment(commentId);
        requireOwnerOrAdmin(comment, actor);
        boolean deletedByModerator = actor.getRole() == UserRole.ADMIN
                && !comment.getAuthor().getUserId().equals(actor.getUserId());
        comment.softDelete(deletedByModerator);
        if (deletedByModerator) {
            moderationAuditRepository.save(new ModerationAudit(
                    actor,
                    ModerationAction.COMMENT_DELETED,
                    comment,
                    comment.getAuthor(),
                    null));
        }
    }

    private User requireWriter(Integer userId) {
        User user = userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new CommentAccessDeniedException("Dit account kan geen reacties beheren"));
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }
        return user;
    }

    private Comment requireVisibleComment(Long commentId) {
        return commentRepository.findById(commentId)
                .filter(Comment::isVisible)
                .orElseThrow(() -> new NotFoundException("Reactie niet gevonden"));
    }

    private void requireOwnerOrAdmin(Comment comment, User actor) {
        boolean owner = comment.getAuthor().getUserId().equals(actor.getUserId());
        if (!owner && actor.getRole() != UserRole.ADMIN) {
            throw new CommentAccessDeniedException("Je mag deze reactie niet beheren");
        }
    }
}
