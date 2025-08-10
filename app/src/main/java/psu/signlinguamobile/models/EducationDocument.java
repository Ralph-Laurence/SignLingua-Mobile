package psu.signlinguamobile.models;

public class EducationDocument
{
    private String docId;
    private String duration;
    private int from;
    private int to;
    private String institution;
    private String degree;

    /**
     * Use this constructor when performing a get request
     * @param institution - The name of the educational institution
     * @param degree - The degree title
     * @param duration - The starting year with the end year (eg formatted as '2025-2026')
     * @param docId - The id of the entry
     */
    public EducationDocument(String institution, String degree, String duration, String docId)
    {
        this.docId = docId;
        this.duration = duration;
        this.institution = institution;
        this.degree = degree;
    }

    /**
     * Use this constructor when performing an update request
     * @param institution - The name of the educational institution
     * @param degree - The degree title
     * @param from - The starting year
     * @param to - The end year
     */
    public EducationDocument(String institution, String degree, int from, int to)
    {
        this.from = from;
        this.to = to;
        this.institution = institution;
        this.degree = degree;
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

    public String getInstitution()
    {
        return institution;
    }

    public void setInstitution(String institution)
    {
        this.institution = institution;
    }

    public String getDegree()
    {
        return degree;
    }

    public void setDegree(String degree)
    {
        this.degree = degree;
    }

}