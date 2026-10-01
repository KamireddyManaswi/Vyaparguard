package com.vyaparguard.dto;

import java.util.ArrayList;
import java.util.List;

public class CsvImportResultDto {
    private boolean success;
    private int importedCount;
    private int failedCount;
    private List<String> errors = new ArrayList<>();
    private String message;

    public CsvImportResultDto() {
    }

    public CsvImportResultDto(boolean success, int importedCount, int failedCount, List<String> errors, String message) {
        this.success = success;
        this.importedCount = importedCount;
        this.failedCount = failedCount;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public void setImportedCount(int importedCount) {
        this.importedCount = importedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
