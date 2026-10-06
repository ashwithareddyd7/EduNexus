package com.edunexus.backend.upload;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class FileSignatureTest {

    @Test
    void detectsPdf() {
        var t = FileSignature.detect("%PDF-1.7 rest".getBytes(StandardCharsets.US_ASCII));
        assertEquals("application/pdf", t.orElseThrow().contentType());
        assertEquals("pdf", t.orElseThrow().extension());
    }

    @Test
    void detectsJpeg() {
        byte[] b = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0};
        assertEquals("image/jpeg", FileSignature.detect(b).orElseThrow().contentType());
    }

    @Test
    void detectsPng() {
        byte[] b = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        assertEquals("image/png", FileSignature.detect(b).orElseThrow().contentType());
    }

    @Test
    void rejectsTextAndEmptyAndShort() {
        assertTrue(FileSignature.detect("hello world".getBytes(StandardCharsets.US_ASCII)).isEmpty());
        assertTrue(FileSignature.detect(new byte[0]).isEmpty());
        assertTrue(FileSignature.detect(new byte[] {'%', 'P'}).isEmpty());
        assertTrue(FileSignature.detect(null).isEmpty());
    }
}