package com.app.batch.model;

public class EmploymentEventRecord {

    private String ssn;
    private String plan;
    private String clientId;
    private String employmentEvent;

    public EmploymentEventRecord() {
    }

    public EmploymentEventRecord(String ssn, String plan, String clientId, String employmentEvent) {
        this.ssn = ssn;
        this.plan = plan;
        this.clientId = clientId;
        this.employmentEvent = employmentEvent;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getEmploymentEvent() {
        return employmentEvent;
    }

    public void setEmploymentEvent(String employmentEvent) {
        this.employmentEvent = employmentEvent;
    }
}
