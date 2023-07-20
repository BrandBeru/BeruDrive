package org.beru.drive;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application {
    private ConfigurableApplicationContext applicationContext;
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Application.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("Beru Drive");
        primaryStage.setScene(scene);
        primaryStage.show();

        applicationContext.publishEvent(new StageReadyEvent(primaryStage));
    }
    @Override
    public void init(){
        applicationContext = new SpringApplicationBuilder(DriveApplication.class).run();
    }
    @Override
    public void stop(){
        applicationContext.close();
        Platform.exit();
    }

    public static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) {
            super(stage);
        }
    }
}
