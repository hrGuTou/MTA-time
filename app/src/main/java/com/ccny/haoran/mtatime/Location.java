package com.ccny.haoran.mtatime;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Location extends GetJson {
    private double lat;
    private double lon;
    private String name;


    Location(String url) throws IOException, JSONException {
        super(url);
        JSONObject j = super.getJson();
        lat = Double.parseDouble((String) j.getJSONObject("result").get("lat"));
        lon = Double.parseDouble((String) j.getJSONObject("result").get("lon"));
        name = (String)j.getJSONObject("result").get("name");
    }

    public double getLat(){return lat;}
    public double getLon(){return lon;}
    public String getName(){return name;}
}
