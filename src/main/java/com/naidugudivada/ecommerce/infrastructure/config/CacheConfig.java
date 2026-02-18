package com.naidugudivada.ecommerce.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Using default Spring Boot SimpleCacheManager (ConcurrentMapCache)
    // No explicit beans needed for in-memory caching
}
