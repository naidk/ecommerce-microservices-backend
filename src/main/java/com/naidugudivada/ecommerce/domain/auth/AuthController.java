package com.naidugudivada.ecommerce.domain.auth;

import com.naidugudivada.ecommerce.domain.auth.dto.AuthResponseDTO;
import com.naidugudivada.ecommerce.domain.auth.dto.LoginRequestDTO;
import com.naidugudivada.ecommerce.domain.customer.dto.CustomerRequestDTO;
import com.naidugudivada.ecommerce.domain.customer.dto.CustomerResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        String token = authService.login(loginRequest.email(), loginRequest.password());
        return ResponseEntity.ok(new AuthResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerResponseDTO> register(@Valid @RequestBody CustomerRequestDTO customerRequest) {
        return ResponseEntity.ok(authService.register(customerRequest));
    }
}
