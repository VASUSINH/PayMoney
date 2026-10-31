package com.PayMoney.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;

// If Redis is down, the cache is simply skipped (the database is used)
// instead of failing the request. Important for transfers: the money has
// already moved when the cache is cleared, so it must not return a 500.
@Configuration
public class CacheErrorConfig implements CachingConfigurer {

    private static final Logger log =
            LoggerFactory.getLogger(CacheErrorConfig.class);

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                log.warn("Cache get failed, using database instead: {}", e.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                log.warn("Cache put failed: {}", e.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                log.warn("Cache evict failed: {}", e.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                log.warn("Cache clear failed: {}", e.getMessage());
            }
        };
    }
}
