package com.naidugudivada.ecommerce.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
// @EnableElasticsearchRepositories(basePackages =
// "com.naidugudivada.ecommerce.domain.search")
public class ElasticsearchConfig {
    // Spring Boot AutoConfiguration handles the RestClient connection based on
    // the spring.elasticsearch.uris property in application.properties.
    // We just need to enable the repositories here.
}
