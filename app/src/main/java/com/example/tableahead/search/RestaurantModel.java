package com.example.tableahead.search;

public class RestaurantModel {
    private final String id;
    private String name = "";
    private int popularity = -1;
    private float distance = -1f;
    private String imageLink = "";
    private long reviewCount;
    private Double reviewRating;


    public RestaurantModel(String id) {
        this.id = id;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }
    public String getImageLink() {
        return imageLink;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public int getPopularity() {
        return popularity;
    }

    public float getDistance() {
        return distance;
    }

    public long getReviewCount() {return reviewCount;}
    public void setReviewCount(long reviewCount) {this.reviewCount = reviewCount;}

    public Double getReviewRating() {return reviewRating;}
    public void setReviewRating(Double reviewRating) {this.reviewRating = reviewRating;}


    /** @noinspection NullableProblems*/
    @Override
    public String toString() {
        return "RestaurantModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", popularity=" + popularity +
                ", distance=" + distance +
                ", reviewCount=" + reviewCount +
                ", reviewRating=" + reviewRating +
                '}';
    }
}