package com.naidugudivada.ecommerce.domain.search;

import com.naidugudivada.ecommerce.domain.product.ProductCategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
public class ProductDocument {

    @Id
    private UUID id;

    @Field(type = FieldType.Keyword)
    private String sku;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String label;

    @Field(type = FieldType.Keyword)
    private ProductCategoryEnum category;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Boolean)
    private Boolean active;

    @Field(type = FieldType.Double)
    private Double averageRating;

    @Field(type = FieldType.Keyword)
    private UUID vendorId;
}
