package com.naidugudivada.ecommerce.domain.promotion;

import com.naidugudivada.ecommerce.domain.promotion.dto.PromotionRequestDTO;
import com.naidugudivada.ecommerce.domain.promotion.dto.PromotionResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/promotion")
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public ResponseEntity<PromotionResponseDTO> createPromotion(@Valid @RequestBody PromotionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.createPromotion(request));
    }
}
