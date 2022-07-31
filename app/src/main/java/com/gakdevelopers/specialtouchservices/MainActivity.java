package com.gakdevelopers.specialtouchservices;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView txtTherapistName, txtSlot;

    ArrayList<String> listTherapists, listTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtTherapistName = (TextView) findViewById(R.id.txtTherapistName);
        txtSlot = (TextView) findViewById(R.id.txtSlot);

        /*String therapistName = getIntent().getStringExtra("therapistName");
        txtTherapistName.setText("" + therapistName);*/

        listTherapists = new ArrayList<>();
        listTime = new ArrayList<>();

        getScheduleDetails();

    }

    private void getScheduleDetails() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://script.google.com/macros/s/AKfycbxsT35DagGLz5JvJVqzKqRbVS-fqcq64rb3yxGixJbe8YbY_kdAzQZVueqLxIujGstXwQ/exec?action=getSchedule",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseItems(response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        //progressBarName.setVisibility(View.GONE);
                    }
                }
        );

        int socketTimeOut = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        stringRequest.setRetryPolicy(policy);

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void parseItems(String jsonResponse) {
        String currentTimeSlot = "";

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        try {
            JSONObject jObj = new JSONObject(jsonResponse);
            JSONArray jArray = jObj.getJSONArray("items");

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jo = jArray.getJSONObject(i);
                String therapists = jo.getString("Therapists");
                String dayOfWeek = jo.getString("DayOfWeek");
                String time = jo.getString("Time");
                String client = jo.getString("Client");

                String[] tokens = time.split(":");
                int minutesToMs = Integer.parseInt(tokens[1]) * 60000;
                int hoursToMs = Integer.parseInt(tokens[0]) * 3600000;
                long total = minutesToMs + hoursToMs;

                listTherapists.add(therapists);
                listTime.add("" + total);
            }

            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            String[] tokens = currentTime.split(":");
            int minutesToMs = Integer.parseInt(tokens[1]) * 60000;
            int hoursToMs = Integer.parseInt(tokens[0]) * 3600000;
            long currentTimeInMS = minutesToMs + hoursToMs;

            /*for(int i = 0; i<listTime.size(); i++) {
                Log.d("ALL_TIMES", String.valueOf(Integer.parseInt(listTime.get(i))));
            }*/

            for (int i = 0; i < listTime.size(); i++) {
                if ((Integer.parseInt(String.valueOf(currentTimeInMS)) >= Integer.parseInt(listTime.get(i))) && (Integer.parseInt(String.valueOf(currentTimeInMS)) < Integer.parseInt(listTime.get(i+1))) ) {

                    currentTimeSlot = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toHours(Long.parseLong(listTime.get(i))),
                            TimeUnit.MILLISECONDS.toMinutes(Long.parseLong(listTime.get(i))) % TimeUnit.HOURS.toMinutes(1));

                    //Log.d("FINAL_TIME", currentTimeSlot);
                }
            }

            txtSlot.setText(currentTimeSlot);

            //Log.d("THERAPIST_NAMES", String.valueOf(listTherapists));
            //Log.d("LIST_TIME", String.valueOf(listTime));
            //Log.d("CURRENT_TIME", String.valueOf(currentTime));
            //Log.d("TOTAL", String.valueOf(currentTimeInMS));

        } catch (JSONException e) {
            Log.d("GET_ERROR", e.getMessage());
        }

        //progressBarName.setVisibility(View.GONE);

    }
}