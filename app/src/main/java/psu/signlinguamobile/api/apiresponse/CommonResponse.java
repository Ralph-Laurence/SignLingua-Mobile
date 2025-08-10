package psu.signlinguamobile.api.apiresponse;

public class CommonResponse<T>
{
    private T content; // <- Can be anything eg JSON data
    private String message;
    private int status;

    // Getters for token and user
    public int getStatus()
    {
        return status;
    }

    public String getMessage()
    {
        return message;
    }

    public T getContent()
    {
        return content;
    }

    public void setContent(T content)
    {
        this.content = content;
    }
}
