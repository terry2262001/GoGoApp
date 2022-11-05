package com.example.instagram.Model;

public class Notification {
    private String userid;
    private String text;
    private String postid;
    private boolean isIspost;

    public Notification(String userid, String text, String postid, boolean isIspost) {
        this.userid = userid;
        this.text = text;
        this.postid = postid;
        this.isIspost = isIspost;
    }

    public Notification() {
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public boolean isIspost() {
        return isIspost;
    }

    public void setIspost(boolean ispost) {
        isIspost = ispost;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "userid='" + userid + '\'' +
                ", text='" + text + '\'' +
                ", postid='" + postid + '\'' +
                ", isIspost=" + isIspost +
                '}';
    }
}
