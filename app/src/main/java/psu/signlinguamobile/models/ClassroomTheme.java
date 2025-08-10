package psu.signlinguamobile.models;

public class ClassroomTheme {
    private String icon;
    private String bgcolor;

    // Constructor
    public ClassroomTheme(String icon, String bgcolor) {
        this.icon = icon;
        this.bgcolor = bgcolor;
    }

    // Getters
    public String getIcon() {
        return icon;
    }

    public String getBgcolor() {
        return bgcolor;
    }

    // Setters
    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setBgcolor(String bgcolor) {
        this.bgcolor = bgcolor;
    }
}

