package com.example.messengerapp;

public class Contacts {

    String username;
    String image;

    public Contacts()
    {

    }

    public Contacts(String username, String image)
    {
        this.username = username;
        this.image = image;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }
}
