package com.gakdevelopers.specialtouchservices;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Schedule extends AppCompatActivity {

    String therapistName = null;

    ToggleButton tglBtn;

    ListView listView;

    ProgressDialog loading;

    SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        //IMP
        therapistName = getIntent().getStringExtra("therapistName");

        //therapistName = "Sharma";

        tglBtn = (ToggleButton) findViewById(R.id.tglBtn);

        listView = (ListView) findViewById(R.id.lv_items);

        tglBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tglBtn.getText().toString().equals("SCHEDULE")) {
                    loadSchedule();
                }

                if (tglBtn.getText().toString().equals("HISTORY")) {
                    loadHistory();
                }
            }
        });

        loadSchedule();
    }

    private void loadSchedule() {
        loading =  ProgressDialog.show(this,"Loading","Please Wait",false,true);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://script.google.com/macros/s/AKfycbwKyFsA_ps_eRVGWQLOjiPQHYS4ShcundZm8rfd-A5GiCzPMStoymh36pxQNZVt-SONQg/exec?action=getSchedule",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseScheduleItems(response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Schedule.this, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                }
        );

        int socketTimeOut = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        stringRequest.setRetryPolicy(policy);

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void loadHistory() {
        loading =  ProgressDialog.show(this,"Loading","Please Wait",false,true);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://script.google.com/macros/s/AKfycbwKyFsA_ps_eRVGWQLOjiPQHYS4ShcundZm8rfd-A5GiCzPMStoymh36pxQNZVt-SONQg/exec?action=getHistory",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseHistoryItems(response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Schedule.this, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                }
        );

        int socketTimeOut = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        stringRequest.setRetryPolicy(policy);

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void parseScheduleItems(String jsonResponse) {

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        try {
            JSONObject jObj = new JSONObject(jsonResponse);
            JSONArray jArray = jObj.getJSONArray("items");

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jo = jArray.getJSONObject(i);
                String Therapists = jo.getString("Therapists");
                String DayOfWeek = jo.getString("DayOfWeek");
                String Time = jo.getString("Time");
                String Client = jo.getString("Client");

                HashMap<String, String> item = new HashMap<>();
                item.put("DayOfWeek", DayOfWeek);
                item.put("Time", Time);
                item.put("Client", Client);

                if (Therapists.equals(therapistName))
                    list.add(item);
            }

        } catch (JSONException e) {
            Log.d("GET_ERROR", e.getMessage());
        }

        adapter = new SimpleAdapter(this, list, R.layout.schedule_items,
                new String[]{"DayOfWeek", "Time", "Client"}, new int[]{R.id.txtDayOfWeek, R.id.txtTime, R.id.txtClient});

        listView.setAdapter((android.widget.ListAdapter) adapter);

        loading.dismiss();
    }

    private void parseHistoryItems(String jsonResponse) {

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        try {
            JSONObject jObj = new JSONObject(jsonResponse);
            JSONArray jArray = jObj.getJSONArray("items");

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jo = jArray.getJSONObject(i);
                String Therapists = jo.getString("Therapists");
                String DayOfWeek = jo.getString("DayOfWeek");
                String Time = jo.getString("Time");
                String Client = jo.getString("Client");
                String Status = jo.getString("Status");
                String Message = jo.getString("Message");

                HashMap<String, String> item = new HashMap<>();
                item.put("DayOfWeek", DayOfWeek);
                item.put("Time", Time);
                item.put("Client", Client);
                item.put("Status", Status);
                item.put("Message", Message);

                if (Therapists.equals(therapistName))
                    list.add(item);
            }

        } catch (JSONException e) {
            Log.d("GET_ERROR", e.getMessage());
        }

        adapter = new SimpleAdapter(this, list, R.layout.history_items,
                new String[]{"DayOfWeek", "Time", "Client", "Status", "Message"}, new int[]{R.id.txtDayOfWeek, R.id.txtTime, R.id.txtClient, R.id.txtStatus, R.id.txtMessage});

        listView.setAdapter((android.widget.ListAdapter) adapter);

        loading.dismiss();
    }
}