package com.example.docconverter.model;

import java.nio.file.Path;
import java.time.Instant;

public class StoredFile {
    private final String id;
    private final String originalName;
    private final String contentType;
    private final long size;
    private final Instant createdAt;
    private final Path pathOnDisk;

    public StoredFile(String id, String originalName, String contentType, long size, Instant createdAt, Path pathOnDisk) {
        this.id = id;
        this.originalName = originalName;
        this.contentType = contentType;
        this.size = size;
        this.createdAt = createdAt;
        this.pathOnDisk = pathOnDisk;
    }

    public String getId() { return id; }
    public String getOriginalName() { return originalName; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }
    public Instant getCreatedAt() { return createdAt; }
    public Path getPathOnDisk() { return pathOnDisk; }
}
