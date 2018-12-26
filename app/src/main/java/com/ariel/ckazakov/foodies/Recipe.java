package com.ariel.ckazakov.foodies;

public class Recipe {

    public String uid;
    public String time;
    public String date;
    public String postImage;
    public String recipe;
    public String fullname;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Recipe(String uid, String time, String date, String postImage, String recipe, String fullname) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.postImage = postImage;
        this.recipe = recipe;
        this.fullname = fullname;
    }
}