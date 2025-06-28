package org.infinitesolutions.tinyurl.common.scheduledtask;

import lombok.RequiredArgsConstructor;
import org.infinitesolutions.tinyurl.common.constant.RedisConstants;
import org.infinitesolutions.tinyurl.repository.ShortUrlRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class SyncAnalytics {
    private final ShortUrlRepository shortUrlRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(fixedRate = 600000)
    @Transactional
    public void updateCounter() {
        Set<String> keys = redisTemplate.keys(RedisConstants.ANALYTICS_CACHE_KEY + "*");
        String shortCode;
        long clickCount;

        if(keys != null) {
            for (String key : keys) {
                shortCode = key.split(RedisConstants.SEPERATOR)[2];
                clickCount = Long.valueOf(redisTemplate.opsForValue().get(key));

                shortUrlRepository.incrementClickCount(shortCode, clickCount);
                redisTemplate.delete(key);
            }
        }
    }
}
