package org.beru.drive.ui.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.beru.drive.domain.FileDriveDTO;
import org.beru.drive.ui.model.API;
import org.beru.drive.ui.model.APIModel;
import org.beru.drive.ui.model.Waiter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class ApplicationController implements Initializable {
    public TableView<FileDriveDTO> contentTable;
    public Label leftStatus;
    public Label rightStatus;
    public TableColumn<FileDriveDTO, String> nameCol;
    public TableColumn<FileDriveDTO, String> lastModCol;

    private API api = new API();

    public static ApplicationController instance;

    public void uploader(ActionEvent actionEvent) {
        try {
            URL url = new URL("http://localhost:8080/drive/google/create");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/json");

            File file = new FileChooser().showOpenDialog(null);

            if(file==null)
                return;

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(new FileDriveDTO(file.getName(), file.getAbsolutePath(), "image/png"));

            System.out.println(json);

            OutputStream outputStream = con.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
            outputStreamWriter.write(json);
            outputStreamWriter.flush();
            outputStreamWriter.close();
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(instance == null){
            instance = this;
        }

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        lastModCol.setCellValueFactory(new PropertyValueFactory<>("lastMod"));
    }

    public void listFiles(){
        try {
        APIModel model = api.get("http://localhost:8080/drive/google/list");
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        if(model.getResponseCode() != HttpURLConnection.HTTP_OK){
            leftStatus.setText("Status error - Code: " + model.getResponseCode());
            new Waiter(5, (() -> {
                Platform.runLater(() -> leftStatus.setText("Left status"));
            })).start();
        }

            List<FileDriveDTO> fileDriveDTOS = mapper.readValue(model.getJson(), mapper.getTypeFactory().constructCollectionType(List.class, FileDriveDTO.class));
            fileDriveDTOS.forEach(fileDriveDTO -> {
                contentTable.getItems().add(fileDriveDTO);
            });
            leftStatus.setText("Status successfully - Code: " + model.getResponseCode());
            new Waiter(5, (() -> {
                Platform.runLater(() -> leftStatus.setText("Left status"));
            })).start();
        } catch (Exception e) {
            login(null);
        }
    }

    public void login(ActionEvent actionEvent) {
        try {
            new WebController().start(new Stage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void exit(ActionEvent actionEvent) {
    }

    public void reload(ActionEvent actionEvent) {
    }
}
