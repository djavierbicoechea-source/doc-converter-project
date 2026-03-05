package com.example.docconverter.controller;

import com.example.docconverter.model.StoredFile;
import com.example.docconverter.service.StorageService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/files")
public class FileController {

    private final StorageService storage;

    public FileController(StorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String id) {
        StoredFile f = storage.get(id);
        byte[] bytes = storage.readAll(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + sanitize(f.getOriginalName()) + "\"")
                .contentType(safeMediaType(f.getContentType()))
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    @GetMapping("/{id}/inline")
    public ResponseEntity<ByteArrayResource> inline(@PathVariable String id) {
        StoredFile f = storage.get(id);
        byte[] bytes = storage.readAll(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + sanitize(f.getOriginalName()) + "\"")
                .contentType(safeMediaType(f.getContentType()))
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    private MediaType safeMediaType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String sanitize(String s) {
        if (s == null) return "file";
        return s.replace("\"", "");
    }
}
