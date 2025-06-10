package org.infinitesolutions.tinyurl.repository;

import org.infinitesolutions.tinyurl.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    ShortUrl findByShortUrl(String shortUrl);

    int deleteByExpiresAtIsBefore(LocalDateTime expiresAtBefore);
}
