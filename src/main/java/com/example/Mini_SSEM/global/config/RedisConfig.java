package com.example.Mini_SSEM.global.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching // 스프링의 캐시 어노테이션(@Cacheable) 활성화
public class RedisConfig {

    // Redis에 직접 접근할 때 사용
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // Key는 String으로 직렬화 (읽기 편하게)
        template.setKeySerializer(new StringRedisSerializer());

        // Value는 JSON으로 직렬화 (객체 내용을 사람이 읽을 수 있게)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    // 캐시 매니저 활성화 -> 안 해주면 @Cacheable 어노테이션이 제대로 작동하지 아니함
    // Spring의 캐시 추상화(@Cacheable, @CacheEvict)가 작동하려면 반드시 필요
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                );

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
