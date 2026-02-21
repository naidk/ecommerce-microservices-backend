package com.naidugudivada.ecommerce.domain.shoppingcart;

import com.naidugudivada.ecommerce.domain.product.ProductEntity;
import com.naidugudivada.ecommerce.domain.product.ProductService;
import com.naidugudivada.ecommerce.domain.product.exceptions.ProductNotFoundException;
import com.naidugudivada.ecommerce.domain.shoppingcart.dto.ShoppingCartRequestDTO;
import com.naidugudivada.ecommerce.domain.shoppingcart.dto.ShoppingCartResponseDTO;
import com.naidugudivada.ecommerce.domain.shoppingcart.exceptions.NegativeQuantityException;
import com.naidugudivada.ecommerce.domain.shoppingcart.exceptions.QuantityGreaterThanAvailableException;
import com.naidugudivada.ecommerce.domain.shoppingcart.exceptions.ShoppingCartNotFoundException;
import com.naidugudivada.ecommerce.domain.shoppingcart.shoppingcartitem.ShoppingCartItemEntity;
import com.naidugudivada.ecommerce.domain.customer.CustomerEntity;
import com.naidugudivada.ecommerce.domain.customer.CustomerRepository;
import com.naidugudivada.ecommerce.domain.shoppingcart.shoppingcartitem.ShoppingCartItemService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.NEGATIVE_QUANTITY;
import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.PRODUCT_NOT_FOUND_WITH_ID;
import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.QUANTITY_GREATER_THAN_AVAILABLE;
import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.SHOPPING_CART_NOT_FOUND;
import static com.naidugudivada.ecommerce.infrastructure.constants.ErrorMessages.SHOPPING_CART_NOT_FOUND_WITH_ID;

@Service
@AllArgsConstructor
@Slf4j
public class ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartItemService shoppingCartItemService;
    private final ProductService productService;
    private final CustomerRepository customerRepository;
    private final com.naidugudivada.ecommerce.domain.promotion.PromotionService promotionService;
    private final ShoppingCartMapper mapper;

    @Transactional
    public ShoppingCartResponseDTO addToCart(UUID customerId, ShoppingCartRequestDTO request) {
        log.info("Request to add Product [{}] (Qty: {}) to cart for Customer [{}]", request.productId(),
                request.quantity(), customerId);

        var product = productService.getEntity(request.productId());

        if (request.quantity() > product.getStockQuantity()) {
            log.warn("Insufficient stock for Product [{}]. Requested: {}, Available: {}", product.getId(),
                    request.quantity(), product.getStockQuantity());
            throw new QuantityGreaterThanAvailableException(QUANTITY_GREATER_THAN_AVAILABLE);
        }

        ShoppingCartEntity cart = shoppingCartRepository.findByCustomerId(customerId)
                .orElseGet(() -> createCart(customerId));

        var cartItemOpt = findCartItem(cart, product);

        if (cartItemOpt.isPresent()) {
            var item = cartItemOpt.get();
            item.setQuantity(item.getQuantity() + request.quantity());
        } else {
            var newCartItem = ShoppingCartItemEntity.builder()
                    .shoppingCart(cart)
                    .product(product)
                    .quantity(request.quantity())
                    .priceAtAddedTime(product.getPrice())
                    .build();
            cart.getItems().add(newCartItem);
        }

        updateTotalPrice(cart);

        shoppingCartRepository.save(cart);
        productService.subtractStockQuantity(product.getId(), request.quantity());

        log.info("Cart updated for Customer [{}]. New Total: {}", customerId, cart.getTotalPrice());
        return mapper.toResponseDTO(cart);
    }

    @Transactional
    public ShoppingCartResponseDTO removeFromCart(UUID customerId, ShoppingCartRequestDTO request) {
        log.info("Request to remove Product [{}] (Qty: {}) from cart for Customer [{}]", request.productId(),
                request.quantity(), customerId);

        var cart = findByCustomerId(customerId);

        var item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(request.productId()))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException(
                        String.format(PRODUCT_NOT_FOUND_WITH_ID, request.productId())));

        if (item.getQuantity() - request.quantity() < 0) {
            log.warn("Attempt to remove more items than exist in cart for Product [{}]", request.productId());
            throw new NegativeQuantityException(NEGATIVE_QUANTITY);
        }

        item.setQuantity(item.getQuantity() - request.quantity());

        if (item.getQuantity() == 0) {
            log.debug("Item quantity reached 0. Removing Product [{}] from cart completely.", request.productId());
            cart.getItems().remove(item);
            shoppingCartItemService.delete(item);
        }

        updateTotalPrice(cart);

        shoppingCartRepository.save(cart);
        productService.addStockById(request.productId(), request.quantity());

        log.info("Cart item removed for Customer [{}]. Stock restored.", customerId);
        return mapper.toResponseDTO(cart);
    }

    @Transactional
    public ShoppingCartResponseDTO applyPromotion(UUID customerId, String code) {
        log.info("Customer [{}] applying promotion [{}]", customerId, code);
        var cart = findByCustomerId(customerId);
        var promotion = promotionService.validateAndGetPromotion(code);

        cart.setAppliedPromotion(promotion);
        updateTotalPrice(cart);
        shoppingCartRepository.save(cart);

        return mapper.toResponseDTO(cart);
    }

    @Transactional
    public ShoppingCartResponseDTO removePromotion(UUID customerId) {
        log.info("Customer [{}] removing applied promotion", customerId);
        var cart = findByCustomerId(customerId);

        cart.setAppliedPromotion(null);
        updateTotalPrice(cart);
        shoppingCartRepository.save(cart);

        return mapper.toResponseDTO(cart);
    }

    @Transactional(readOnly = true)
    public ShoppingCartResponseDTO getShoppingCartByCustomerId(UUID customerId) {
        return mapper.toResponseDTO(findByCustomerId(customerId));
    }

    @Transactional
    public void save(ShoppingCartEntity cart) {
        shoppingCartRepository.save(cart);
    }

    public ShoppingCartEntity findByCustomerId(UUID customerId) {
        return shoppingCartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ShoppingCartNotFoundException(SHOPPING_CART_NOT_FOUND));
    }

    public ShoppingCartEntity getEntity(UUID id) {
        return shoppingCartRepository.findById(id)
                .orElseThrow(
                        () -> new ShoppingCartNotFoundException(String.format(SHOPPING_CART_NOT_FOUND_WITH_ID, id)));
    }

    private ShoppingCartEntity createCart(UUID customerId) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ShoppingCartNotFoundException("Customer not found with id: " + customerId));

        ShoppingCartEntity newCart = ShoppingCartEntity.builder()
                .customer(customer)
                .totalPrice(BigDecimal.ZERO)
                .build();
        return shoppingCartRepository.save(newCart);
    }

    private Optional<ShoppingCartItemEntity> findCartItem(ShoppingCartEntity shoppingCart, ProductEntity product) {
        return shoppingCart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst();
    }

    private void updateTotalPrice(ShoppingCartEntity cart) {
        var subtotal = cart.getItems().stream()
                .map(item -> item.getPriceAtAddedTime().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var discountAmount = BigDecimal.ZERO;

        if (cart.getAppliedPromotion() != null) {
            var promo = cart.getAppliedPromotion();
            if (promo.isValid()) {
                if (promo
                        .getDiscountType() == com.naidugudivada.ecommerce.domain.promotion.DiscountTypeEnum.PERCENTAGE) {
                    // e.g., 20% off -> subtotal * 0.20
                    discountAmount = subtotal.multiply(promo.getDiscountValue().divide(BigDecimal.valueOf(100)));
                } else {
                    // Fixed amount off
                    discountAmount = promo.getDiscountValue();
                }
            } else {
                // If the promo became invalid (expired, deactivated), remove it
                cart.setAppliedPromotion(null);
            }
        }

        // Ensure discount doesn't exceed subtotal
        if (discountAmount.compareTo(subtotal) > 0) {
            discountAmount = subtotal;
        }

        cart.setDiscountAmount(discountAmount);
        cart.setTotalPrice(subtotal.subtract(discountAmount));
    }
}
