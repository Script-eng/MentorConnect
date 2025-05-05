package hu.nje.mentorconnect.models;

public class Document {


    private String id;
    private String title;

    private String description;
    private String downloadUrl;

    public Document(String id, String title, String description, String downloadUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.downloadUrl = downloadUrl;
    }

    // Constructor


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


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
