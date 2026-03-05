package com.example.docconverter.service;

import com.example.docconverter.model.ConversionType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class ConversionService {

    public record ConvertedResult(byte[] bytes, String contentType, String extension) {}

    public ConvertedResult convert(byte[] inputBytes, ConversionType type) {
        try {
            return switch (type) {
                case PDF_TO_DOCX -> pdfToDocx(inputBytes);
                case DOCX_TO_PDF -> docxToPdf(inputBytes);
                case PDF_TO_XLSX -> pdfToXlsx(inputBytes);
            };
        } catch (Exception e) {
            throw new RuntimeException("Conversion failed: " + e.getMessage(), e);
        }
    }

    private ConvertedResult pdfToDocx(byte[] pdfBytes) throws Exception {
        String text = extractTextFromPdf(pdfBytes);

        XWPFDocument doc = new XWPFDocument();
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setFontFamily("Calibri");
        run.setFontSize(11);

        for (String line : text.split("\\R")) {
            run.setText(line);
            run.addBreak();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.write(out);
        doc.close();

        return new ConvertedResult(out.toByteArray(),
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "docx");
    }

    private ConvertedResult docxToPdf(byte[] docxBytes) throws Exception {
        try (InputStream in = new ByteArrayInputStream(docxBytes);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(in);
            Docx4J.toPDF(wordMLPackage, out);

            return new ConvertedResult(out.toByteArray(), "application/pdf", "pdf");
        }
    }

    private ConvertedResult pdfToXlsx(byte[] pdfBytes) throws Exception {
        String text = extractTextFromPdf(pdfBytes);

        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("PDF_Text");

        int r = 0;
        for (String line : text.split("\\R")) {
            String clean = line == null ? "" : line.strip();
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(clean);
        }
        sheet.setColumnWidth(0, 12000);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();

        return new ConvertedResult(out.toByteArray(),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "xlsx");
    }

    private String extractTextFromPdf(byte[] pdfBytes) throws Exception {
        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            return text == null ? "" : text;
        }
    }
}
