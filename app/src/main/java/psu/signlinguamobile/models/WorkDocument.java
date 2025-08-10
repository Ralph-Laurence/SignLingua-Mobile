package psu.signlinguamobile.models;

public class WorkDocument
{
    private String docId;
    private String duration;
    private int from;
    private int to;
    private String company;
    private String role;

    /**
     * Use this constructor when performing a get request
     * @param company - The name of the company you worked for
     * @param role - The job title
     * @param duration - The starting year with the end year (eg formatted as '2025-2026')
     * @param docId - The id of the entry
     */
    public WorkDocument(String company, String role, String duration, String docId)
    {
        this.docId = docId;
        this.duration = duration;
        this.company = company;
        this.role = role;
    }

    /**
     * Use this constructor when performing an update request
     * @param company - The name of the company you worked for
     * @param role - The job title
     * @param from - The starting year
     * @param to - The end year
     */
    public WorkDocument(String company, String role, int from, int to)
    {
        this.from = from;
        this.to = to;
        this.company = company;
        this.role = role;
    }

    public String getDocId()
    {
        return docId;
    }

    public void setDocId(String docId)
    {
        this.docId = docId;
    }

    public String getDuration()
    {
        return duration;
    }

    public void setDuration(String duration)
    {
        this.duration = duration;
    }

    public int getFrom()
    {
        return from;
    }

    public void setFrom(int from)
    {
        this.from = from;
    }

    public int getTo()
    {
        return to;
    }

    public void setTo(int to)
    {
        this.to = to;
    }

    public String getCompany()
    {
        return company;
    }

    public void setCompany(String company)
    {
        this.company = company;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }
}