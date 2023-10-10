package com.readcsb.frontend;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;



import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Route("")
public class CSVUpload extends VerticalLayout {


    Button save = new Button("Save");

    private final Grid<CustomerResponse> grid = new Grid<>(CustomerResponse.class);

    /*
    * this code load the UI, which contain upload field and data table view.
    * Here i use MultiFileMemoryBuffer for take input the csv file.
    * and then it's sent it to CsvToCustomer() method to parsing the data from CSV to
    * raw data.
    * */
        public CSVUpload() {

            MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
            Upload upload = new Upload(buffer);


            upload.addSucceededListener(event -> {
                String fileName = event.getFileName();
                if(isCSV(fileName)){

                    InputStream inputStream = buffer.getInputStream(fileName);
                    successNotification();
                    save.addClickListener(Click-> {
                        try {

                            sentBackEnd(inputStream);
                            getAllList();
                            upload.clearFileList();
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
                else {
                    warningNotification();
                }

            });

            add(upload,save,grid);
    }

    private boolean isCSV(String fileName) {
        if (fileName != null && fileName.toLowerCase().endsWith(".csv")) {
            return true;
        }
        return false;
    }


    /*
    * after loading the CSV file its show a notification
    * */
    private void successNotification(){
        Notification notification = Notification.show("File Uploaded Successfully");
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.setDuration(3000);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void warningNotification(){
        Notification notification = Notification.show("Invalid FIle Type");
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.setDuration(3000);
        notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
    }
    /*
    * this is Showing the all data in a tabular format in the frontend.
    * here, I used to @PostConstruct annotation to loading the data, when UI
    * Page is load, it means that this method call after the UI design loaded.
    * */
    @PostConstruct
    public void getAllList(){
            List<CustomerResponse> customerResponses = getAllCustomer();
            grid.setItems(customerResponses);
    }

    /*
    * In this method i Integrated a backend API which sent the list data in the backend
    * Here i create API using RestTemplate;
    * */
    private void sentBackEnd(InputStream file) throws IOException {

       List<CustomerRequest> getList= CsvTOCustomer(file);

        RestTemplate restTemplate= new RestTemplate();

        String serverUrl="http://localhost:8082/csv/upload-csv";

        String getResponse= restTemplate.postForObject(serverUrl,getList,String.class);
    }


    /*
    * Fetching list of company data using this method.
    * */
    public List<CustomerResponse> getAllCustomer(){

            RestTemplate restTemplate = new RestTemplate();
            String serverUrl= "http://localhost:8082/csv/get-all-customer";
        ResponseEntity<List<CustomerResponse>> customerList= restTemplate.exchange(serverUrl, HttpMethod.GET,null, new ParameterizedTypeReference<List<CustomerResponse>>(){});
        List<CustomerResponse> responses = customerList.getBody();

        for (CustomerResponse response: responses){
            System.out.println("-> "+response.getCompanyName()+" => "+response.getNumberOfEmployees()+" --> "+response.getEmployeesRating());
        }
        return responses;
    }

    public List<CustomerRequest> CsvTOCustomer(InputStream file) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file, "UTF-8"));


        CSVFormat format = CSVFormat.RFC4180.builder().setAllowMissingColumnNames(true).
                setHeader("Name","Employees","Rating").setSkipHeaderRecord(true).build();

        CSVParser csvParser = new CSVParser(bufferedReader, format);

        List<CustomerRequest> customerList= new ArrayList<>();

        for (CSVRecord record : csvParser) {

            String companyName= record.get(0);
            String empValue= record.get(1);
            empValue=empValue.trim();
            int numberOfEmployees = Integer.valueOf(empValue);
            double employeesRating= Double.parseDouble(record.get(2));

            if(companyName.contains("/")){
               List<String> getAllBranchCompanyName= getCompanyNameWithBranchName(companyName);

               for (String name: getAllBranchCompanyName){

                   CustomerRequest customerRequest = new CustomerRequest();
                   customerRequest.setCompanyName(name);
                   customerRequest.setNumberOfEmployees(numberOfEmployees);
                   customerRequest.setEmployeesRating(employeesRating);
                   customerList.add(customerRequest);
               }
            }else{

                CustomerRequest customerRequest = new CustomerRequest();
                customerRequest.setCompanyName(companyName);
                customerRequest.setNumberOfEmployees(numberOfEmployees);
                customerRequest.setEmployeesRating(employeesRating);

                customerList.add(customerRequest);
            }
        }
        return customerList;
    }

    private List<String> getCompanyNameWithBranchName(String companyName) {

        List<String> getSplitCompanyNameList= getCompanyNameListWithBranch(companyName);
        return getSplitCompanyNameList;
    }

    private List<String> getCompanyNameListWithBranch(String companyName) {

        String[] splitName=companyName.split("[/]");
        List<String> companyNameWithBranchList= new ArrayList<>();

        String companyNameWithOutBranch= getCompanyName(splitName[0]);

        companyNameWithBranchList.add(splitName[0]);
        for (int i=1;i<splitName.length;i++){

            String fullCompanyNameWithBranch="";
            fullCompanyNameWithBranch=companyNameWithOutBranch+" "+splitName[i];
            companyNameWithBranchList.add(fullCompanyNameWithBranch);
        }

        return companyNameWithBranchList;
    }

    private String getCompanyName(String name) {

        String[] splitCompanyName=name.split("[ ]");
        String nameWithOutBranch="";
        for (int i=0; i<splitCompanyName.length-1;i++){

            if(i==0){
                nameWithOutBranch+=splitCompanyName[i];
            }
            else{
                nameWithOutBranch+=" "+splitCompanyName[i];
            }
        }

        return nameWithOutBranch;
    }

}
