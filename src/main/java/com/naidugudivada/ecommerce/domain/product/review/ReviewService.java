package com.naidugudivada.ecommerce.domain.product.review;

import com.naidugudivada.ecommerce.domain.customer.CustomerEntity;
import com.naidugudivada.ecommerce.domain.customer.CustomerRepository;
import com.naidugudivada.ecommerce.domain.customer.exceptions.CustomerNotFoundException;
import com.naidugudivada.ecommerce.domain.order.OrderRepository;
import com.naidugudivada.ecommerce.domain.product.ProductEntity;
import com.naidugudivada.ecommerce.domain.product.ProductRepository;
import com.naidugudivada.ecommerce.domain.product.exceptions.ProductNotFoundException;
import com.naidugudivada.ecommerce.domain.product.review.dto.ReviewRequestDTO;
import com.naidugudivada.ecommerce.domain.product.review.dto.ReviewResponseDTO;
import com.naidugudivada.ecommerce.domain.product.review.exceptions.DuplicateReviewException;
import com.naidugudivada.ecommerce.domain.product.review.exceptions.ReviewNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.CUSTOMER_NOT_FOUND_WITH_ID;
import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.PRODUCT_NOT_FOUND_WITH_ID;
import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.REVIEW_ALREADY_EXISTS;
import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.REVIEW_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewResponseDTO createReview(UUID customerId, ReviewRequestDTO requestDTO) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(
                        () -> new CustomerNotFoundException(String.format(CUSTOMER_NOT_FOUND_WITH_ID, customerId)));

        ProductEntity product = productRepository.findById(requestDTO.productId())
                .orElseThrow(() -> new ProductNotFoundException(
                        String.format(PRODUCT_NOT_FOUND_WITH_ID, requestDTO.productId())));

        if (reviewRepository.existsByProductIdAndCustomerId(product.getId(), customer.getId())) {
            throw new DuplicateReviewException(REVIEW_ALREADY_EXISTS);
        }

        long purchaseCount = orderRepository.countPurchasesByCustomerAndProduct(customer.getId(), product.getId());
        if (purchaseCount == 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only review products that you have successfully purchased.");
        }

        ReviewEntity reviewEntity = reviewMapper.toEntity(requestDTO);
        reviewEntity.setProduct(product);
        reviewEntity.setCustomer(customer);

        ReviewEntity savedReview = reviewRepository.save(reviewEntity);

        return reviewMapper.toResponseDTO(savedReview);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviewsByProductId(UUID productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(String.format(PRODUCT_NOT_FOUND_WITH_ID, productId));
        }

        return reviewRepository.findByProductId(productId, pageable)
                .map(reviewMapper::toResponseDTO);
    }

    @Transactional
    public void deleteReview(UUID reviewId, UUID customerId) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(String.format(REVIEW_NOT_FOUND, reviewId)));

        if (!review.getCustomer().getId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own reviews.");
        }

        reviewRepository.delete(review);
    }
}
