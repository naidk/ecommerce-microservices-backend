package com.naidugudivada.ecommerce.domain.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naidugudivada.ecommerce.domain.product.ProductEntity;
import com.naidugudivada.ecommerce.domain.product.ProductRepository;
// import com.naidugudivada.ecommerce.domain.product.event.ProductCreatedEvent;
// import com.naidugudivada.ecommerce.domain.product.event.StockUpdatedEvent;
// import com.naidugudivada.ecommerce.infrastructure.constants.KafkaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.stereotype.Service;

import java.util.Optional;

// @Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchSyncConsumer {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final ObjectMapper objectMapper;

    // @KafkaListener(topics = KafkaConstants.PRODUCT_TOPIC, groupId =
    // KafkaConstants.GROUP_ID)
    public void consumeProductEvent(String message) {
        log.info("Received message on product-events topic: {}", message);
        try {
            // Determine event type by parsing the JSON tree or we can just fetch the
            // product directly if an ID is present
            // Since our events contain the productId, a robust approach is to extract the
            // ID and sync the latest DB state.

            // For simplicity in consuming a generic string message that could be
            // ProductCreated or StockUpdated:
            var rootNode = objectMapper.readTree(message);
            if (rootNode.has("productId")) {
                String productIdStr = rootNode.get("productId").asText();
                syncProductToElasticsearch(java.util.UUID.fromString(productIdStr));
            }
        } catch (Exception e) {
            log.error("Failed to process product event for Elasticsearch sync: {}", e.getMessage());
        }
    }

    private void syncProductToElasticsearch(java.util.UUID productId) {
        Optional<ProductEntity> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            ProductEntity entity = productOpt.get();
            ProductDocument doc = ProductDocument.builder()
                    .id(entity.getId())
                    .sku(entity.getSku())
                    .name(entity.getName())
                    .label(entity.getLabel())
                    .category(entity.getCategory())
                    .price(entity.getPrice())
                    .active(entity.getActive())
                    .averageRating(entity.getAverageRating())
                    .vendorId(entity.getVendor() != null ? entity.getVendor().getId() : null)
                    .build();

            productSearchRepository.save(doc);
            log.info("Successfully synced product {} to Elasticsearch.", productId);
        } else {
            // If the product was deleted, remove it from Elasticsearch
            productSearchRepository.deleteById(productId);
            log.info("Successfully removed product {} from Elasticsearch.", productId);
        }
    }
}
