package org.infinitesolutions.tinyurl.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShortUrlDTO {
    private String longUrl;
    private LocalDateTime expiryDate;
}
