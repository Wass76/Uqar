package com.Uqar.product.controller;


import com.Uqar.product.config.PharmaceuticalImportConfig;
import com.Uqar.product.dto.ImportResponse;
import com.Uqar.product.service.PharmaceuticalImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controller for handling pharmaceutical data import from Excel files
 * يستخدم الكيانات الموجودة: Manufacturer, Form, MasterProduct
 */
@RestController
@RequestMapping("/api/pharmaceutical")
@CrossOrigin(origins = "*") // Allow CORS for frontend integration
public class PharmaceuticalController {

    @Autowired
    private PharmaceuticalImportService importService;

    @Autowired
    private PharmaceuticalImportConfig config;

    /**
     * Import pharmaceutical data from Excel file to existing database tables
     *
     * @param file Excel file containing pharmaceutical data
     * @return ImportResponse with success status and details
     */
    @PostMapping("/import")
    public ResponseEntity<ImportResponse> importPharmaceuticalData(
            @RequestParam("file") MultipartFile file) {

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ImportResponse(false, "الملف فارغ / File is empty", 0, null));
            }

            // Check file extension
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                return ResponseEntity.badRequest()
                        .body(new ImportResponse(false, "نوع الملف غير مدعوم. يجب أن يكون Excel / Unsupported file type. Must be Excel", 0, null));
            }

            // Check file size
            long fileSizeMB = file.getSize() / (1024 * 1024);
            if (fileSizeMB > config.getImport().getMaxFileSize().getMb()) {
                return ResponseEntity.badRequest()
                        .body(new ImportResponse(false, 
                                String.format("حجم الملف كبير جداً. الحد الأقصى %d ميجابايت / File too large. Maximum %d MB", 
                                        config.getImport().getMaxFileSize().getMb(), config.getImport().getMaxFileSize().getMb()), 0, null));
            }

            // Validate database schema before processing
            if (!importService.validateDatabaseSchema()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ImportResponse(false, "خطأ في قاعدة البيانات. تأكد من وجود الجداول المطلوبة / Database error. Ensure required tables exist", 0, null));
            }

            // Process the file
            ImportResponse response = importService.processPharmaceuticalFile(file);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImportResponse(false, "خطأ في قراءة الملف / Error reading file: " + e.getMessage(), 0, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImportResponse(false, "خطأ غير متوقع / Unexpected error: " + e.getMessage(), 0, null));
        }
    }

    /**
     * Get import status and database statistics
     */
    @GetMapping("/import/status")
    public ResponseEntity<ImportResponse> getImportStatus() {
        try {
            ImportResponse statistics = importService.getImportStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImportResponse(false, "خطأ في الحصول على الإحصائيات / Error getting statistics: " + e.getMessage(), 0, null));
        }
    }

    /**
     * Validate database schema
     */
    @GetMapping("/import/validate")
    public ResponseEntity<ImportResponse> validateDatabase() {
        try {
            boolean isValid = importService.validateDatabaseSchema();

            if (isValid) {
                return ResponseEntity.ok(
                        new ImportResponse(true, "قاعدة البيانات صحيحة والجداول موجودة / Database is valid and tables exist", 0, null)
                );
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ImportResponse(false, "قاعدة البيانات غير صحيحة أو الجداول مفقودة / Database is invalid or tables are missing", 0, null));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImportResponse(false, "خطأ في التحقق من قاعدة البيانات / Error validating database: " + e.getMessage(), 0, null));
        }
    }

    /**
     * Get supported file formats
     */
    @GetMapping("/import/formats")
    public ResponseEntity<String[]> getSupportedFormats() {
        return ResponseEntity.ok(new String[]{"xlsx", "xls"});
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("خدمة استيراد البيانات الصيدلانية جاهزة / Pharmaceutical import service is ready");
    }

    /**
     * Get database information
     */
    @GetMapping("/database/info")
    public ResponseEntity<ImportResponse> getDatabaseInfo() {
        try {
            ImportResponse info = importService.getImportStatistics();
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImportResponse(false, "خطأ في الحصول على معلومات قاعدة البيانات / Error getting database info: " + e.getMessage(), 0, null));
        }
    }
}

