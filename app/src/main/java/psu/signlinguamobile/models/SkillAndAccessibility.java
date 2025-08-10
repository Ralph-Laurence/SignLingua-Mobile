package psu.signlinguamobile.models;

import java.util.List;

public class SkillAndAccessibility
{
    public List<String> getSkills()
    {
        return skills;
    }

    public void setSkills(List<String> skills)
    {
        this.skills = skills;
    }

    public int getDisability()
    {
        return disability;
    }

    public void setDisability(int disability)
    {
        this.disability = disability;
    }

    private List<String> skills;
    private int disability;
}
