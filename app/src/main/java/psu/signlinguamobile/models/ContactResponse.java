package psu.signlinguamobile.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ContactResponse
{
    @SerializedName("contactsList")
    private List<ContactItem> contactsList;

    @SerializedName("userInformation")
    private User userInformation;

    public User getUserInformation()
    {
        return userInformation;
    }
}
