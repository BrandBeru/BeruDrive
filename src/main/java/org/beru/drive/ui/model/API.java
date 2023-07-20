package org.beru.drive.ui.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Button;
import org.beru.drive.domain.FileDriveDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class API {
    public APIModel get(String path){
        try {
            URL url = new URL(path);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");

            APIModel model = new APIModel();
            model.setResponseCode(con.getResponseCode());
            model.setJson(jsonReader(new BufferedReader(new InputStreamReader(con.getInputStream()))));

            return model;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private String jsonReader(BufferedReader in) throws IOException {
        StringBuilder response = new StringBuilder();
        String line = "";
        while((line = in.readLine()) != null){
            response.append(line);
        }

        return response.toString();
    }
}
