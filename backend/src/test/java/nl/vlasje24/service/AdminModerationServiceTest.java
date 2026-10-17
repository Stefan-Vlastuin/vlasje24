package nl.vlasje24.service;

import nl.vlasje24.domain.AdminCommentFilter;
import nl.vlasje24.domain.Comment;
import nl.vlasje24.domain.CommentStatus;
import nl.vlasje24.domain.ModerationAction;
import nl.vlasje24.domain.ModerationAudit;
import nl.vlasje24.domain.User;
import nl.vlasje24.domain.UserRole;
import nl.vlasje24.domain.UserStatus;
import nl.vlasje24.exception.ModerationConflictException;
import nl.vlasje24.repository.CommentRepository;
import nl.vlasje24.repository.ModerationAuditRepository;
import nl.vlasje24.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminModerationServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock UserRepository userRepository;
    @Mock ModerationAuditRepository auditRepository;
    @Mock FindByIndexNameSessionRepository<Session> sessionRepository;

    @InjectMocks AdminModerationService service;

    @Test
    void deletedFilterOnlyQueriesDeletedStatuses() {
        when(commentRepository.findByStatusIn(any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        service.getComments(AdminCommentFilter.DELETED, 0, 20);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CommentStatus>> statuses = ArgumentCaptor.forClass(List.class);
        verify(commentRepository).findByStatusIn(statuses.capture(), any(Pageable.class));
        assertThat(statuses.getValue()).containsExactlyInAnyOrder(
                CommentStatus.DELETED_BY_AUTHOR, CommentStatus.DELETED_BY_MODERATOR);
    }

    @Test
    void deleteCommentSoftDeletesAndStoresTrimmedAuditReason() {
        User admin = user(1, "admin", UserRole.ADMIN, UserStatus.ACTIVE);
        User author = user(42, "member", UserRole.USER, UserStatus.ACTIVE);
        Comment comment = comment(9L, author, true);
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(commentRepository.findById(9L)).thenReturn(Optional.of(comment));

        service.deleteComment(9L, 1, "  Spam  ");

        verify(comment).softDelete(true);
        ArgumentCaptor<ModerationAudit> audit = ArgumentCaptor.forClass(ModerationAudit.class);
        verify(auditRepository).save(audit.capture());
        assertThat(audit.getValue().getAction()).isEqualTo(ModerationAction.COMMENT_DELETED);
        assertThat(audit.getValue().getTargetComment()).isEqualTo(comment);
        assertThat(audit.getValue().getTargetUser()).isEqualTo(author);
        assertThat(audit.getValue().getReason()).isEqualTo("Spam");
    }

    @Test
    void alreadyDeletedCommentCannotBeModeratedAgain() {
        User admin = user(1, "admin", UserRole.ADMIN, UserStatus.ACTIVE);
        Comment comment = comment(9L, user(42, "member", UserRole.USER, UserStatus.ACTIVE), false);
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(commentRepository.findById(9L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> service.deleteComment(9L, 1, null))
                .isInstanceOf(ModerationConflictException.class);
        verify(auditRepository, never()).save(any());
    }

    @Test
    void suspendUserEndsAllSessionsAndAuditsAction() {
        User admin = user(1, "admin", UserRole.ADMIN, UserStatus.ACTIVE);
        User target = user(42, "member", UserRole.USER, UserStatus.ACTIVE);
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(userRepository.findById(42)).thenReturn(Optional.of(target));
        when(sessionRepository.findByPrincipalName("member"))
                .thenReturn(Map.of("session-one", mock(Session.class), "session-two", mock(Session.class)));

        service.updateSuspension(42, 1, true, "Misbruik");

        verify(target).suspend();
        verify(sessionRepository).deleteById("session-one");
        verify(sessionRepository).deleteById("session-two");
        ArgumentCaptor<ModerationAudit> audit = ArgumentCaptor.forClass(ModerationAudit.class);
        verify(auditRepository).save(audit.capture());
        assertThat(audit.getValue().getAction()).isEqualTo(ModerationAction.USER_SUSPENDED);
    }

    @Test
    void reactivateUserDoesNotCreateANewSession() {
        User admin = user(1, "admin", UserRole.ADMIN, UserStatus.ACTIVE);
        User target = user(42, "member", UserRole.USER, UserStatus.SUSPENDED);
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(userRepository.findById(42)).thenReturn(Optional.of(target));

        service.updateSuspension(42, 1, false, null);

        verify(target).reactivate();
        verify(sessionRepository, never()).findByPrincipalName(any());
        ArgumentCaptor<ModerationAudit> audit = ArgumentCaptor.forClass(ModerationAudit.class);
        verify(auditRepository).save(audit.capture());
        assertThat(audit.getValue().getAction()).isEqualTo(ModerationAction.USER_REACTIVATED);
    }

    @Test
    void adminCannotSuspendSelfOrAnotherAdmin() {
        User admin = user(1, "admin", UserRole.ADMIN, UserStatus.ACTIVE);
        User otherAdmin = user(2, "other-admin", UserRole.ADMIN, UserStatus.ACTIVE);
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.updateSuspension(1, 1, true, null))
                .isInstanceOf(ModerationConflictException.class);

        when(userRepository.findById(2)).thenReturn(Optional.of(otherAdmin));
        assertThatThrownBy(() -> service.updateSuspension(2, 1, true, null))
                .isInstanceOf(ModerationConflictException.class);
    }

    private User user(int id, String username, UserRole role, UserStatus status) {
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(id);
        when(user.getUsername()).thenReturn(username);
        when(user.getRole()).thenReturn(role);
        when(user.getStatus()).thenReturn(status);
        when(user.isActive()).thenReturn(status == UserStatus.ACTIVE);
        when(user.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 10, 17, 10, 0));
        return user;
    }

    private Comment comment(long id, User author, boolean visible) {
        Comment comment = mock(Comment.class);
        when(comment.getCommentId()).thenReturn(id);
        when(comment.getAuthor()).thenReturn(author);
        when(comment.isVisible()).thenReturn(visible);
        return comment;
    }
}
