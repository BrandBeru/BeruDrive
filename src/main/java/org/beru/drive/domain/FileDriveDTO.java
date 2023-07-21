package org.beru.drive.domain;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.User;

import java.io.Serializable;
import java.util.List;

public class FileDriveDTO implements Serializable {
    private String name;
    private String file;
    private String type;
    private String id;
    private String size;
    private String thumbnailLink;
    private String lastMod;
    private List<User> owners;
    private String createdDate;


    public FileDriveDTO() {
    }
    public FileDriveDTO(String name, String file, String type) {
        this.name = name;
        this.file = file;
        this.type = type;
    }

    public List<User> getOwners() {
        return owners;
    }

    public void setOwners(List<User> owners) {
        this.owners = owners;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getThumbnailLink() {
        return thumbnailLink;
    }

    public void setThumbnailLink(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastMod() {
        return lastMod;
    }

    public void setLastMod(String lastMod) {
        this.lastMod = lastMod;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
