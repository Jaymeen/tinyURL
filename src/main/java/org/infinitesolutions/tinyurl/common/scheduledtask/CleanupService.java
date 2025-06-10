package org.infinitesolutions.tinyurl.common.scheduledtask;

import lombok.RequiredArgsConstructor;
import org.infinitesolutions.tinyurl.repository.ShortUrlRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CleanupService {
    private final ShortUrlRepository shortUrlRepository;

    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional
    public void deleteExpiredUrls() {
        int deletedCount = shortUrlRepository.deleteByExpiresAtIsBefore(LocalDateTime.now());
        if (deletedCount > 0) {
            System.out.println("Deleted " + deletedCount + " expired short urls");
        }
    }
}
