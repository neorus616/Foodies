package com.ariel.ckazakov.models;

public class Recipe {

    private String uid, time, date, recipeimage, recipe, fullname, profileimage, title;

    public Recipe() {

    }

    public Recipe(String uid, String time, String date, String recipeimage, String recipe, String fullname, String profileimage, String title) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.recipeimage = recipeimage;
        this.recipe = recipe;
        this.fullname = fullname;
        this.profileimage = profileimage;
        this.title = title;
    }

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

    public String getRecipeImage() {
        return recipeimage;
    }

    public void setRecipeImage(String recipeimage) {
        this.recipeimage = recipeimage;
    }

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public String getFullName() {
        return fullname;
    }

    public void setFullName(String fullname) {
        this.fullname = fullname;
    }

    public String getProfileImage() {
        return profileimage;
    }

    public void setProfileImage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}