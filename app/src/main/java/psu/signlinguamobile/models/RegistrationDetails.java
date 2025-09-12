package psu.signlinguamobile.models;

// import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RegistrationDetails
{
    private int role;
    private String firstname;
    private String lastname;
    private String contact;
    private String address;
    private String username;
    private String email;
//    @SerializedName("password")
//    private String newPassword;
    private String password;
    private String confirm;
    private int impairment;

    public static final int LEARNER_REGISTRATION = 2;
    public static final int  TUTOR_REGISTRATION = 1;

    public RegistrationDetails(int role, String firstname, String lastname, int impairment, String contact, String address, String username, String email, String password, String confirm)
    {
        this.role               = role;
        this.firstname          = firstname;
        this.lastname           = lastname;
        this.impairment         = impairment;
        this.contact            = contact;
        this.address            = address;
        this.username           = username;
        this.email              = email;
        this.password           = password;
        this.confirm            = confirm;
    }

    public static Map<String, RequestBody> toRequestBodyMap(RegistrationDetails details)
    {
        Map<String, RequestBody> map = new HashMap<>();

        String[] fields = new String[] {
            "firstname",
            "lastname",
            "contact",
            "address",
            "username",
            "email",
            "newPassword",
            "confirmPassword",
        };

        for (String field : fields)
        {
            map.put(field, RequestBody.create(details.getFirstname(), MediaType.parse("text/plain")));
        }

        return map;
    }

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

    public void setImpairment(int impairment)
    {
        this.impairment = impairment;
    }

    public int getImpairment()
    {
        return impairment;
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

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String newPassword)
    {
        this.password = newPassword;
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirmPassword)
    {
        this.confirm = confirmPassword;
    }

    public int getRole()
    {
        return role;
    }

    public void setRole(int role)
    {
        this.role = role;
    }
}
