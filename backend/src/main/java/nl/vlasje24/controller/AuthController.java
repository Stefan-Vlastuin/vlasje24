package nl.vlasje24.controller;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.domain.User;
import nl.vlasje24.dto.LoginRequestDto;
import nl.vlasje24.dto.LoginResponseDto;
import nl.vlasje24.repository.UserRepository;
import nl.vlasje24.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto dto) {
        User user = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ongeldige inloggegevens"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ongeldige inloggegevens");
        }

        return new LoginResponseDto(jwtUtil.generateToken(user.getUsername()));
    }
}
