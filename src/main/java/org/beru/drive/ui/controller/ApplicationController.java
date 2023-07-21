package org.beru.drive.ui.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.beru.drive.JavaFxApplication;
import org.beru.drive.domain.CredentialsDTO;
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
    public ImageView picture;
    public Label typeText;
    public Label sizeText;
    public Label locationText;
    public Label ownerText;
    public Label modifiedText;
    public Label openText;
    public Label createdText;
    public ProgressBar progressBar;
    public Label cloudStorageText;
    public MenuItem loginText;
    public MenuButton accountText;

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
            listFiles();
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

        contentTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                FileDriveDTO file = contentTable.getSelectionModel().getSelectedItem();
                if(file == null)
                    return;
                fileInfo(file);
            }
        });
    }
    public void logged(){
        showCredentials();
        listFiles();
    }
    public void showCredentials(){
        try{
            APIModel model = api.get("http://localhost:8080/drive/google/home");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            if(model.getResponseCode() != HttpURLConnection.HTTP_OK){
                leftStatus.setText("Status error - Code: " + model.getResponseCode());
                new Waiter(5, (() -> {
                    Platform.runLater(() -> leftStatus.setText("Left status"));
                })).start();
            }

            CredentialsDTO credentialsDTO = mapper.readValue(model.getJson(), CredentialsDTO.class);
            loginText.setText(credentialsDTO.getEmail());
            accountText.setText(credentialsDTO.getName());
            ImageView view = new ImageView(new Image(credentialsDTO.getThumbnailLink()));
            view.setFitHeight(24);
            view.setFitWidth(24);
            accountText.setGraphic(view);

            int use = (int)credentialsDTO.getUseSpace()/1000000;
            int max = (int)credentialsDTO.getTotalSpace()/1000000;

            int abs = Math.abs(use-max);
            int average = (use+max)/2;

            double percentage = (double) average /abs;

            cloudStorageText.setText(use + " MB de " + max + " MB utilizado(s)");
            progressBar.setProgress(percentage);

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    public void listFiles(){
        try {
            contentTable.getItems().clear();
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
    public void fileInfo(FileDriveDTO file){
        try{
            picture.setImage(new Image(file.getThumbnailLink()));
            typeText.setText(file.getType());
            sizeText.setText(file.getSize());
            file.getOwners().forEach(user -> ownerText.setText(user.getDisplayName()));
            modifiedText.setText(file.getLastMod());
            createdText.setText(file.getCreatedDate());
        }catch (Exception e){
            picture.setImage(new Image(JavaFxApplication.class.getResource("images/error.png").getPath()));
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
        logged();
    }
}
