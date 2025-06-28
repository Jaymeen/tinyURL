package org.infinitesolutions.tinyurl.controller;

import lombok.RequiredArgsConstructor;
import org.infinitesolutions.tinyurl.dto.ShortUrlDTO;
import org.infinitesolutions.tinyurl.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class UrlShortnerController {

    private final UrlShortenerService urlShortenerService;

    @Value("${service.url}")
    private String serviceUrl;

    @PostMapping("/shorten")
    public ResponseEntity<Map<String, String>> shorten(@RequestBody ShortUrlDTO shortUrlDTO) {
        String shortCode = urlShortenerService.shorten(shortUrlDTO);
        String fullShortUrl = serviceUrl + shortCode;

        Map<String, String> response = new HashMap<>();
        response.put("shortUrl", fullShortUrl);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> redirect(@PathVariable String code) {
        String longUrl = urlShortenerService.getLongUrl(code);

//        return ResponseEntity.status(HttpStatus.FOUND).body(longUrl);

        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(longUrl))
                .build();
    }
}
