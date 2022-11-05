package com.example.instagram.Model;

public class User {
    String imageURL;
    String bio;
    String fullname;
    String id;
    String username;

    public User() {
    }

    public User(String imageURL, String bio, String fullName, String id, String username) {
        this.imageURL = imageURL;
        this.bio = bio;
        this.fullname = fullName;
        this.id = id;
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getFullName() {
        return fullname;
    }

    public void setFullName(String fullName) {
        this.fullname = fullName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "imageURL='" + imageURL + '\'' +
                ", bio='" + bio + '\'' +
                ", fullName='" + fullname + '\'' +
                ", id='" + id + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
