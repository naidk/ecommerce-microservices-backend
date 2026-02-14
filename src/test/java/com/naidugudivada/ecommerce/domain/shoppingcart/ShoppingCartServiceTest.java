package com.naidugudivada.ecommerce.domain.shoppingcart;

import com.naidugudivada.ecommerce.domain.customer.CustomerService;
import com.naidugudivada.ecommerce.domain.product.ProductEntity;
import com.naidugudivada.ecommerce.domain.product.ProductService;
import com.naidugudivada.ecommerce.domain.shoppingcart.dto.ShoppingCartRequestDTO;
import com.naidugudivada.ecommerce.domain.shoppingcart.dto.ShoppingCartResponseDTO;
import com.naidugudivada.ecommerce.domain.shoppingcart.exceptions.NegativeQuantityException;
import com.naidugudivada.ecommerce.domain.shoppingcart.exceptions.QuantityGreaterThanAvailableException;
import com.naidugudivada.ecommerce.domain.shoppingcart.exceptions.ShoppingCartNotFoundException;
import com.naidugudivada.ecommerce.domain.shoppingcart.shoppingcartitem.ShoppingCartItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.naidugudivada.ecommerce.utils.ProductTestUtils.createProductEntity;
import static com.naidugudivada.ecommerce.utils.ShoppingCartTestUtils.createShoppingCartEntity;
import static com.naidugudivada.ecommerce.utils.ShoppingCartTestUtils.createShoppingCartRequestDTO;
import static com.naidugudivada.ecommerce.utils.ShoppingCartTestUtils.createShoppingCartRequestDTOWithInvalidQuantity;
import static com.naidugudivada.ecommerce.utils.ShoppingCartTestUtils.createShoppingCartResponseDTO;
import static com.naidugudivada.ecommerce.utils.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private ProductService productService;

    @Mock
    private CustomerService customerService;

    @Mock
    private ShoppingCartItemService shoppingCartItemService;

    @Mock
    private ShoppingCartMapper mapper;

    @InjectMocks
    private ShoppingCartService shoppingCartService;

    private ProductEntity productEntity;
    private ShoppingCartEntity shoppingCartEntity;
    private ShoppingCartRequestDTO shoppingCartRequestDTO;
    private ShoppingCartResponseDTO shoppingCartResponseDTO;

    @BeforeEach
    void setUp() {
        shoppingCartEntity = createShoppingCartEntity();
        productEntity = createProductEntity();
        shoppingCartRequestDTO = createShoppingCartRequestDTO();
        shoppingCartResponseDTO = createShoppingCartResponseDTO();
    }

    @Test
    void testAddProductToShoppingCart() {
        // arrange
        when(productService.getEntity(productEntity.getId())).thenReturn(productEntity);
        when(shoppingCartRepository.findByCustomerId(ID))
                .thenReturn(Optional.of(shoppingCartEntity));
        when(mapper.toResponseDTO(shoppingCartEntity)).thenReturn(shoppingCartResponseDTO);

        // act
        var response = shoppingCartService.addToCart(ID, shoppingCartRequestDTO);

        // assert
        assertThat(response).isNotNull().isEqualTo(shoppingCartResponseDTO);
        verify(shoppingCartRepository).findByCustomerId(ID);
        verify(productService).getEntity(productEntity.getId());
    }

    @Test
    void testAddProductToShoppingCartEmptyItems() {
        // arrange
        when(productService.getEntity(productEntity.getId())).thenReturn(productEntity);
        when(shoppingCartRepository.findByCustomerId(ID))
                .thenReturn(Optional.of(shoppingCartEntity));
        when(mapper.toResponseDTO(shoppingCartEntity)).thenReturn(shoppingCartResponseDTO);

        shoppingCartEntity.getItems().clear();

        // act
        var response = shoppingCartService.addToCart(ID, shoppingCartRequestDTO);

        // assert
        assertThat(response).isNotNull().isEqualTo(shoppingCartResponseDTO);
        verify(shoppingCartRepository).findByCustomerId(ID);
        verify(productService).getEntity(productEntity.getId());
    }

    @Test
    void testAddProductToShoppingCartQuantityGreaterThanAvailable() {
        // arrange
        when(productService.getEntity(productEntity.getId())).thenReturn(productEntity);

        productEntity.setStockQuantity(2);

        // act and assert
        assertThatException()
                .isThrownBy(() -> shoppingCartService.addToCart(ID, shoppingCartRequestDTO))
                .isInstanceOf(QuantityGreaterThanAvailableException.class);
    }

    @Test
    void testRemoveProductFromShoppingCart() {
        // arrange
        when(shoppingCartRepository.findByCustomerId(ID))
                .thenReturn(Optional.of(shoppingCartEntity));
        when(mapper.toResponseDTO(shoppingCartEntity)).thenReturn(shoppingCartResponseDTO);

        // act
        var response = shoppingCartService.removeFromCart(ID, shoppingCartRequestDTO);

        // assert
        assertThat(response).isNotNull().isEqualTo(shoppingCartResponseDTO);
        verify(shoppingCartRepository).findByCustomerId(ID);
        verify(productService).addStockById(productEntity.getId(), shoppingCartRequestDTO.quantity());
    }

    @Test
    void testRemoveFromCartNegativeQuantity() {
        // arrange
        when(shoppingCartRepository.findByCustomerId(ID))
                .thenReturn(Optional.of(shoppingCartEntity));

        var invalidRequest = createShoppingCartRequestDTOWithInvalidQuantity();

        // act and assert
        assertThatException()
                .isThrownBy(() -> shoppingCartService.removeFromCart(ID, invalidRequest))
                .isInstanceOf(NegativeQuantityException.class);
    }

    @Test
    void testGetShoppingCartByCustomerId() {
        // arrange
        when(shoppingCartRepository.findByCustomerId(ID))
                .thenReturn(Optional.of(shoppingCartEntity));
        when(mapper.toResponseDTO(shoppingCartEntity)).thenReturn(shoppingCartResponseDTO);

        // act
        var response = shoppingCartService.getShoppingCartByCustomerId(ID);

        // assert
        assertThat(response).isNotNull().isEqualTo(shoppingCartResponseDTO);
    }

    @Test
    void testSave() {
        // arrange
        when(shoppingCartRepository.save(shoppingCartEntity)).thenReturn(shoppingCartEntity);

        // act
        shoppingCartService.save(shoppingCartEntity);

        // assert
        verify(shoppingCartRepository).save(shoppingCartEntity);
    }

    @Test
    void testGetEntity() {
        // arrange
        when(shoppingCartRepository.findById(shoppingCartEntity.getId()))
                .thenReturn(Optional.of(shoppingCartEntity));

        // act
        var response = shoppingCartService.getEntity(ID);

        // assert
        assertThat(response).isNotNull().isEqualTo(shoppingCartEntity);
    }

    @Test
    void testGetEntityNotFound() {
        // arrange
        when(shoppingCartRepository.findById(ID)).thenReturn(Optional.empty());

        // act and assert
        assertThatException()
                .isThrownBy(() -> shoppingCartService.getEntity(ID))
                .isInstanceOf(ShoppingCartNotFoundException.class);
    }

}
