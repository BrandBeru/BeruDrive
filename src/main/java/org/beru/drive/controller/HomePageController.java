package org.beru.drive.controller;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.beru.drive.domain.CredentialsDTO;
import org.beru.drive.domain.FileDriveDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/google")
public class HomePageController {
    private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    private static final String USER_IDENTIFIER_KEY = "BeruDrive";
    @Value("${google.oauth.callback.uri}")
    private String CALLBACK_URI;
    @Value("${google.home.uri}")
    private String HOME_URI;
    @Value("${google.oauth.name}")
    private String APP_NAME;
    @Value("${google.secret.key.path}")
    private Resource gdSecretKeys;
    @Value("${google.credentials.folder.path}")
    private Resource credentialsFolder;
    private GoogleAuthorizationCodeFlow flow;

    @PostConstruct
    public void init(){
        try {
            GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(gdSecretKeys.getInputStream()));
            flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, secrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(credentialsFolder.getFile())).build();
            url = flow.newAuthorizationUrl();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private GoogleAuthorizationCodeRequestUrl url;
    @GetMapping(value = {"/"})
    public void showHomePage(HttpServletResponse response) throws IOException {
        Credential credential = flow.loadCredential(USER_IDENTIFIER_KEY);
        if(credential!=null) {
            if(credential.refreshToken()) {
                String redirectUrl = url.setRedirectUri(HOME_URI).setAccessType("online").build();
                response.sendRedirect(redirectUrl);
            }
            return;
        }

        String redirectUrl = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
        response.sendRedirect(redirectUrl);
    }
    @GetMapping(value = {"/signin"})
    public void doGoogleSignIn(HttpServletResponse response){
        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
        String redirectUrl = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
        try {
            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping(value = {"/oauth"})
    public ResponseEntity<String> saveAuthorizationCode(HttpServletRequest request){
        String code = request.getParameter("code");
        if(code==null)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        saveToken(code);
        return new ResponseEntity<>(code, HttpStatus.OK);
    }
    private void saveToken(String code){
        try {
            GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
            flow.createAndStoreCredential(response, USER_IDENTIFIER_KEY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping(value = {"/create"})
    public ResponseEntity<?> createFile(HttpServletResponse response, @RequestBody FileDriveDTO fileDrive){
        try {
            Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);

            Drive drive = new Drive.Builder(HTTP_TRANSPORT,JSON_FACTORY,cred).setApplicationName(APP_NAME).build();

            File file = new File();
            file.setName(fileDrive.getName());

            FileContent content = new FileContent(fileDrive.getType(), new java.io.File(fileDrive.getFile()));
            File uploaded = drive.files().create(file, content).setFields("id").execute();

            String fileReference = String.format("{fileID: %s}", uploaded.getId());
            response.getWriter().write(fileReference);

            return new ResponseEntity<>(fileReference, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping(value = {"/list"}, produces = "application/json")
    public ResponseEntity<List<FileDriveDTO>> getFiles(HttpServletResponse response){
        try {
            Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
            Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred).setApplicationName(APP_NAME).build();

            FileList files = drive.files().list().setFields("files(id,name,webContentLink,thumbnailLink,modifiedTime, createdTime, size, mimeType,owners)").execute();

            List<FileDriveDTO> fileDriveDTOS = new ArrayList<>();

            files.getFiles().forEach(file -> {
                FileDriveDTO item = new FileDriveDTO();
                item.setId(file.getId());
                item.setName(file.getName());
                item.setFile(file.getWebContentLink());
                item.setThumbnailLink(file.getThumbnailLink());
                item.setLastMod(file.getModifiedTime().toString());
                item.setCreatedDate(file.getCreatedTime().toString());
                item.setSize(String.valueOf(file.getSize()));
                item.setType(file.getMimeType());
                item.setOwners(file.getOwners());
                fileDriveDTOS.add(item);
            });

            return new ResponseEntity<>(fileDriveDTOS, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping(value = {"/get/{id}"})
    public ResponseEntity<Drive.Files.Get> getFileById(HttpServletResponse response, @PathVariable String id){
        try {
            Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
            Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred).setApplicationName(APP_NAME).build();

            return new ResponseEntity<>(drive.files().get(id), HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping(value = {"/home"}, produces = "application/json")
    public ResponseEntity<CredentialsDTO> greeting(){
        try {
            Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
            Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred).setApplicationName(APP_NAME).build();

            About about = drive.about().get().setFields("user(displayName,emailAddress,photoLink)").execute();
            About quota = drive.about().get().setFields("storageQuota(usage,limit)").execute();

            CredentialsDTO credentialsDTO = new CredentialsDTO();
            credentialsDTO.setEmail(about.getUser().getEmailAddress());
            credentialsDTO.setName(about.getUser().getDisplayName());
            credentialsDTO.setThumbnailLink(about.getUser().getPhotoLink());
            credentialsDTO.setUseSpace(quota.getStorageQuota().getUsage());
            credentialsDTO.setTotalSpace(quota.getStorageQuota().getLimit());
            return new ResponseEntity<>(credentialsDTO, HttpStatus.ACCEPTED);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}