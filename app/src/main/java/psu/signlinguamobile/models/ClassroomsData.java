package psu.signlinguamobile.models;

public class ClassroomsData
{
    private String classroomName;
    private String classroomTheme;
    private String classroomCode;
    private String classroomUid;
    private String classroomId;

    public String getClassroomName()
    {
        return classroomName;
    }

    public void setClassroomName(String classroomName)
    {
        this.classroomName = classroomName;
    }

    public String getClassroomTheme()
    {
        return classroomTheme;
    }

    public void setClassroomTheme(String classroomTheme)
    {
        this.classroomTheme = classroomTheme;
    }

    public String getClassroomCode()
    {
        return classroomCode;
    }

    public void setClassroomCode(String classroomCode)
    {
        this.classroomCode = classroomCode;
    }

    public String getClassroomUid()
    {
        return classroomUid;
    }

    public void setClassroomUid(String classroomUid)
    {
        this.classroomUid = classroomUid;
    }

    public String getClassroomId()
    {
        return classroomId;
    }

    public void setClassroomId(String classroomId)
    {
        this.classroomId = classroomId;
    }

    public String getTotalLearners()
    {
        return totalLearners;
    }

    public void setTotalLearners(String totalLearners)
    {
        this.totalLearners = totalLearners;
    }

    private String totalLearners;
}
