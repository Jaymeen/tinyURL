package org.infinitesolutions.tinyurl.repository;

import org.infinitesolutions.tinyurl.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    ShortUrl findByShortCode(String shortCode);

    int deleteByExpiresAtIsBefore(LocalDateTime expiresAtBefore);

    @Modifying
    @Query("UPDATE ShortUrl s SET s.clickCount = s.clickCount + :count WHERE s.shortCode = :code")
    void incrementClickCount(@Param("code") String shortCode, @Param("count") Long count);
}
