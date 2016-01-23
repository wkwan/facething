package com.dualcnhq.opencv;

/**
 * Created by james on 16-01-23.
 */
public class Profile {
    private int id;
    private String name;
    private String twitterID;

    public Profile(int id, String name, String twitterID) {
        this.id = id;
        this.name = name;
        this.twitterID = twitterID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTwitterID() {
        return twitterID;
    }

    public void setTwitterID(String twitterID) {
        this.twitterID = twitterID;
    }
}
