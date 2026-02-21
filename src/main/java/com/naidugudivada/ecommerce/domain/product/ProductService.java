package com.naidugudivada.ecommerce.domain.product;

import com.naidugudivada.ecommerce.domain.product.exceptions.DuplicateProductException;
import com.naidugudivada.ecommerce.domain.product.exceptions.InvalidCategoryException;
import com.naidugudivada.ecommerce.domain.product.exceptions.ProductNotFoundException;
import com.naidugudivada.ecommerce.domain.product.event.ProductCreatedEvent;
import com.naidugudivada.ecommerce.domain.product.event.ProductEventProducer;
import com.naidugudivada.ecommerce.domain.product.event.StockUpdatedEvent;
import com.naidugudivada.ecommerce.domain.product.dto.ProductRequestDTO;
import com.naidugudivada.ecommerce.domain.product.dto.ProductResponseDTO;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.INVALID_CATEGORY;
import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.PRODUCT_NOT_FOUND_WITH_ID;
import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.SKU_ALREADY_EXISTS;

import com.naidugudivada.ecommerce.service.AwsS3Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductEventProducer productEventProducer;
    private final AwsS3Service awsS3Service;

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponseDTO uploadImage(UUID id, MultipartFile file) throws IOException {
        ProductEntity product = getEntity(id);
        String imageUrl = awsS3Service.uploadFile(file, "products/" + id.toString());
        product.setImageUrl(imageUrl);
        return productMapper.toResponseDTO(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        return productRepository.findAllByActiveTrue(pageable).map(productMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponseDTO findById(UUID id) {
        return productMapper.toResponseDTO(getEntity(id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'category-' + #category + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductResponseDTO> findAllByCategory(String category, Pageable pageable) {
        return productRepository.findAllByCategoryIgnoreCaseAndActiveTrue(validateCategory(category), pageable)
                .map(productMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'label-' + #label + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductResponseDTO> findAllByLabel(String label, Pageable pageable) {
        return productRepository.findAllByLabelIgnoreCaseAndActiveTrue(label, pageable)
                .map(productMapper::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDTO create(ProductRequestDTO product) {

        if (verifyIfThisSkuAlreadyExists(product.sku())) {
            throw new DuplicateProductException(SKU_ALREADY_EXISTS);
        }

        validateCategory(product.category());

        ProductEntity savedProduct = productRepository.save(productMapper.toEntity(product));

        productEventProducer.publishProductCreated(
                ProductCreatedEvent.builder()
                        .productId(savedProduct.getId())
                        .name(savedProduct.getName())
                        .sku(savedProduct.getSku())
                        .price(savedProduct.getPrice())
                        .stockQuantity(savedProduct.getStockQuantity())
                        .source("ProductService")
                        .eventType("ProductCreated")
                        .build());

        return productMapper.toResponseDTO(savedProduct);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponseDTO updateById(UUID id, ProductRequestDTO product) {

        var entity = getEntity(id);

        if (ObjectUtils.notEqual(entity.getSku(), product.sku()) && verifyIfThisSkuAlreadyExists(product.sku())) {
            throw new DuplicateProductException(SKU_ALREADY_EXISTS);
        }

        validateCategory(product.category());
        productMapper.updateEntityFromDto(product, entity);

        return productMapper.toResponseDTO(productRepository.save(entity));
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteById(UUID id) {
        var product = getEntity(id);

        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponseDTO addStockById(UUID id, Integer quantity) {
        var product = getEntityWithLock(id);
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);

        productEventProducer.publishStockUpdated(
                StockUpdatedEvent.builder()
                        .productId(product.getId())
                        .newQuantity(product.getStockQuantity())
                        .source("ProductService")
                        .eventType("StockUpdated")
                        .build());

        return productMapper.toResponseDTO(product);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void subtractStockQuantity(UUID id, Integer quantity) {
        var product = getEntityWithLock(id);
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        productEventProducer.publishStockUpdated(
                StockUpdatedEvent.builder()
                        .productId(product.getId())
                        .newQuantity(product.getStockQuantity())
                        .source("ProductService")
                        .eventType("StockUpdated")
                        .build());
    }

    public ProductEntity getEntity(UUID id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException(String.format(PRODUCT_NOT_FOUND_WITH_ID, id)));
    }

    public ProductEntity getEntityWithLock(UUID id) {
        return productRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ProductNotFoundException(String.format(PRODUCT_NOT_FOUND_WITH_ID, id)));
    }

    private ProductCategoryEnum validateCategory(String category) {
        try {
            return ProductCategoryEnum.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidCategoryException(String.format(INVALID_CATEGORY, category));
        }
    }

    private boolean verifyIfThisSkuAlreadyExists(String sku) {
        return productRepository.existsBySku(sku);
    }
}
