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
import org.springframework.context.annotation.Configuration;
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
                InputStream inputStream = buffer.getInputStream(fileName);
                notificationStyle();
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
            });

            add(upload,save,grid);
    }


    /*
    * after loading the CSV file its show a notification
    * */
    private void notificationStyle(){
        Notification notification = Notification.show("File Uploaded Successfully");
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.setDuration(3000);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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
        return responses;
    }

    public List<CustomerRequest> CsvTOCustomer(InputStream file) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file, "UTF-8"));


        CSVFormat format = CSVFormat.RFC4180.builder().setAllowMissingColumnNames(true).
                setHeader("Name","Employees","Rating").setSkipHeaderRecord(true).build();

        CSVParser csvParser = new CSVParser(bufferedReader, format);

        List<CustomerRequest> customerList= new ArrayList<>();

        for (CSVRecord record : csvParser) {

            String name= record.get(0);

            String empValue= record.get(1);
            empValue=empValue.trim();
            int employees = Integer.valueOf(empValue);
            double rating= Double.parseDouble(record.get(2));

            CustomerRequest customerRequest = new CustomerRequest();
            customerRequest.setName(name);
            customerRequest.setEmployees(employees);
            customerRequest.setRating(rating);

            customerList.add(customerRequest);
        }
        return customerList;
    }

}
