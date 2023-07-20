package org.beru.drive.ui.controller;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.beru.drive.JavaFxApplication;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class WebController extends Application implements Initializable {

    public WebView webView;
    private WebEngine webEngine;
    private static Stage stage;
    private boolean success = false;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webEngine = webView.getEngine();
        webEngine.load("http://localhost:8080/drive/google/");

        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                try {
                    URL url = new URL(webEngine.getLocation());
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    System.out.println(con.getResponseCode());
                    if(con.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED && !success){
                        ApplicationController.instance.listFiles();
                        close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(JavaFxApplication.class.getResource("WebView.fxml"));

        primaryStage.setScene(new Scene(root));
        primaryStage.setIconified(false);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setResizable(false);
        primaryStage.show();
        this.stage = primaryStage;

    }
    private void close(){
        stage.close();
        stage = null;
        success = true;
    }
    @Override
    public void stop(){
        webEngine.setOnAlert(event -> System.out.println("JS Alert: " + event.getData()));
    }

}
