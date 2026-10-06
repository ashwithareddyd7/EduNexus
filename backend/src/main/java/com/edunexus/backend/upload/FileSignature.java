package com.edunexus.backend.upload;

import java.util.Optional;

/** Detects the real file type from the first bytes. Never trusts the filename or client content type. */
public final class FileSignature {
    private FileSignature() {}

    public record Type(String contentType, String extension) {}

    public static Optional<Type> detect(byte[] h) {
        if (h == null) return Optional.empty();
        if (h.length >= 5 && h[0] == '%' && h[1] == 'P' && h[2] == 'D' && h[3] == 'F' && h[4] == '-')
            return Optional.of(new Type("application/pdf", "pdf"));
        if (h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF)
            return Optional.of(new Type("image/jpeg", "jpg"));
        if (h.length >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
                && h[4] == 0x0D && h[5] == 0x0A && h[6] == 0x1A && h[7] == 0x0A)
            return Optional.of(new Type("image/png", "png"));
        return Optional.empty();
    }
}