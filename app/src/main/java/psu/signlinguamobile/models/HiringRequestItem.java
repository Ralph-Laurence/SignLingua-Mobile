package psu.signlinguamobile.models;

public class HiringRequestItem
{
    private String name;
    private String photo;
    private String username;
    private int disability;
    private String learnerId;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPhoto()
    {
        return photo;
    }

    public void setPhoto(String photo)
    {
        this.photo = photo;
    }

    public int getDisability()
    {
        return disability;
    }

    public void setDisability(int disability)
    {
        this.disability = disability;
    }

    public String getLearnerId()
    {
        return learnerId;
    }

    public void setLearnerId(String learnerId)
    {
        this.learnerId = learnerId;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
}
