package com.app.batch.model;

public class EmploymentEventWithContext {

    private EmploymentEventRecord record;
    private String fileName;

    public EmploymentEventWithContext() {
    }

    public EmploymentEventWithContext(EmploymentEventRecord record, String fileName) {
        this.record = record;
        this.fileName = fileName;
    }

    public EmploymentEventRecord getRecord() {
        return record;
    }

    public void setRecord(EmploymentEventRecord record) {
        this.record = record;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
