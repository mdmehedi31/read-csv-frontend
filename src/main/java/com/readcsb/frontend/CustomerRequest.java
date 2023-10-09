package com.readcsb.frontend;


import java.util.Date;

public class CustomerRequest {

    private Date created;
    private String companyName;
    private int numberOfEmployees;
    private double companyRating;

//    public CustomerRequest(String name, int employees, double rating) {
//        this.name = name;
//        this.employees = employees;
//        this.rating = rating;
//    }


    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = new Date();
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

    public void setNumberOfEmployees(int employees) {
        this.numberOfEmployees = employees;
    }

    public double getCompanyRating() {
        return companyRating;
    }

    public void setCompanyRating(double rating) {
        this.companyRating = rating;
    }
}
