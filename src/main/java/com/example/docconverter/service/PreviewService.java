package com.example.docconverter.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
public class PreviewService {

    public String previewText(byte[] bytes, String ext) {
        try {
            return switch (ext.toLowerCase()) {
                case "pdf" -> previewPdfText(bytes);
                case "docx" -> previewDocxText(bytes);
                case "xlsx" -> "Fichier Excel généré. Télécharge pour l’ouvrir (preview texte non activé pour XLSX).";
                default -> "Preview non disponible pour ce format.";
            };
        } catch (Exception e) {
            return "Erreur preview: " + e.getMessage();
        }
    }

    private String previewPdfText(byte[] bytes) throws Exception {
        try (PDDocument doc = PDDocument.load(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String t = stripper.getText(doc);
            return t == null ? "" : t;
        }
    }

    private String previewDocxText(byte[] bytes) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : doc.getParagraphs()) {
                sb.append(p.getText()).append("\n");
            }
            return sb.toString();
        }
    }
}
