package psu.signlinguamobile.api.apiresponse;

import java.util.List;

import psu.signlinguamobile.models.CertificationDocument;
import psu.signlinguamobile.models.EducationDocument;
import psu.signlinguamobile.models.SkillAndAccessibility;
import psu.signlinguamobile.models.WorkDocument;

/**
 * This model can be reused by any tutor profile contexts.
 */
public class TutorProfileDetailsResponse
{

    //=================================================
    // <editor-fold desc="USER ACCOUNT">
    //=================================================
    private String bio;
    private String username;
    private String email;
    private String photo;

    public String getBio()
    {
        return bio;
    }

    public void setBio(String bio)
    {
        this.bio = bio;
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

    public String getPhoto()
    {
        return photo;
    }

    public void setPhoto(String photo)
    {
        this.photo = photo;
    }
    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="GENERAL INFORMATION">
    //=================================================

    public String getFirstname()
    {
        return firstname;
    }

    public void setFirstname(String firstname)
    {
        this.firstname = firstname;
    }

    public String getLastname()
    {
        return lastname;
    }

    public void setLastname(String lastname)
    {
        this.lastname = lastname;
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

    public String getAbout()
    {
        return about;
    }

    public void setAbout(String about)
    {
        this.about = about;
    }

    private String firstname;
    private String lastname;
    private String contact;
    private String address;
    private String about;

    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="DOCUMENTARY PROOFS">
    //=================================================

    private List<CertificationDocument> certificationDocuments;
    private List<EducationDocument> educationDocuments;
    private List<WorkDocument> workExpDocuments;

    public SkillAndAccessibility getSkillAndAccessibility()
    {
        return skillAndAccessibility;
    }

    public void setSkillAndAccessibility(SkillAndAccessibility skillAndAccessibility)
    {
        this.skillAndAccessibility = skillAndAccessibility;
    }

    private SkillAndAccessibility skillAndAccessibility;

    public List<WorkDocument> getWorkExpDocuments()
    {
        return workExpDocuments;
    }

    public void setWorkExpDocuments(List<WorkDocument> workExpDocuments)
    {
        this.workExpDocuments = workExpDocuments;
    }

    public List<EducationDocument> getEducationDocuments()
    {
        return educationDocuments;
    }

    public void setEducationDocuments(List<EducationDocument> educationDocuments)
    {
        this.educationDocuments = educationDocuments;
    }

    public List<CertificationDocument> getCertificationDocuments()
    {
        return certificationDocuments;
    }

    public void setCertificationDocuments(List<CertificationDocument> certificationDocuments)
    {
        this.certificationDocuments = certificationDocuments;
    }

    //=================================================
    // </editor-fold>
    //=================================================
}
