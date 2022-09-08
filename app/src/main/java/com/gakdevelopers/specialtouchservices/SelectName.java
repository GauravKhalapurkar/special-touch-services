package com.gakdevelopers.specialtouchservices;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class SelectName extends AppCompatActivity {

    Spinner spinnerName;
    ArrayList<String> arrayListName;
    ArrayAdapter<String> arrayAdapterName;
    ProgressBar progressBarName;

    Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_name);

        spinnerName = (Spinner) findViewById(R.id.spinnerName);
        progressBarName = (ProgressBar) findViewById(R.id.progressBarName);

        btnNext = (Button) findViewById(R.id.btnNext);

        arrayListName = new ArrayList<>();
        arrayAdapterName = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, arrayListName);
        spinnerName.setAdapter(arrayAdapterName);

        getTherapistNames();

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String name = (String) spinnerName.getSelectedItem();

                    if (!name.equals("---Select your name---")) {
                        Intent intent = new Intent(SelectName.this, MainActivity.class);
                        intent.putExtra("therapistName", name);
                        startActivity(intent);
                    } else {
                        Toast.makeText(SelectName.this, "Please select your name.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(SelectName.this, "Please wait while it's loading.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getTherapistNames() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.api) + "?action=getTherapistNames",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseItems(response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SelectName.this, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void openAdminLogin(View view) {
        startActivity(new Intent(SelectName.this, AdminLogin.class));
    }
}