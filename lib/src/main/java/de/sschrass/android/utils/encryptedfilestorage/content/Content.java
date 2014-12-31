package de.sschrass.android.utils.encryptedfilestorage.content;

public class Content {
    private long id;
    private String contentId;
    private String availabilityEnd;

    /** C'tor
     *
     * */
    public Content(long id, String contentId, String availabilityEnd) {
        this.id = id;
        this.contentId = contentId;
        this.availabilityEnd = availabilityEnd;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }
    public String getAvailabilityEnd() { return availabilityEnd; }
    public void setAvailabilityEnd(String availabilityEnd) { this.availabilityEnd = availabilityEnd; }
}
