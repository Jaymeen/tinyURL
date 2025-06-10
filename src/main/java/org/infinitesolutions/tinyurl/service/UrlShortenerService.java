package org.infinitesolutions.tinyurl.service;

import lombok.RequiredArgsConstructor;
import org.infinitesolutions.tinyurl.dto.ShortUrlDTO;
import org.infinitesolutions.tinyurl.common.exception.ShortUrlExpiredException;
import org.infinitesolutions.tinyurl.common.exception.ShortUrlNotFoundException;
import org.infinitesolutions.tinyurl.model.ShortUrl;
import org.infinitesolutions.tinyurl.repository.ShortUrlRepository;
import org.infinitesolutions.tinyurl.common.util.Base62Encoder;
import org.infinitesolutions.tinyurl.common.util.SnowflakeIdGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private final ShortUrlRepository shortUrlRepository;
    private final SnowflakeIdGenerator idGenerator;

    public String shorten(ShortUrlDTO shortUrlDTO) {
        long id = idGenerator.nextId();
        String shortCode = Base62Encoder.encode(id);

        ShortUrl shortUrlEntity = new ShortUrl();
        shortUrlEntity.setLongUrl(shortUrlDTO.getLongUrl());
        shortUrlEntity.setShortUrl(shortCode);
        shortUrlEntity.setCreatedAt(LocalDateTime.now());
        shortUrlEntity.setExpiresAt(shortUrlDTO.getExpiryDate());
        shortUrlRepository.save(shortUrlEntity);

        return shortCode;
    }

    public String getLongUrl(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortUrl(shortCode);
        if(shortUrl == null) {
            throw new ShortUrlNotFoundException("Short URL not found for code: " + shortCode);
        }

        if(shortUrl.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ShortUrlExpiredException("Short URL has expired for code: " + shortCode);
        }

        return shortUrl.getLongUrl();
    }
}
