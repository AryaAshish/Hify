package com.amsavarthan.hify.models;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class Notification {

    private String from, message;

    public Notification() {
    }


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public Notification( String from, String message) {

        this.from = from;
        this.message = message;
    }
}

