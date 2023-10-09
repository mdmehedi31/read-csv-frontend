package com.readcsb.frontend;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.shared.util.SharedUtil;
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
@Configuration
public class CSVUpload extends VerticalLayout {


    Button save = new Button("Save");

    private final Grid<CustomerResponse> grid = new Grid<>(CustomerResponse.class);

        public CSVUpload() {

            MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
            Upload upload = new Upload(buffer);

            upload.addSucceededListener(event -> {
                String fileName = event.getFileName();
                InputStream inputStream = buffer.getInputStream(fileName);

                save.addClickListener(Click-> {
                    try {
                        sentBackEnd(inputStream);
                        getAllList();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
              //  System.out.println("File Name is "+fileName+", length is "+contentLength+" mime Type is : "+mimeType);
            });



            add(upload,save,grid);
    }



    private void listView(){
            grid.setColumns("Name","Employees","Rating");
        grid.asSingleSelect().addValueChangeListener(event ->event.getValue());
    }


    @PostConstruct
    public void getAllList(){
            List<CustomerResponse> customerResponses = getAllCustomer();
            grid.setItems(customerResponses);

    }
    private void sentBackEnd(InputStream file) throws IOException {

       List<CustomerRequest> getList= CsvTOCustomer(file);

        RestTemplate restTemplate= new RestTemplate();

        String serverUrl="http://localhost:8082/csv/upload-csv";

        String getResponse= restTemplate.postForObject(serverUrl,getList,String.class);

        System.out.println("Your response Type is : "+getResponse);


    }



    public List<CustomerResponse> getAllCustomer(){

            RestTemplate restTemplate = new RestTemplate();

            List<CustomerResponse> getCustomerList= new ArrayList<>();
            String serverUrl= "http://localhost:8082/csv/get-all-customer";

        ResponseEntity<List<CustomerResponse>> customerList= restTemplate.exchange(serverUrl, HttpMethod.GET,null, new ParameterizedTypeReference<List<CustomerResponse>>(){});

        List<CustomerResponse> responses = customerList.getBody();

        int i=1;
        for (CustomerResponse response1: responses){

            System.out.println(i+" th  Name "+response1.getName()+", Employees "+response1.getEmployees()+", Rating "+response1.getRating());

            i++;
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
