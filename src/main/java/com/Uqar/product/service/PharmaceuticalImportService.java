package com.Uqar.product.service;

import com.Uqar.product.config.PharmaceuticalImportConfig;
import com.Uqar.product.dto.ImportResponse;
import com.Uqar.product.dto.MProductDTORequest;
import com.Uqar.product.entity.Form;
import com.Uqar.product.entity.Manufacturer;
import com.Uqar.product.entity.MasterProduct;
import com.Uqar.product.repo.FormRepo;
import com.Uqar.product.repo.ManufacturerRepo;
import com.Uqar.product.repo.MasterProductRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Service for processing pharmaceutical data imports using existing entities
 * يستخدم الكيانات الموجودة: Manufacturer, Form, MasterProduct
 */
@Service
public class PharmaceuticalImportService {

    private static final Logger logger = LoggerFactory.getLogger(PharmaceuticalImportService.class);

    @Autowired
    private ManufacturerRepo manufacturerRepository;

    @Autowired
    private FormRepo formRepository;

    @Autowired
    private MasterProductRepo masterProductRepository;

    @Autowired
    private PharmaceuticalImportConfig config;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Process pharmaceutical Excel file and import data to existing tables
     */
    public ImportResponse processPharmaceuticalFile(MultipartFile file) throws IOException {
        logger.info("Starting processPharmaceuticalFile for file: {}", file.getOriginalFilename());
        logger.info("بدء معالجة ملف البيانات الصيدلانية / Starting pharmaceutical file processing: {}", file.getOriginalFilename());

        // Create temp directory if not exists
        Path tempDirPath = Paths.get(config.getTemp().getDir());
        try {
            if (!Files.exists(tempDirPath)) {
                Files.createDirectories(tempDirPath);
                logger.info("Created temp directory: {}", tempDirPath);
            }
        } catch (IOException e) {
            logger.error("Failed to create temp directory: {}", tempDirPath, e);
            throw new IOException("Failed to create temporary directory: " + e.getMessage(), e);
        }

        // Save uploaded file temporarily
        String tempFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path tempFilePath = tempDirPath.resolve(tempFileName);
        try {
            file.transferTo(tempFilePath.toFile());
            logger.info("Saved uploaded file to: {}", tempFilePath);
        } catch (IOException e) {
            logger.error("Failed to save uploaded file to: {}", tempFilePath, e);
            throw new IOException("Failed to save uploaded file: " + e.getMessage(), e);
        }

        try {
            // Call Python script to process the file with database connection
            List<MProductDTORequest> products = callPythonScriptWithDatabase(tempFilePath.toString());

            if (products == null || products.isEmpty()) {
                return new ImportResponse(false, "لم يتم العثور على بيانات صالحة في الملف / No valid data found in file", 0, null);
            }
            
            logger.info("Starting to process {} products in Spring Boot service...", products.size());

            // Process products in batches for better performance
            int successCount = 0;
            int failureCount = 0;
            StringBuilder errors = new StringBuilder();
            int batchSize = 50; // Process 50 records at a time (since we only have 100 total)
            
            logger.info("Processing {} products in batches of {} (TEST MODE: First 100 records only)", products.size(), batchSize);
            
            for (int i = 0; i < products.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, products.size());
                List<MProductDTORequest> batch = products.subList(i, endIndex);
                
                logger.info("Processing batch {}/{} (records {}-{}) - Starting at {}", 
                    (i/batchSize + 1),
                    (products.size() + batchSize - 1) / batchSize,
                    i + 1,
                    endIndex,
                    java.time.LocalTime.now());
                
                List<MasterProduct> batchEntities = new ArrayList<>();
                logger.info("Creating entities for batch of {} products...", batch.size());
                
                for (MProductDTORequest productRequest : batch) {
                    try {
                        // Create MasterProduct entity from request
                        MasterProduct masterProduct = createMasterProductFromRequest(productRequest);
                        batchEntities.add(masterProduct);
                    } catch (Exception e) {
                        failureCount++;
                        errors.append("خطأ في إضافة المنتج / Error adding product ")
                                .append(productRequest.getTradeName())
                                .append(": ")
                                .append(e.getMessage())
                                .append("\n");
                        logger.error("Error creating entity for product: {}", productRequest.getTradeName(), e);
                    }
                }
                logger.info("Created {} entities for batch", batchEntities.size());
                
                    // Save batch to database
                    if (!batchEntities.isEmpty()) {
                        try {
                            logger.info("Saving batch of {} entities to database...", batchEntities.size());
                            masterProductRepository.saveAll(batchEntities);
                            successCount += batchEntities.size();
                            logger.info("Successfully saved batch of {} products at {}", batchEntities.size(), java.time.LocalTime.now());
                        } catch (Exception e) {
                            failureCount += batchEntities.size();
                            errors.append("Error saving batch: ").append(e.getMessage()).append("\n");
                            logger.error("Error saving batch", e);
                        }
                    }
            }

            // Prepare response
            logger.info("Finished processing all products. Success: {}, Failed: {}", successCount, failureCount);
            String message = String.format(
                    "تم استيراد %d منتج بنجاح، فشل %d منتج / Successfully imported %d products, failed %d products",
                    successCount, failureCount, successCount, failureCount
            );

            boolean isSuccess = successCount > 0;
            String errorDetails = errors.length() > 0 ? errors.toString() : null;

            logger.info("انتهت معالجة الملف / File processing completed: {} success, {} failures", successCount, failureCount);

            return new ImportResponse(isSuccess, message, successCount, errorDetails);

        } finally {
            // Clean up temp file
            try {
                Files.deleteIfExists(tempFilePath);
            } catch (IOException e) {
                logger.warn("Failed to delete temp file: {}", tempFilePath, e);
            }
            logger.info("Finished processPharmaceuticalFile for file: {}", file.getOriginalFilename());
        }
    }

    /**
     * Create MasterProduct entity from request DTO
     */
    private MasterProduct createMasterProductFromRequest(MProductDTORequest request) {
        logger.debug("Creating MasterProduct entity for: {}", request.getTradeName());
        MasterProduct masterProduct = new MasterProduct();

        // Set basic properties
        masterProduct.setTradeName(request.getTradeName());
        masterProduct.setScientificName(request.getScientificName());
        masterProduct.setConcentration(request.getConcentration());
        masterProduct.setSize(request.getSize());
        masterProduct.setRefPurchasePrice(request.getRefPurchasePrice());
        masterProduct.setRefSellingPrice(request.getRefSellingPrice());
        masterProduct.setNotes(request.getNotes());
        masterProduct.setTax(request.getTax());
        masterProduct.setBarcode(request.getBarcode());
        masterProduct.setRequiresPrescription(request.getRequiresPrescription());

        // Set relationships using existing entities
        if (request.getFormId() != null) {
            Optional<Form> form = formRepository.findById(request.getFormId());
            form.ifPresent(masterProduct::setForm);
        }

        if (request.getManufacturerId() != null) {
            Optional<Manufacturer> manufacturer = manufacturerRepository.findById(request.getManufacturerId());
            manufacturer.ifPresent(masterProduct::setManufacturer);
        }

        // Note: typeId and categoryIds are not directly mapped to MasterProduct entity
        // They might need to be handled separately based on your entity structure

        logger.debug("Successfully created MasterProduct entity for: {}", request.getTradeName());
        return masterProduct;
    }

    /**
     * Call Python script with database connection parameters
     */
    private List<MProductDTORequest> callPythonScriptWithDatabase(String filePath) {
        logger.info("Starting callPythonScriptWithDatabase for file: {}", filePath);
        try {
            // Build database connection string for Python script
            String dbConnectionString = buildDatabaseConnectionString();

            // Build command to execute Python script with database config
            String scriptPath = config.getPython().getScript().getPath() + "extract_for_spring_boot.py";
            // Remove duplicate path if it exists
            if (scriptPath.contains("scripts/scripts/")) {
                scriptPath = scriptPath.replace("scripts/scripts/", "scripts/");
            }
            
            // Check if script exists
            File scriptFile = new File(scriptPath);
            if (!scriptFile.exists()) {
                logger.error("Python script not found: {}", scriptPath);
                return null;
            }
            
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python", scriptPath, filePath, dbConnectionString
            );

            // Set working directory to project root, not scripts folder
            processBuilder.directory(new File(System.getProperty("user.dir")));
            // Don't redirect stderr to stdout to keep logs separate from JSON output
            processBuilder.redirectErrorStream(false);
            
            logger.info("Executing Python script: {} with file: {}", scriptPath, filePath);
            logger.info("Database connection string: {}", dbConnectionString);
            logger.info("Working directory: {}", System.getProperty("user.dir"));
            logger.info("Script file exists: {}", scriptFile.exists());

            // Start process
            logger.info("Starting Python script execution...");
            Process process = processBuilder.start();
            logger.info("Python script started, waiting for completion...");

            // Read output from stdout only (JSON data)
            logger.info("Reading Python script output...");
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                int lineCount = 0;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    lineCount++;
                    if (lineCount % 1000 == 0) {
                        logger.info("Read {} lines from Python output", lineCount);
                    }
                }
                logger.info("Finished reading {} lines from Python output", lineCount);
            }
            
            // Read error output from stderr (logs)
            StringBuilder errorOutput = new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }
            
            // Log the error output (Python logs)
            if (errorOutput.length() > 0) {
                logger.info("Python script logs: {}", errorOutput.toString());
            }

            // Wait for process to complete
            logger.info("Waiting for Python script to complete (timeout: {} minutes)...", config.getImport().getTimeout().getMinutes());
            boolean finished = process.waitFor(config.getImport().getTimeout().getMinutes(), TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                logger.error("Python script timed out after {} minutes", config.getImport().getTimeout().getMinutes());
                return null;
            }

            int exitCode = process.exitValue();
            logger.info("Python script completed with exit code: {}", exitCode);
            logger.info("Python script output: {}", output.toString());
            if (exitCode != 0) {
                logger.error("Python script failed with exit code: {}, output: {}", exitCode, output.toString());
                return null;
            }

            // Parse JSON output from stdout
            logger.info("Parsing JSON output from Python script...");
            String jsonOutput = output.toString().trim();
            logger.info("JSON output length: {}", jsonOutput.length());
            logger.info("First 200 chars: {}", jsonOutput.substring(0, Math.min(200, jsonOutput.length())));
            logger.info("Last 200 chars: {}", jsonOutput.substring(Math.max(0, jsonOutput.length() - 200)));
            
            if (jsonOutput.startsWith("[") && jsonOutput.endsWith("]")) {
                try {
                    List<MProductDTORequest> result = objectMapper.readValue(jsonOutput, new TypeReference<List<MProductDTORequest>>() {});
                    logger.info("Successfully parsed {} products from Python script", result.size());
                    return result;
                } catch (Exception e) {
                    logger.error("Error parsing JSON from Python script: {}", e.getMessage());
                    logger.error("JSON preview: {}", jsonOutput.substring(0, Math.min(500, jsonOutput.length())));
                    return null;
                }
            } else {
                logger.error("Invalid JSON output from Python script - doesn't start with [ and end with ]");
                logger.error("Output preview: {}", jsonOutput.substring(0, Math.min(500, jsonOutput.length())));
                return null;
            }

        } catch (Exception e) {
            logger.error("Error calling Python script", e);
            return null;
        } finally {
            logger.info("Finished callPythonScriptWithDatabase");
        }
    }

    /**
     * Build database connection string for Python script
     */
    private String buildDatabaseConnectionString() {
        logger.info("Building database connection string...");
        try {
            logger.info("Building database connection string from URL: {}", databaseUrl);
            // Parse JDBC URL to create Python-compatible connection string
            if (databaseUrl.contains("postgresql")) {
                // Extract database details from JDBC URL
                // jdbc:postgresql://localhost:5432/Uqar -> postgresql://username:password@localhost:5432/Uqar
                String cleanUrl = databaseUrl.replace("jdbc:", "");
                String result = cleanUrl.replace("//", "//" + databaseUsername + ":" + databasePassword + "@");
                logger.info("Generated PostgreSQL connection string: {}", result);
                return result;
            } else if (databaseUrl.contains("mysql")) {
                // Extract database details from JDBC URL
                // jdbc:mysql://localhost:3306/pharmaceutical_db -> mysql://username:password@localhost:3306/pharmaceutical_db
                String cleanUrl = databaseUrl.replace("jdbc:", "");
                return cleanUrl.replace("//", "//" + databaseUsername + ":" + databasePassword + "@");
            } else if (databaseUrl.contains("sqlite")) {
                // Handle SQLite
                return databaseUrl.replace("jdbc:sqlite:", "sqlite://");
            } else {
                // Default fallback
                logger.warn("Unsupported database type, using SQLite fallback");
                return "sqlite:///tmp/pharmaceutical_fallback.db";
            }
        } catch (Exception e) {
            logger.error("Error building database connection string: {}", e.getMessage());
            return "sqlite:///tmp/pharmaceutical_fallback.db";
        } finally {
            logger.info("Finished building database connection string");
        }
    }

    /**
     * Get import statistics
     */
    public ImportResponse getImportStatistics() {
        logger.info("Starting getImportStatistics...");
        try {
            long totalProducts = masterProductRepository.count();
            long totalManufacturers = manufacturerRepository.count();
            long totalForms = formRepository.count();

            String message = String.format(
                    "إحصائيات قاعدة البيانات / Database Statistics: %d منتج، %d مصنع، %d شكل صيدلاني / %d products, %d manufacturers, %d forms",
                    totalProducts, totalManufacturers, totalForms, totalProducts, totalManufacturers, totalForms
            );

            return new ImportResponse(true, message, (int) totalProducts, null);

        } catch (Exception e) {
            logger.error("Error getting statistics: {}", e.getMessage());
            return new ImportResponse(false, "خطأ في الحصول على الإحصائيات / Error getting statistics", 0, e.getMessage());
        } finally {
            logger.info("Finished getImportStatistics");
        }
    }

    /**
     * Validate database connection and tables
     */
    public boolean validateDatabaseSchema() {
        logger.info("Starting database schema validation...");
        try {
            // Check if required tables exist by trying to count records
            manufacturerRepository.count();
            formRepository.count();
            masterProductRepository.count();

            logger.info("تم التحقق من صحة قاعدة البيانات / Database schema validation successful");
            return true;

        } catch (Exception e) {
            logger.error("فشل في التحقق من قاعدة البيانات / Database schema validation failed: {}", e.getMessage());
            return false;
        } finally {
            logger.info("Finished database schema validation");
        }
    }
}

