package com.Uqar.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for pharmaceutical import response
 */
public class ImportResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("importedCount")
    private int importedCount;

    @JsonProperty("errors")
    private String errors;

    @JsonProperty("timestamp")
    private long timestamp;

    // Constructors
    public ImportResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public ImportResponse(boolean success, String message, int importedCount, String errors) {
        this.success = success;
        this.message = message;
        this.importedCount = importedCount;
        this.errors = errors;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public void setImportedCount(int importedCount) {
        this.importedCount = importedCount;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ImportResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", importedCount=" + importedCount +
                ", errors='" + errors + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

