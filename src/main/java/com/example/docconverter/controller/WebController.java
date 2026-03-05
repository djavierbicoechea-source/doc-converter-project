package com.example.docconverter.controller;

import com.example.docconverter.config.AppProperties;
import com.example.docconverter.model.ConversionType;
import com.example.docconverter.model.StoredFile;
import com.example.docconverter.service.ConversionService;
import com.example.docconverter.service.PreviewService;
import com.example.docconverter.service.StorageService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class WebController {

    private final StorageService storage;
    private final ConversionService conversion;
    private final PreviewService preview;
    private final AppProperties props;

    public WebController(StorageService storage, ConversionService conversion, PreviewService preview, AppProperties props) {
        this.storage = storage;
        this.conversion = conversion;
        this.preview = preview;
        this.props = props;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("types", ConversionType.values());
        return "index";
    }

    @PostMapping("/convert")
    public String convert(@RequestParam("file") MultipartFile file,
                          @RequestParam("type") ConversionType type,
                          Model model) {

        if (file == null || file.isEmpty()) {
            model.addAttribute("types", ConversionType.values());
            model.addAttribute("error", "Veuillez choisir un fichier.");
            return "index";
        }

        String ext = FilenameUtils.getExtension(file.getOriginalFilename() == null ? "" : file.getOriginalFilename()).toLowerCase();
        if (!isAllowed(ext, type)) {
            model.addAttribute("types", ConversionType.values());
            model.addAttribute("error", "Type de fichier non compatible avec cette conversion.");
            return "index";
        }

        StoredFile uploaded = storage.saveUploaded(file);
        byte[] inputBytes = storage.readAll(uploaded.getId());

        var converted = conversion.convert(inputBytes, type);
        StoredFile out = storage.saveGenerated(file.getOriginalFilename(), converted.contentType(), converted.bytes(), converted.extension());

        String downloadUrl = props.getBaseUrl() + "/files/" + out.getId() + "/download";
        String inlineUrl = props.getBaseUrl() + "/files/" + out.getId() + "/inline";

        model.addAttribute("types", ConversionType.values());
        model.addAttribute("uploadedName", uploaded.getOriginalName());
        model.addAttribute("outName", out.getOriginalName());
        model.addAttribute("outExt", converted.extension());
        model.addAttribute("outId", out.getId());
        model.addAttribute("downloadUrl", downloadUrl);
        model.addAttribute("inlineUrl", inlineUrl);

        model.addAttribute("previewText", preview.previewText(converted.bytes(), converted.extension()));

        model.addAttribute("whatsAppShare", "https://wa.me/?text=" + urlEncode("Document converti: " + downloadUrl));
        model.addAttribute("telegramShare", "https://t.me/share/url?url=" + urlEncode(downloadUrl) + "&text=" + urlEncode("Document converti"));

        return "result";
    }

    private boolean isAllowed(String ext, ConversionType type) {
        return switch (type) {
            case PDF_TO_DOCX, PDF_TO_XLSX -> "pdf".equals(ext);
            case DOCX_TO_PDF -> "docx".equals(ext);
        };
    }

    private String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }
}
