package com.naidugudivada.ecommerce.domain.promotion;

import com.naidugudivada.ecommerce.domain.promotion.dto.PromotionRequestDTO;
import com.naidugudivada.ecommerce.domain.promotion.dto.PromotionResponseDTO;
import com.naidugudivada.ecommerce.domain.promotion.exceptions.InvalidPromotionException;
import com.naidugudivada.ecommerce.domain.promotion.exceptions.PromotionNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;

    @Transactional
    public PromotionResponseDTO createPromotion(PromotionRequestDTO request) {
        log.info("Creating new promotion code: [{}]", request.code());

        if (promotionRepository.findByCode(request.code()).isPresent()) {
            throw new InvalidPromotionException("Promotion code already exists: " + request.code());
        }

        if (request.endDate().isBefore(request.startDate())) {
            throw new InvalidPromotionException("End date cannot be before start date.");
        }

        PromotionEntity entity = promotionMapper.toEntity(request);
        entity.setCode(entity.getCode().toUpperCase().trim());

        return promotionMapper.toResponseDTO(promotionRepository.save(entity));
    }

    public PromotionEntity validateAndGetPromotion(String code) {
        String normalizedCode = code.toUpperCase().trim();
        PromotionEntity promotion = promotionRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new PromotionNotFoundException("Promotion not found with code: " + normalizedCode));

        if (!promotion.isValid()) {
            throw new InvalidPromotionException("Promotion code is expired or inactive: " + normalizedCode);
        }

        return promotion;
    }
}
