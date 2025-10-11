package psu.signlinguamobile.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChatConvResponse
{
    @SerializedName("messages")
    private List<ConvItem> messages;

    @SerializedName("currentUser")
    private String currentUser;

    @SerializedName("authToken")
    private String authToken;

    @SerializedName("contactName")
    private String contactName;

    @SerializedName("contactPhoto")
    private String contactPhoto;

    @SerializedName("contactId")
    private String contactId;

    @SerializedName("senderId")
    private String senderId;

    public String getAuthToken() {
        return authToken;
    }

    public String getSenderId()
    {
        return senderId;
    }
}
