package psu.signlinguamobile.models;

import com.google.gson.annotations.SerializedName;

public class TotalIndividualRatings {

    @SerializedName("5")
    private int rating5;

    @SerializedName("4")
    private int rating4;

    @SerializedName("3")
    private int rating3;

    @SerializedName("2")
    private int rating2;

    @SerializedName("1")
    private int rating1;

    // Optionally provide getters and setters

    public int getRating5() {
        return rating5;
    }

    public void setRating5(int rating5) {
        this.rating5 = rating5;
    }

    public int getRating4() {
        return rating4;
    }

    public void setRating4(int rating4) {
        this.rating4 = rating4;
    }

    public int getRating3() {
        return rating3;
    }

    public void setRating3(int rating3) {
        this.rating3 = rating3;
    }

    public int getRating2() {
        return rating2;
    }

    public void setRating2(int rating2) {
        this.rating2 = rating2;
    }

    public int getRating1() {
        return rating1;
    }

    public void setRating1(int rating1) {
        this.rating1 = rating1;
    }
}
