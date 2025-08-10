package psu.signlinguamobile.models;

import java.util.List;

public class TutorDetails
{
    public String getHashedId()
    {
        return hashedId;
    }

    public void setHashedId(String hashedId)
    {
        this.hashedId = hashedId;
    }

    public String getFirstname()
    {
        return firstname;
    }

    public void setFirstname(String firstname)
    {
        this.firstname = firstname;
    }

    public String getPossessiveName()
    {
        return possessiveName;
    }

    public void setPossessiveName(String possessiveName)
    {
        this.possessiveName = possessiveName;
    }

    public String getFullname()
    {
        return fullname;
    }

    public void setFullname(String fullname)
    {
        this.fullname = fullname;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public int getDisability()
    {
        return disability;
    }

    public void setDisability(int disability)
    {
        this.disability = disability;
    }

    public String getContact()
    {
        return contact;
    }

    public void setContact(String contact)
    {
        this.contact = contact;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public List<Work> getWork()
    {
        return work;
    }

    public void setWork(List<Work> work)
    {
        this.work = work;
    }

    public List<Education> getEducation()
    {
        return education;
    }

    public void setEducation(List<Education> education)
    {
        this.education = education;
    }

    public List<CertificationDocument> getCerts()
    {
        return certs;
    }

    public void setCerts(List<CertificationDocument> certs)
    {
        this.certs = certs;
    }

    public String getBio()
    {
        return bio;
    }

    public void setBio(String bio)
    {
        this.bio = bio;
    }

    public String getAbout()
    {
        return about;
    }

    public void setAbout(String about)
    {
        this.about = about;
    }

    public List<String> getSkills()
    {
        return skills;
    }

    public void setSkills(List<String> skills)
    {
        this.skills = skills;
    }

    public String getPhoto()
    {
        return photo;
    }

    public void setPhoto(String photo)
    {
        this.photo = photo;
    }

    public int getHireStatus()
    {
        return hireStatus;
    }

    public void setHireStatus(int hireStatus)
    {
        this.hireStatus = hireStatus;
    }

    public double getAverageRating()
    {
        return averageRating;
    }

    public void setAverageRating(double averageRating)
    {
        this.averageRating = averageRating;
    }

    public int getTotalReviews()
    {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews)
    {
        this.totalReviews = totalReviews;
    }

    public List<RatingsAndReviews> getRatingsAndReviews()
    {
        return ratingsAndReviews;
    }

    public void setRatingsAndReviews(List<RatingsAndReviews> ratingsAndReviews)
    {
        this.ratingsAndReviews = ratingsAndReviews;
    }

    public TotalIndividualRatings getTotalIndividualRatings()
    {
        return totalIndividualRatings;
    }

    public void setTotalIndividualRatings(TotalIndividualRatings totalIndividualRatings)
    {
        this.totalIndividualRatings = totalIndividualRatings;
    }

    public int getHighestIndividualRating()
    {
        return highestIndividualRating;
    }

    public void setHighestIndividualRating(int highestIndividualRating)
    {
        this.highestIndividualRating = highestIndividualRating;
    }

    private String hashedId;
    private String firstname;
    private String possessiveName;
    private String fullname;
    private String username;
    private String email;
    private int disability;
    private String contact;
    private String address;
    private List<Work> work;
    private List<Education> education;
    private List<CertificationDocument> certs;

    private String bio;
    private String about;
    private List<String> skills;
    private String photo;
    private int hireStatus;
    private double averageRating;
    private int totalReviews;
    private List<RatingsAndReviews> ratingsAndReviews;
    private TotalIndividualRatings totalIndividualRatings;
    private int highestIndividualRating;
}
