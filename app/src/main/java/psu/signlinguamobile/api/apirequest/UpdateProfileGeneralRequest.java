package psu.signlinguamobile.api.apirequest;

public class UpdateProfileGeneralRequest
{
    private String firstname;
    private String lastname;
    private String contact;
    private String address;
    private String about;

    public UpdateProfileGeneralRequest
            (String firstname, String lastname, String contact, String address, String about)
    {
        this.firstname  = firstname;
        this.lastname   = lastname;
        this.contact    = contact;
        this.address    = address;
        this.about      = about;
    }
}
