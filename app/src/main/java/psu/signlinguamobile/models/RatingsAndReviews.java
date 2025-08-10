package psu.signlinguamobile.models;

public class RatingsAndReviews
{
    private int rating;
    private String review;
    private String reviewDate;
    private String learnerName;

    public int getRating()
    {
        return rating;
    }

    public void setRating(int rating)
    {
        this.rating = rating;
    }

    public String getReview()
    {
        return review;
    }

    public void setReview(String review)
    {
        this.review = review;
    }

    public String getReviewDate()
    {
        return reviewDate;
    }

    public void setReviewDate(String reviewDate)
    {
        this.reviewDate = reviewDate;
    }

    public String getLearnerName()
    {
        return learnerName;
    }

    public void setLearnerName(String learnerName)
    {
        this.learnerName = learnerName;
    }

    public String getLearnerPhoto()
    {
        return learnerPhoto;
    }

    public void setLearnerPhoto(String learnerPhoto)
    {
        this.learnerPhoto = learnerPhoto;
    }

    private String learnerPhoto;
}
