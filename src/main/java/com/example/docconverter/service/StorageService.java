package com.example.docconverter.service;

import com.example.docconverter.config.AppProperties;
import com.example.docconverter.model.StoredFile;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StorageService {

    private final Path root;
    private final Map<String, StoredFile> db = new ConcurrentHashMap<>();

    public StorageService(AppProperties props) {
        this.root = Paths.get(props.getStorageDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create storage directory: " + root, e);
        }
    }

    public StoredFile saveUploaded(MultipartFile file) {
        String id = UUID.randomUUID().toString().replace("-", "");
        String ext = safeExt(file.getOriginalFilename());
        String filename = "upload_" + id + (ext.isBlank() ? "" : "." + ext);

        Path dest = root.resolve(filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }

        StoredFile stored = new StoredFile(
                id,
                file.getOriginalFilename() == null ? filename : file.getOriginalFilename(),
                file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
                file.getSize(),
                Instant.now(),
                dest
        );
        db.put(id, stored);
        return stored;
    }

    public StoredFile saveGenerated(String originalName, String contentType, byte[] bytes, String extension) {
        String id = UUID.randomUUID().toString().replace("-", "");
        String base = FilenameUtils.getBaseName(originalName == null ? "converted" : originalName);
        String filename = base + "_converted_" + id + "." + extension;

        Path dest = root.resolve(filename);
        try {
            Files.write(dest, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Saving output failed", e);
        }

        StoredFile stored = new StoredFile(
                id,
                filename,
                contentType,
                bytes.length,
                Instant.now(),
                dest
        );
        db.put(id, stored);
        return stored;
    }

    public StoredFile get(String id) {
        StoredFile f = db.get(id);
        if (f == null) throw new IllegalArgumentException("File not found: " + id);
        return f;
    }

    public byte[] readAll(String id) {
        try {
            return Files.readAllBytes(get(id).getPathOnDisk());
        } catch (Exception e) {
            throw new RuntimeException("Read failed", e);
        }
    }

    private String safeExt(String name) {
        if (name == null) return "";
        String ext = FilenameUtils.getExtension(name);
        return ext == null ? "" : ext.toLowerCase();
    }
}
