package com.ariel.ckazakov.models;

import java.util.ArrayList;

/**
 * This is a model class for Recipe
 */
public class Recipe {

    private String uid, time, date, recipeimage, recipe, fullname, profileimage, title;
    private int counter;
    private ArrayList<String> ingredients;

    /**
     * default constructor
     */
    public Recipe() {

    }

    /**
     * constructor
     *
     * @param uid          - unique identifier for a recipe
     * @param time         - time (HH:mm)
     * @param date         - date (dd-mm-YYYY)
     * @param recipeimage  - image of the recipe
     * @param recipe       - content of the recipe
     * @param fullname     - full name of the publisher
     * @param profileimage - profile image of the publisher
     * @param title        - title of the recipe
     * @param counter      - counter for sorting
     * @param ingredients  - ingredients of the recipe
     */
    public Recipe(String uid, String time, String date, String recipeimage, String recipe, String fullname, String profileimage, String title, int counter, ArrayList<String> ingredients) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.recipeimage = recipeimage;
        this.recipe = recipe;
        this.fullname = fullname;
        this.profileimage = profileimage;
        this.title = title;
        this.counter = counter;
        this.ingredients = ingredients;
    }

    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
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

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

}