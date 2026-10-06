package com.edunexus.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.edunexus.backend.exception.ResourceNotFoundException;

@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(@Value("${app.storage.dir}") String dir) {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create storage directory " + root, e);
        }
    }

    public void store(String storedName, MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, resolve(storedName));
        }
    }

    public Resource load(String storedName) {
        Path p = resolve(storedName);
        if (!Files.isReadable(p)) throw new ResourceNotFoundException("File not found");
        return new FileSystemResource(p);
    }

    public void delete(String storedName) {
        try {
            Files.deleteIfExists(resolve(storedName));
        } catch (IOException ignored) {
            // the database row is the source of truth; an orphan file is harmless
        }
    }

    private Path resolve(String storedName) {
        Path p = root.resolve(storedName).normalize();
        if (!p.startsWith(root)) throw new IllegalArgumentException("Invalid file name");
        return p;
    }
}