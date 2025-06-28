package org.infinitesolutions.tinyurl.service;

import lombok.RequiredArgsConstructor;
import org.infinitesolutions.tinyurl.common.constant.RedisConstants;
import org.infinitesolutions.tinyurl.dto.ShortUrlDTO;
import org.infinitesolutions.tinyurl.common.exception.ShortUrlExpiredException;
import org.infinitesolutions.tinyurl.common.exception.ShortUrlNotFoundException;
import org.infinitesolutions.tinyurl.model.ShortUrl;
import org.infinitesolutions.tinyurl.repository.ShortUrlRepository;
import org.infinitesolutions.tinyurl.common.util.Base62Encoder;
import org.infinitesolutions.tinyurl.common.util.SnowflakeIdGenerator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private final ShortUrlRepository shortUrlRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final RedisTemplate<String, String> redisTemplate;

    public String shorten(ShortUrlDTO shortUrlDTO) {
        long id = idGenerator.nextId();
        String shortCode = Base62Encoder.encode(id);

        ShortUrl shortUrlEntity = new ShortUrl();
        shortUrlEntity.setLongUrl(shortUrlDTO.getLongUrl());
        shortUrlEntity.setShortCode(shortCode);
        shortUrlEntity.setCreatedAt(LocalDateTime.now());
        shortUrlEntity.setExpiresAt(shortUrlDTO.getExpiryDate());
        shortUrlRepository.save(shortUrlEntity);

        return shortCode;
    }

    public String getLongUrl(String shortCode) {
        String cacheKey = RedisConstants.URL_CACHE_KEY + shortCode;
        String analyticsCacheKey = RedisConstants.ANALYTICS_CACHE_KEY + shortCode;

        String longUrl = redisTemplate.opsForValue().get(cacheKey);

        if(longUrl != null) {
            updateCounter(analyticsCacheKey);
            return longUrl;
        }

        ShortUrl shortUrlData = shortUrlRepository.findByShortCode(shortCode);
        if(shortUrlData == null) {
            throw new ShortUrlNotFoundException("Short URL not found for code: " + shortCode);
        }

        if(shortUrlData.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ShortUrlExpiredException("Short URL has expired for code: " + shortCode);
        }

        Duration cacheTtl = getCacheKeyTtl(shortUrlData);

        redisTemplate.opsForValue().set(cacheKey, shortUrlData.getLongUrl(), cacheTtl);
        redisTemplate.opsForValue().set(analyticsCacheKey, "1", cacheTtl);

        return shortUrlData.getLongUrl();
    }

    private Duration getCacheKeyTtl(ShortUrl shortUrlData) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = shortUrlData.getExpiresAt();

        Duration timeToExpiry = Duration.between(now, expiresAt);
        Duration sixHours = Duration.ofHours(6);

        return timeToExpiry.compareTo(sixHours) < 0 ? timeToExpiry : sixHours;
    }

    private void updateCounter(String analyticsCacheKey) {
        redisTemplate.opsForValue().increment(analyticsCacheKey);
    }
}
