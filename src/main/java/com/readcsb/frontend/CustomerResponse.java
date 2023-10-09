package com.readcsb.frontend;


import java.util.Date;

public class CustomerResponse {

    private Date created;
    private String companyName;
    private int numberOfEmployees;
    private double employeesRating;

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public void setNumberOfEmployees(int numberOfEmployees) {
        this.numberOfEmployees = numberOfEmployees;
    }

    public double getEmployeesRating() {
        return employeesRating;
    }

    public void setEmployeesRating(double employeesRating) {
        this.employeesRating = employeesRating;
    }
}
