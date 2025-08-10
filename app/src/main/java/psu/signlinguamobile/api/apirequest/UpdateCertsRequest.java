package psu.signlinguamobile.api.apirequest;

public class UpdateCertsRequest
{
     private String from;
     private String certification;
     private String description;

    public UpdateCertsRequest(String from, String certification, String description)
    {
        this.from = from;
        this.certification = certification;
        this.description = description;
    }
}