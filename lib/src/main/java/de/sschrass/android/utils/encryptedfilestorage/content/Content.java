package de.sschrass.android.utils.encryptedfilestorage.content;

public class Content {
    private String id;
    private String contentId;
    private String availabilityEnd;

    public Content(String id, String contentId, String availabilityEnd) {
        this.id = id;
        this.contentId = contentId;
        this.availabilityEnd = availabilityEnd;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }
    public String getAvailabilityEnd() { return availabilityEnd; }
    public void setAvailabilityEnd(String availabilityEnd) { this.availabilityEnd = availabilityEnd; }
}
