package psu.signlinguamobile.api.apirequest;

public class UpdateSkillsRequest
{
    private int disability;
    private String[] skills;

    public UpdateSkillsRequest(int disability, String[] skills)
    {
        this.disability = disability;
        this.skills     = skills;
    }
}