package com.naidugudivada.ecommerce.domain.product.review;

import com.naidugudivada.ecommerce.domain.product.review.dto.ReviewRequestDTO;
import com.naidugudivada.ecommerce.domain.product.review.dto.ReviewResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewEntity toEntity(ReviewRequestDTO requestDTO) {
        return ReviewEntity.builder()
                .rating(requestDTO.rating())
                .title(requestDTO.title())
                .comment(requestDTO.comment())
                .build();
    }

    public ReviewResponseDTO toResponseDTO(ReviewEntity entity) {
        return new ReviewResponseDTO(
                entity.getId(),
                entity.getProduct().getId(),
                entity.getCustomer().getId(),
                entity.getCustomer().getName(),
                entity.getRating(),
                entity.getTitle(),
                entity.getComment(),
                entity.getCreatedAt());
    }
}
