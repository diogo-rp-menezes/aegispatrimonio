package br.com.aegispatrimonio.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Spring Boot auto-configures a SimpleCacheManager/ConcurrentMapCacheManager if no other provider is present.
}
