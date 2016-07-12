package com.example.aditya.checkout.model;

public class ColorList
{
    private String color;
    private static String name;

    public ColorList(String color, String name) {
        this.color = color;
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public String getName() {
        return name;
    }
}
