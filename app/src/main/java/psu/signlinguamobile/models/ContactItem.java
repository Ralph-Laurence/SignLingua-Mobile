package psu.signlinguamobile.models;

import com.google.gson.annotations.SerializedName;

public class ContactItem
{
    @SerializedName("name")
    private String name;

    @SerializedName("photo")
    private String photo;

    @SerializedName("id")
    private String id;

    @SerializedName("last_message_body")
    private String lastMessageBody;

    @SerializedName("last_message_time")
    private String lastMessageTime;
}
