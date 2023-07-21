package org.beru.drive.domain;

public class CredentialsDTO {
    private String name;
    private String email;
    private String thumbnailLink;
    private double useSpace;
    private double totalSpace;

    public double getUseSpace() {
        return useSpace;
    }

    public void setUseSpace(double useSpace) {
        this.useSpace = useSpace;
    }

    public double getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(double totalSpace) {
        this.totalSpace = totalSpace;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnailLink() {
        return thumbnailLink;
    }

    public void setThumbnailLink(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }
}
