/*
        USING MTA-API FOR ALL MTA SUBWAY DATA
        MTA-API AUTHOR: mimouncadosch
        GITHUB PAGE: https://github.com/mimouncadosch/MTA-API

        USING AndroidSlidingUpPanel LIBRARY FOR SLIDING
        AndroidSlidingUpPanel AUTHOR: UMANO
        GITHUB PAGE: https://github.com/umano/AndroidSlidingUpPanel

        USING GOOGLE MAPS API FOR ONLY DISPLAYING THE EXACT LOCATION OF THE TRAIN STATION

        App design, implementation:
        BY; Haoran He
        Emp ID: 23528972
        CSC 221

 */

package com.ccny.haoran.mtatime;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, TimePickerDialog.OnTimeSetListener{
    private String inputStationName;
    private static final DateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private GoogleMap mMap;
    private static  String[] stationDATABASE;
    private HashMap<String, String> table = new HashMap<String,String>();
    private String id;
    TimePickerDialog tpk;
    //private Button update;
    CountDownTimer Count;


    public void processTime(String[] all, String depSet) throws ParseException {       // depends on current time, get 10 future arrival times

        String[] result = new String[5];
        Date time = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String currentTime = sdf.format(time);
        Date cur = sdf.parse(currentTime);
        if(depSet!=null) {
            cur = sdf.parse(depSet);
        }
            int i = 0;

            for (; i < all.length; i++) {
                Date check = sdf.parse(all[i]);
                Date checkPlus = sdf.parse(all[i + 1]);
                if (cur.after(check) && cur.before(checkPlus)) {   //！！！待修： 如果当前时间接近凌晨0点，会出array out of bound错误
                    if (cur.equals(check)) {
                        result = Arrays.copyOfRange(all, i, i + 4);
                        break;
                    } else {
                        result = Arrays.copyOfRange(all, i + 1, i + 5);
                        break;
                    }
                }
            }

            setList(result);  //return 5 arrival times
          // return result;
    }

    public String[] getTrainTime(String id, String cal) throws IOException, JSONException, ParseException {     //use api to get all the arrival time for a station, input is station ID, output is time table
        // no need to verify station name because mapAction() guarantees it
        this.id=id;
        ArrivalTime ar = new ArrivalTime("http://mtaapi.herokuapp.com/api?id="+id);
        String[] timeTable = ar.getArrival();

        processTime(timeTable,cal);


        return timeTable;
    }

    public void setList(String[] times) throws ParseException {        //add times as list to the slideup panel
        String[] uptown = new String[3];
        uptown[0] = "D - Bronx";
        uptown[1]=times[0];
        uptown[2]=times[2];

        String[] downtown = new String[3];
        downtown[0] = "D - Coney Island";
        downtown[1]=times[1];
        downtown[2]=times[3];

        ArrayAdapter<String> arr = new ArrayAdapter<String>(this,R.layout.activity_list,uptown);
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(arr);

        ArrayAdapter<String> arr2 = new ArrayAdapter<String>(this,R.layout.activity_list,downtown);
        ListView list2 = (ListView) findViewById(R.id.list2);
        list2.setAdapter(arr2);

        depButton(true);
        counter(times);
    }

    public void mapAction(String name) throws IOException, JSONException, ParseException {      //location loop up then pin on map, then zoom in
        String stationID;

        if (table.get(name) != null) { //ensure a valid station name
            stationID = table.get(name);
            Location lo = new Location("http://mtaapi.herokuapp.com/stop?id=" + stationID);    //location look up
            LatLng stationLO = new LatLng(lo.getLat(), lo.getLon());
            String title = lo.getName();
            mMap.addMarker(new MarkerOptions().position(stationLO).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(stationLO));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18f));

            // now call for getting the train time
            try {
               getTrainTime(stationID,null);        //time table, not been processed.
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
    }

        public void counter(final String[] str) throws ParseException {     //next train arrival time counter, auto update after 20 seconds when "Train Arrvies" is displayed


            Date time = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String currentTime = sdf.format(time);
            Date cur = sdf.parse(currentTime);
            Date arr = sdf.parse(str[0]);
            long elasped = arr.getTime() - cur.getTime();
            final TextView textic = (TextView) findViewById(R.id.trainTime);
            if(Count!=null) Count.cancel();
            Count = new CountDownTimer(elasped, 60000) {
                public void onTick(long millisUntilFinished) {
                    if( millisUntilFinished / 60000 > 0)
                        textic.setText("Next train arrives: " + millisUntilFinished / 60000 + " minute(s)");
                    else
                        textic.setText("Next train arrives: less than 1 minute");
                }
                public void onFinish() {
                    textic.setText("Train Arrives");

                    CountDownTimer newCount = new CountDownTimer(20000,1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            try {
                                getTrainTime(id,null);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }; newCount.start();


                }
            };Count.start();
        }


    public void searchBar(){        //search bar
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, stationDATABASE);
        final AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.result);
        textView.setAdapter(adapter);
        textView.setThreshold(1);

        textView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
                if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(textView.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);   //hide keyboard after enter is pressed
                    if(textView.getText().toString()!="") inputStationName = textView.getText().toString();     //store the current station name to string
                    try {
                        mapAction(inputStationName);        //look up stationID and to put marker on there
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng newYork = new LatLng(40.694016, -73.968709); //set default location at new york
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newYork));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 11.0f ) );  //make it zoom in
    }

    public void depButton (boolean show){           // set depart time button
        final Button depart = (Button)findViewById(R.id.depart);
        if(!show) depart.setVisibility(View.GONE);
        else{
            depart.setVisibility(View.VISIBLE);

            depart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar depat = Calendar.getInstance();
                    tpk = TimePickerDialog.newInstance(MainActivity.this,depat.get(Calendar.HOUR_OF_DAY), depat.get(Calendar.MINUTE),false);
                    tpk.setTitle("Set depart time");

                    tpk.show(getFragmentManager(),"TimePicker");


                }
            });
        }
    }


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try {
                Stations data = new Stations("http://mtaapi.herokuapp.com/stations");
                stationDATABASE=data.getName();
                table=data.getMap();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            setContentView(R.layout.activity_main);
            depButton(false);
            searchBar();

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        Toast.makeText(this, "Depart time set",Toast.LENGTH_SHORT).show();
        String hr, m, s, time;
        if(hourOfDay<10){
            hr="0"+ Integer.toString(hourOfDay);
        } else hr = Integer.toString(hourOfDay);

        if(minute<10){
            m="0"+ Integer.toString(minute);
        }else m = Integer.toString(minute);

        s="0"+ Integer.toString(0);
        time = hr+":"+m+":"+s;


        try {
            getTrainTime(id,time);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }



    }
}

