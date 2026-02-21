package com.naidugudivada.ecommerce.domain.product.review;

import com.naidugudivada.ecommerce.domain.product.review.dto.ReviewRequestDTO;
import com.naidugudivada.ecommerce.domain.product.review.dto.ReviewResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.naidugudivada.ecommerce.domain.customer.CustomerRepository;
import com.naidugudivada.ecommerce.domain.customer.exceptions.CustomerNotFoundException;

import java.util.UUID;

import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.CUSTOMER_NOT_FOUND_WITH_ID;

@RestController
@RequestMapping("/api/product/{productId}/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final CustomerRepository customerRepository;

    @GetMapping
    public ResponseEntity<Page<ReviewResponseDTO>> getReviews(@PathVariable UUID productId, Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviewsByProductId(productId, pageable));
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(@PathVariable UUID productId,
            @Valid @RequestBody ReviewRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Find customer ID based on UserDetails email
        UUID customerId = customerRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new CustomerNotFoundException(
                        String.format(CUSTOMER_NOT_FOUND_WITH_ID, userDetails.getUsername())))
                .getId();

        ReviewResponseDTO createdReview = reviewService.createReview(customerId,
                new ReviewRequestDTO(productId, requestDTO.rating(), requestDTO.title(), requestDTO.comment()));

        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdReview.id())
                .toUri();

        return ResponseEntity.created(location).body(createdReview);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID productId,
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID customerId = customerRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new CustomerNotFoundException(
                        String.format(CUSTOMER_NOT_FOUND_WITH_ID, userDetails.getUsername())))
                .getId();

        reviewService.deleteReview(reviewId, customerId);
        return ResponseEntity.noContent().build();
    }
}
