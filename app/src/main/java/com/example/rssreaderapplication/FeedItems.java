package com.example.rssreaderapplication;

public class FeedItems {

    String title;
    String description;

    public FeedItems(){}

    public FeedItems(String title, String description)
    {
        this.title = title;
        this.description = description;
    }


    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        description = description;
    }
}
