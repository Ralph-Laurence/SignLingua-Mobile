package psu.signlinguamobile.models;

public class User
{
    public enum Role {
        ADMIN(0, "Admin"),
        TUTOR(1, "Tutor"),
        LEARNER(2, "Learner"),
        PENDING(3, "Pending");

        private final int value;
        private final String displayName;

        Role(int value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public int getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        // Utility method to convert from int to Role enum
        public static Role fromInt(int value) {
            for (Role role : Role.values()) {
                if (role.getValue() == value) {
                    return role;
                }
            }
            return null; // or throw an exception if an unknown role is received
        }
    }

    public enum Disability
    {
        NO_IMPAIRMENTS(0, "No Impairments"),
        DEAF(1, "Deaf or Hard of Hearing"),
        MUTE(2, "Non-Verbal"),
        DEAF_MUTE(3, "Deaf and Non-Verbal");

        private final int value;
        private final String displayName;

        Disability(int value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public int getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        // Utility method to convert from int to Role enum
        public static Disability fromInt(int value)
        {
            for (Disability disability : Disability.values())
            {
                if (disability.getValue() == value)
                    return disability;
            }
            return null; // or throw an exception if an unknown role is received
        }
    }

    private String id;
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private int role; //  You can also define an enum if needed
    private String contact;
    private String address;
    private String photo; // This will hold the photo URL or relative path, so process as needed
    private int isVerified; // 0 or 1

    public String getId() {
        return id;
    }
    public String getFirstname() {
        return firstname;
    }
    public String getLastname() {
        return lastname;
    }
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public int getRole() {
        return role;
    }

    // Helper method to get the Role enum
    public Role getRoleEnum() {
        return Role.fromInt(role);
    }

    // Helper method to get the display string
    public String getRoleDisplay() {
        Role r = getRoleEnum();
        return (r != null) ? r.getDisplayName() : "Unknown";
    }

    public String getContact() {
        return contact;
    }
    public String getAddress() {
        return address;
    }
    public String getPhoto() {
        return photo;
    }
    public int getIsVerified() {
        return isVerified;
    }
}
