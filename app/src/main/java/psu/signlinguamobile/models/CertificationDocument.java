package psu.signlinguamobile.models;

public class CertificationDocument
{
    private String from;
    private String certification;
    private String description;
    private String docId;

    public CertificationDocument(String from, String certification, String description, String docId)
    {
        this.from = from;
        this.certification = certification;
        this.description = description;
        this.docId = docId;
    }

    public String getDocId() { return docId; }

    public void setDocId(String docId) { this.docId = docId; }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getCertification()
    {
        return certification;
    }

    public void setCertification(String certification)
    {
        this.certification = certification;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
