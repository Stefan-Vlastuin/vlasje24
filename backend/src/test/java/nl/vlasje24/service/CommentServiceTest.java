package nl.vlasje24.service;

import nl.vlasje24.domain.ChartWeek;
import nl.vlasje24.domain.Comment;
import nl.vlasje24.domain.CommentStatus;
import nl.vlasje24.domain.User;
import nl.vlasje24.domain.UserRole;
import nl.vlasje24.dto.CommentPageDto;
import nl.vlasje24.exception.CommentAccessDeniedException;
import nl.vlasje24.exception.EmailNotVerifiedException;
import nl.vlasje24.exception.NotFoundException;
import nl.vlasje24.repository.ChartWeekRepository;
import nl.vlasje24.repository.CommentRepository;
import nl.vlasje24.repository.ModerationAuditRepository;
import nl.vlasje24.repository.UserRepository;
import nl.vlasje24.security.CommentRateLimiter;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommentServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock ChartWeekRepository chartWeekRepository;
    @Mock UserRepository userRepository;
    @Mock ModerationAuditRepository moderationAuditRepository;
    @Mock CommentRateLimiter rateLimiter;

    @InjectMocks CommentService commentService;

    @Test
    void getCommentsReturnsVisibleCommentsNewestFirst() {
        Comment comment = comment(1L, user(10, UserRole.USER, true));
        when(chartWeekRepository.existsByWeekId(7)).thenReturn(true);
        when(commentRepository.findByChartWeekWeekIdAndStatus(
                org.mockito.ArgumentMatchers.eq(7),
                org.mockito.ArgumentMatchers.eq(CommentStatus.VISIBLE),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(comment)));

        CommentPageDto result = commentService.getComments(7, 0, 20);

        assertThat(result.comments()).hasSize(1);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(commentRepository).findByChartWeekWeekIdAndStatus(
                org.mockito.ArgumentMatchers.eq(7),
                org.mockito.ArgumentMatchers.eq(CommentStatus.VISIBLE), pageable.capture());
        assertThat(pageable.getValue().getSort().getOrderFor("createdAt").isDescending()).isTrue();
        assertThat(pageable.getValue().getSort().getOrderFor("commentId").isDescending()).isTrue();
    }

    @Test
    void getCommentsForUnknownWeekThrowsNotFound() {
        when(chartWeekRepository.existsByWeekId(99)).thenReturn(false);

        assertThatThrownBy(() -> commentService.getComments(99, 0, 20))
                .isInstanceOf(NotFoundException.class);
        verify(commentRepository, never()).findByChartWeekWeekIdAndStatus(any(), any(), any());
    }

    @Test
    void createCommentRequiresVerifiedAccount() {
        User unverifiedUser = user(10, UserRole.USER, false);
        when(userRepository.findById(10)).thenReturn(Optional.of(unverifiedUser));

        assertThatThrownBy(() -> commentService.createComment(7, 10, "Hallo"))
                .isInstanceOf(EmailNotVerifiedException.class);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void suspendedAccountCannotCreateComment() {
        User suspendedUser = user(10, UserRole.USER, true);
        when(suspendedUser.isActive()).thenReturn(false);
        when(userRepository.findById(10)).thenReturn(Optional.of(suspendedUser));

        assertThatThrownBy(() -> commentService.createComment(7, 10, "Hallo"))
                .isInstanceOf(CommentAccessDeniedException.class);
        verify(commentRepository, never()).save(any());
        verifyNoInteractions(rateLimiter);
    }

    @Test
    void createCommentTrimsBodyAndChecksRateLimit() {
        User author = user(10, UserRole.USER, true);
        ChartWeek week = mock(ChartWeek.class);
        Comment persisted = comment(1L, author);
        when(userRepository.findById(10)).thenReturn(Optional.of(author));
        when(chartWeekRepository.findById(7)).thenReturn(Optional.of(week));
        when(commentRepository.save(any(Comment.class))).thenReturn(persisted);

        commentService.createComment(7, 10, "  Hallo  ");

        ArgumentCaptor<Comment> comment = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(comment.capture());
        assertThat(comment.getValue().getBody()).isEqualTo("Hallo");
        verify(rateLimiter).check(10);
    }

    @Test
    void regularUserCannotEditSomeoneElsesComment() {
        User actor = user(10, UserRole.USER, true);
        Comment comment = comment(1L, user(11, UserRole.USER, true));
        when(userRepository.findById(10)).thenReturn(Optional.of(actor));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.editComment(1L, 10, "Gewijzigd"))
                .isInstanceOf(CommentAccessDeniedException.class);
        verify(comment, never()).edit(any());
    }

    @Test
    void ownerCanEditAndBodyIsTrimmed() {
        User owner = user(10, UserRole.USER, true);
        Comment comment = comment(1L, owner);
        when(userRepository.findById(10)).thenReturn(Optional.of(owner));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.editComment(1L, 10, "  Gewijzigd  ");

        verify(comment).edit("Gewijzigd");
    }

    @Test
    void adminCanSoftDeleteSomeoneElsesCommentAsModerator() {
        User admin = user(10, UserRole.ADMIN, true);
        Comment comment = comment(1L, user(11, UserRole.USER, true));
        when(userRepository.findById(10)).thenReturn(Optional.of(admin));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L, 10);

        verify(comment).softDelete(true);
        verify(moderationAuditRepository).save(any());
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void ownerCanSoftDeleteOwnCommentWithoutModerationAudit() {
        User owner = user(10, UserRole.USER, true);
        Comment comment = comment(1L, owner);
        when(userRepository.findById(10)).thenReturn(Optional.of(owner));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L, 10);

        verify(comment).softDelete(false);
        verifyNoInteractions(moderationAuditRepository);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void regularUserCannotDeleteSomeoneElsesComment() {
        User actor = user(10, UserRole.USER, true);
        Comment comment = comment(1L, user(11, UserRole.USER, true));
        when(userRepository.findById(10)).thenReturn(Optional.of(actor));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(1L, 10))
                .isInstanceOf(CommentAccessDeniedException.class);

        verify(comment, never()).softDelete(anyBoolean());
        verifyNoInteractions(moderationAuditRepository);
    }

    private User user(int id, UserRole role, boolean verified) {
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(id);
        when(user.getRole()).thenReturn(role);
        when(user.isActive()).thenReturn(true);
        when(user.isEmailVerified()).thenReturn(verified);
        when(user.getUsername()).thenReturn("user-" + id);
        return user;
    }

    private Comment comment(long id, User author) {
        Comment comment = mock(Comment.class);
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 17, 10, 0);
        when(comment.getCommentId()).thenReturn(id);
        when(comment.getAuthor()).thenReturn(author);
        when(comment.getBody()).thenReturn("Een reactie");
        when(comment.getCreatedAt()).thenReturn(createdAt);
        when(comment.getUpdatedAt()).thenReturn(createdAt);
        when(comment.isVisible()).thenReturn(true);
        return comment;
    }
}
