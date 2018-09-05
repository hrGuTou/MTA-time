package com.ccny.haoran.mtatime;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


public class ArrivalTime extends GetJson {
    String[] arrival;
    //@RequiresApi(api = Build.VERSION_CODES.O)
    ArrivalTime(String url) throws IOException, JSONException, ParseException {
        super(url);
        JSONObject j = super.getJson();
        int length = j.getJSONObject("result").getJSONArray("arrivals").length();
        arrival = new String[length];

        for(int i=0; i<length; i++){

            StringBuilder time= new StringBuilder(j.getJSONObject("result").getJSONArray("arrivals").get(i).toString());
            if(time.charAt(0)=='2' && time.charAt(1)=='5') {
                time.setCharAt(0,'0');
                time.setCharAt(1,'1');
            }
            if(time.charAt(0)=='2' && time.charAt(1)=='4') {
                time.setCharAt(0,'0');
                time.setCharAt(1,'0');
            }

            arrival[i] = time.toString();
        }

    }

    public String[] removeDup(String arr[]){
        int n = 1;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] != arr[i-1]) n++;
        }
        String[] res = new String[n];
        res[0] = arr[0];
        n = 1;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] != arr[i-1]) res[n++] = arr[i];
        }
        return res;

    }

    public String[] getArrival() {
        Arrays.sort(arrival);
        String[] arr = removeDup(arrival);
        return arr;
    }
}
