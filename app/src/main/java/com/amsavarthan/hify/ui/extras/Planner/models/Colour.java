package com.amsavarthan.hify.ui.extras.Planner.models;

public class Colour {

    private int colour;
    private String dateAndTime;

    public Colour(int colour, String dateAndTime) {
        this.colour = colour;
        this.dateAndTime = dateAndTime;
    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public int getColour() {
        return colour;
    }
}