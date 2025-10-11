package psu.signlinguamobile.models;

public class ChatMessagePayload
{
    private String message;
    private String fromSender;

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getFromSender()
    {
        return fromSender;
    }

    public void setFromSender(String fromSender)
    {
        this.fromSender = fromSender;
    }
}
