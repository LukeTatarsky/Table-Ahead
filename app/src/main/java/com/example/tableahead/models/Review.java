package com.example.tableahead.models;

public class Review {
    private final String reviewerName;
    private String reviewerId;
    private final String reviewContent;
    private final Double rating;
    private String reviewId;

    public Review(String reviewerName, String reviewContent, Double rating) {
        this.reviewerName = reviewerName;
        this.reviewContent = reviewContent;
        this.rating = rating;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public Double getRating() {
        return rating;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    /** @noinspection NullableProblems*/
    @Override
    public String toString() {
        return "Review{" +
                "reviewerName='" + reviewerName + '\'' +
                ", reviewContent='" + reviewContent + '\'' +
                ", rating=" + rating +
                '}';
    }
}