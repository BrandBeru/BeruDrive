package org.beru.drive.domain;

import com.google.api.client.util.DateTime;

import java.io.Serializable;

public class FileDriveDTO implements Serializable {
    private String name;
    private String file;
    private String type;
    private String id;

    private String thumbnailLink;
    private String lastMod;

    public FileDriveDTO() {
    }
    public FileDriveDTO(String name, String file, String type) {
        this.name = name;
        this.file = file;
        this.type = type;
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
