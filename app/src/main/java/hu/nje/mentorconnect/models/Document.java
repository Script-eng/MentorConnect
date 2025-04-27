package hu.nje.mentorconnect.models;

public class Document {
    private String title;
    private String description;
    private String downloadUrl; // The direct URL to the document file

    // Constructor
    public Document(String title, String description, String downloadUrl) {
        this.title = title;
        this.description = description;
        this.downloadUrl = downloadUrl;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    // Setters (optional, if you need to modify documents after creation)
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
