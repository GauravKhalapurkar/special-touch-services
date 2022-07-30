package com.gakdevelopers.specialtouchservices;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerName;
    ArrayList<String> arrayListName;
    ArrayAdapter<String> arrayAdapterName;
    ProgressBar progressBarName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerName = (Spinner) findViewById(R.id.spinnerName);
        progressBarName = (ProgressBar) findViewById(R.id.progressBarName);

        arrayListName = new ArrayList<>();
        arrayAdapterName = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, arrayListName);
        spinnerName.setAdapter(arrayAdapterName);

        getTherapistNames();

    }

    private void getTherapistNames() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://script.google.com/macros/s/AKfycbw4m8tC1rkmOgZk0naVWVxbGa2owd_BRfD5woUiLpimIkhEP9m-uP807mL6UnJR3xJypA/exec?action=getItems",
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
                        progressBarName.setVisibility(View.GONE);
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
        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        arrayListName.add("---Select your name---");

        try {
            JSONObject jObj = new JSONObject(jsonResponse);
            JSONArray jArray = jObj.getJSONArray("items");

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jo = jArray.getJSONObject(i);
                String therapists = jo.getString("Therapists");
                arrayListName.add(therapists);
            }
            arrayAdapterName.notifyDataSetChanged();

            Log.d("THERAPIST_NAMES", String.valueOf(list));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        progressBarName.setVisibility(View.GONE);

    }

}