package com.example.instagram.Model;

public class Post {
    private String postid;
    private String postimage;
    private String description;
    private String publisher;





    public Post(String postId, String postImage, String description, String publisher) {
        this.postid = postId;
        this.postimage = postImage;
        this.description = description;
        this.publisher = publisher;
    }

    public Post() {
    }

    public String getPostId() {
        return postid;
    }

    public void setPostId(String postId) {
        this.postid = postId;
    }

    public String getPostImage() {
        return postimage;
    }

    public void setPostImage(String postImage) {
        this.postimage = postImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
