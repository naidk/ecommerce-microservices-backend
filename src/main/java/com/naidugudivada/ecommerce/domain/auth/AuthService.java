package com.naidugudivada.ecommerce.domain.auth;

import com.naidugudivada.ecommerce.domain.customer.event.CustomerEventProducer;
import com.naidugudivada.ecommerce.domain.customer.event.CustomerRegisteredEvent;
import com.naidugudivada.ecommerce.domain.customer.CustomerEntity;
import com.naidugudivada.ecommerce.domain.customer.CustomerMapper;
import com.naidugudivada.ecommerce.domain.customer.CustomerRepository;
import com.naidugudivada.ecommerce.domain.customer.dto.CustomerRequestDTO;
import com.naidugudivada.ecommerce.domain.customer.dto.CustomerResponseDTO;
import com.naidugudivada.ecommerce.infrastructure.exceptions.DuplicateEmailException;
import com.naidugudivada.ecommerce.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.EMAIL_ALREADY_EXISTS;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerMapper customerMapper;

    private final CustomerEventProducer customerEventProducer;

    public String login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return tokenProvider.generateToken(authentication);
    }

    @Transactional
    public CustomerResponseDTO register(CustomerRequestDTO customerDTO) {
        log.info("Starting registration process for user with email: {} [DIAGNOSTIC_V3]", customerDTO.email());
        if (customerRepository.existsByEmailIgnoreCase(customerDTO.email())) {
            throw new DuplicateEmailException(EMAIL_ALREADY_EXISTS);
        }

        CustomerEntity customer = customerMapper.toEntity(customerDTO);
        customer.setPassword(passwordEncoder.encode(customerDTO.password()));
        customer.setRole("ROLE_USER");

        if (customer.getAddress() != null) {
            customer.getAddress().forEach(address -> address.setCustomer(customer));
        } else {
            log.warn("Customer address list is null for {}", customerDTO.email());
        }

        log.info("Saving customer entity...");
        CustomerEntity savedCustomer = customerRepository.save(customer);
        log.info("Customer saved with ID: {}", savedCustomer.getId());

        if (customerEventProducer != null) {
            customerEventProducer.publishCustomerRegistered(
                    CustomerRegisteredEvent.builder()
                            .customerId(savedCustomer.getId())
                            .name(savedCustomer.getName())
                            .email(savedCustomer.getEmail())
                            .source("AuthService")
                            .eventType("CustomerRegistered")
                            .build());
        }

        return customerMapper.toResponseDTO(savedCustomer);
    }
}
