package com.example.e_float;

import java.util.ArrayList;

public class Device {
    private String _name;
    private int _strength;

    public Device(String pName, int pStrength) {
        _name = pName;
        _strength = pStrength;
    }

    public String getName() { return _name; }

    public int getStrength() { return _strength; }

    //TODO: include a method to allow device to refetch its own information
}
