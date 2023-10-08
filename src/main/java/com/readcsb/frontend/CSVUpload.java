package com.readcsb.frontend;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;


import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;


import javax.swing.plaf.multi.MultiListUI;
import java.io.File;
import java.io.InputStream;

@Route("")
public class CSVUpload extends VerticalLayout {


    Button save = new Button("Save");

    private final Grid<String[]> grid = new Grid<>();

        public CSVUpload() {

            MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
            Upload upload = new Upload(buffer);

            upload.addSucceededListener(event -> {
                String fileName = event.getFileName();
                InputStream inputStream = buffer.getInputStream(fileName);



              //  System.out.println("File Name is "+fileName+", length is "+contentLength+" mime Type is : "+mimeType);
               save.addClickListener(Click->sentBackEnd(upload));

            });
            add(upload,save);
    }


    private Component buttonLayout(){
         save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
         save.addClickShortcut(Key.ENTER);

       return new HorizontalLayout(save);
    }

    private void sentBackEnd(Upload file){



        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file);


        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);

        System.out.println("This is from back end method before api call");
        String serverUrl = "http://localhost:8082/csv/upload-csv";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate
                .postForEntity(serverUrl, requestEntity, String.class);
        System.out.println("This is from back end method after api call");
        System.out.println("Your application response is : "+response);
    }

}
