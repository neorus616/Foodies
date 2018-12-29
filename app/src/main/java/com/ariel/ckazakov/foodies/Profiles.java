package com.ariel.ckazakov.foodies;

public class Profiles {
    public String profileimage, firstname, lastname;

    public Profiles() {
    }

    public Profiles(String profileiamge, String firstname, String lastname) {
        this.profileimage = profileiamge;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}
