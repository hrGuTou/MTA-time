package com.ccny.haoran.mtatime;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Stations extends GetJson {
    private JSONObject j;
    private ArrayList<String> names;
    private HashMap<String,String> map=new HashMap<String, String>();

    Stations(String url) throws IOException, JSONException {
        super(url);
        j= super.getJson();
        int length = j.getJSONArray("result").length();
        names=new ArrayList<String>();

        for(int i=0; i<length; i++){        //store all the station names and their ID in the hash map
            JSONObject tmp = (JSONObject) j.getJSONArray("result").get(i);
            String name = (String) tmp.get("name");
            names.add(name.toString());
            String id = (String)tmp.get("id");
            map.put(name,id);
        }

    }



    public String[] getName(){
        String[] result= new String[names.size()];
        result = names.toArray(result);
        return result;
    }
    public HashMap<String, String> getMap(){
        return map;
    }
}
