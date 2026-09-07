package nl.vlasje24.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.vlasje24.dto.AccountDto;
import nl.vlasje24.dto.CsrfTokenDto;
import nl.vlasje24.dto.LoginRequestDto;
import nl.vlasje24.dto.RegisterRequestDto;
import nl.vlasje24.exception.InvalidCredentialsException;
import nl.vlasje24.security.AccountPrincipal;
import nl.vlasje24.security.AuthRateLimiter;
import nl.vlasje24.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final CsrfTokenRepository csrfTokenRepository;
    private final AuthRateLimiter rateLimiter;

    @GetMapping("/csrf")
    public CsrfTokenDto csrf(CsrfToken csrfToken) {
        return new CsrfTokenDto(csrfToken.getToken());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDto register(
            @Valid @RequestBody RegisterRequestDto request,
            HttpServletRequest servletRequest) {
        rateLimiter.checkRegistration(servletRequest.getRemoteAddr());
        return accountService.register(request);
    }

    @PostMapping("/login")
    public AccountDto login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        rateLimiter.checkLogin(servletRequest.getRemoteAddr());

        Authentication authenticated;
        try {
            authenticated = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.username().trim(), request.password()));
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }

        HttpSession oldSession = servletRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        AccountPrincipal principal = ((AccountPrincipal) authenticated.getPrincipal()).withoutPassword();
        Authentication sessionAuthentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(sessionAuthentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, servletRequest, servletResponse);

        return new AccountDto(
                principal.userId(), principal.username(), principal.role(), principal.emailVerified());
    }

    @GetMapping("/me")
    public AccountDto me(Authentication authentication) {
        AccountPrincipal principal = (AccountPrincipal) authentication.getPrincipal();
        return new AccountDto(
                principal.userId(), principal.username(), principal.role(), principal.emailVerified());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        csrfTokenRepository.saveToken(null, request, response);
    }
}
