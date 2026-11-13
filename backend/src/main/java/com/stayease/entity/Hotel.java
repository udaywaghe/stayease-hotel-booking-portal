package com.stayease.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="hotels",indexes=@Index(name="idx_hotels_location_area",columnList="location,area"))
public class Hotel {
    @Id @Column(length=40) private String id;
    @Column(nullable=false,length=160) private String name;
    @Column(nullable=false,length=180) private String location;
    @Column(nullable=false,length=80) private String area;
    @Column(nullable=false,length=2000) private String description;
    @Column(nullable=false,length=500) private String imageUrl;
    @Column(nullable=false) private double rating;
    @Column(nullable=false) private int reviews;
    @Column(nullable=false,length=80) private String badge;
    @JdbcTypeCode(SqlTypes.JSON) @Column(nullable=false,columnDefinition="json")
    private List<String> amenities = new ArrayList<>();
    public Hotel() {}
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getLocation(){return location;} public void setLocation(String v){location=v;}
    public String getArea(){return area;} public void setArea(String v){area=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
    public double getRating(){return rating;} public void setRating(double v){rating=v;}
    public int getReviews(){return reviews;} public void setReviews(int v){reviews=v;}
    public String getBadge(){return badge;} public void setBadge(String v){badge=v;}
    public List<String> getAmenities(){return amenities;} public void setAmenities(List<String> v){amenities=v;}
}
