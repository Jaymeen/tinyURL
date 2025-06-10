package org.infinitesolutions.tinyurl.common.exception;

public class ShortUrlExpiredException extends RuntimeException {
    public ShortUrlExpiredException(String message) {
        super(message);
    }
}
