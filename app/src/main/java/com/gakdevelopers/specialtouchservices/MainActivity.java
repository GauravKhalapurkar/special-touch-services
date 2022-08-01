package com.gakdevelopers.specialtouchservices;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView txtTherapistName, txtSlotDay, txtAt, txtSlotTime, txtClientName;

    ArrayList<String> listTherapists, listTime, listClients, listDayOfWeek;

    CardView cardHere, cardAbsent, cardPhoneCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtTherapistName = (TextView) findViewById(R.id.txtTherapistName);
        txtSlotDay = (TextView) findViewById(R.id.txtSlotDay);
        txtAt = (TextView) findViewById(R.id.txtAt);
        txtSlotTime = (TextView) findViewById(R.id.txtSlotTime);
        txtClientName = (TextView) findViewById(R.id.txtClientName);

        txtAt.setVisibility(View.GONE);
        txtSlotTime.setVisibility(View.GONE);

        cardHere = (CardView) findViewById(R.id.cardHere);
        cardAbsent = (CardView) findViewById(R.id.cardAbsent);
        cardPhoneCall = (CardView) findViewById(R.id.cardPhoneCall);

        listTherapists = new ArrayList<>();
        listTime = new ArrayList<>();
        listClients = new ArrayList<>();
        listDayOfWeek = new ArrayList<>();

        getScheduleDetails();

        cardHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToSheet("Here", "NA");
            }
        });

        cardAbsent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToSheet("Absent", "NA");
            }
        });

        cardPhoneCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("Message");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                builder.setView(input);

                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String message = input.getText().toString();
                        addToSheet("Phone Call", "" + message);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

    }

    private void addToSheet(String sts, String msg) {
        final ProgressDialog loading = ProgressDialog.show(this, "Saving Data", "Please Wait");
        final String therapist = txtTherapistName.getText().toString().trim();
        final String dayOfWeek = txtSlotDay.getText().toString().trim();
        final String time = txtSlotTime.getText().toString().trim();
        final String client = txtClientName.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://script.google.com/macros/s/AKfycbz9IfyollSEmnJJB7esEqK3eQ6wkRhSKyig78kpO85cgkKngvUBAV7r8CZCb23Og7-_Ag/exec",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        Toast.makeText(MainActivity.this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parmas = new HashMap<>();

                parmas.put("action", "addItem");
                parmas.put("Therapists", therapist);
                parmas.put("DayOfWeek", dayOfWeek);
                parmas.put("Time", "'" + time);
                parmas.put("Client", client);
                parmas.put("Status", sts);
                parmas.put("Message", msg);

                return parmas;
            }
        };
        int socketTimeOut = 30000;

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(this);

        queue.add(stringRequest);

    }

    private void getScheduleDetails() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://script.google.com/macros/s/AKfycbz9IfyollSEmnJJB7esEqK3eQ6wkRhSKyig78kpO85cgkKngvUBAV7r8CZCb23Og7-_Ag/exec?action=getSchedule",
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

    @SuppressLint("DefaultLocale")
    private void parseItems(String jsonResponse) {
        String currentTimeSlot = "";
        String accountHolder = txtTherapistName.getText().toString();

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
                listDayOfWeek.add(dayOfWeek);
                listTime.add("" + total);
                listClients.add(client);
            }

            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
            Date d = new Date();
            String dayOfTheWeek = sdf.format(d);

            String[] tokens = currentTime.split(":");
            int minutesToMs = Integer.parseInt(tokens[1]) * 60000;
            int hoursToMs = Integer.parseInt(tokens[0]) * 3600000;
            long currentTimeInMS = minutesToMs + hoursToMs;

            for (int i = 0; i < listTime.size(); i++) {
                if ( (accountHolder.equals(listTherapists.get(i)) && (Integer.parseInt(String.valueOf(currentTimeInMS)) >= Integer.parseInt(listTime.get(i)))) && dayOfTheWeek.equals(listDayOfWeek.get(i)) ) {

                    currentTimeSlot = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toHours(Long.parseLong(listTime.get(i))),
                            TimeUnit.MILLISECONDS.toMinutes(Long.parseLong(listTime.get(i))) % TimeUnit.HOURS.toMinutes(1));

                    txtSlotDay.setText(listDayOfWeek.get(i));
                    txtSlotTime.setText(currentTimeSlot);
                    txtClientName.setText(listClients.get(i));

                    txtAt.setVisibility(View.VISIBLE);
                    txtSlotTime.setVisibility(View.VISIBLE);

                    //Log.d("FINAL_TIME", currentTimeSlot);
                }
            }

            if (txtSlotDay.getText().toString().equals("Loading...")) {
                txtSlotDay.setText("No bookings for now.");
                txtClientName.setText("NA");
                txtAt.setVisibility(View.GONE);
                txtSlotTime.setVisibility(View.GONE);
            }

            Log.d("THERAPIST_NAMES", String.valueOf(listTherapists));
            Log.d("LIST_TIME", String.valueOf(listTime));
            //Log.d("CURRENT_TIME", String.valueOf(currentTime));
            //Log.d("TOTAL", String.valueOf(currentTimeInMS));

        } catch (JSONException e) {
            Log.d("GET_ERROR", e.getMessage());
        }

        //progressBarName.setVisibility(View.GONE);

    }
}