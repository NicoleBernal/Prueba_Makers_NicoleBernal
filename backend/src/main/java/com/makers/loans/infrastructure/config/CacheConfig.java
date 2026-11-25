package com.makers.loans.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Los TTL y tamaños de cada caché se definen en ehcache.xml (referenciado desde application.yml).
}
