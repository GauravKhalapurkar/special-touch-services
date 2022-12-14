package com.gakdevelopers.specialtouchservices;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView txtTherapistName, txtSlotDay, txtAt, txtSlotTime, txtClientName, txtUpdatedOn;

    ArrayList<String> listTherapists, listTime, listClients, listDayOfWeek, listDate;

    CardView cardHere, cardAbsent, cardPhoneCall;

    public static String therapistName = null;

    LinearLayout linearCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //IMP
        therapistName = getIntent().getStringExtra("therapistName");

        txtTherapistName = (TextView) findViewById(R.id.txtTherapistName);
        txtSlotDay = (TextView) findViewById(R.id.txtSlotDay);
        txtAt = (TextView) findViewById(R.id.txtAt);
        txtSlotTime = (TextView) findViewById(R.id.txtSlotTime);
        txtClientName = (TextView) findViewById(R.id.txtClientName);
        txtUpdatedOn = (TextView) findViewById(R.id.txtUpdatedOn);

        //IMP
        txtTherapistName.setText("" + therapistName);

        txtAt.setVisibility(View.GONE);
        txtSlotTime.setVisibility(View.GONE);

        cardHere = (CardView) findViewById(R.id.cardHere);
        cardAbsent = (CardView) findViewById(R.id.cardAbsent);
        cardPhoneCall = (CardView) findViewById(R.id.cardPhoneCall);

        linearCards = (LinearLayout) findViewById(R.id.linearCards);
        for ( int i = 0; i < linearCards.getChildCount();  i++ ){
            View view = linearCards.getChildAt(i);
            view.setVisibility(View.GONE);
        }

        listTherapists = new ArrayList<>();
        listTime = new ArrayList<>();
        listClients = new ArrayList<>();
        listDayOfWeek = new ArrayList<>();
        listDate = new ArrayList<>();

        final Handler ha = new Handler();
        ha.postDelayed(new Runnable() {
            @Override
            public void run() {
                getScheduleDetails();
                String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                txtUpdatedOn.setText("Last updated @ " + currentTime);
                ha.postDelayed(this, 300000);

            }
        }, 0);

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
                input.setHint("(Optional)");
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

        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        final String dayOfWeek = txtSlotDay.getText().toString().trim();
        final String time = txtSlotTime.getText().toString().trim();
        final String client = txtClientName.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "" + getString(R.string.api),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        //Toast.makeText(MainActivity.this, "Saved Successfully!", Toast.LENGTH_SHORT).show();

                        String therapist = txtTherapistName.getText().toString();
                        String dayOfWeek = txtSlotDay.getText().toString();
                        String time = txtSlotTime.getText().toString();
                        String client = txtClientName.getText().toString();

                        deleteCurrentItem(therapist, dayOfWeek, time, client);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "ERROR SAVING: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parmas = new HashMap<>();

                parmas.put("action", "addItem");
                parmas.put("Therapists", therapist);
                parmas.put("Date", "'" + date);
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

    private void deleteCurrentItem(String therapist, String dayOfWeek, String time, String client) {
        final ProgressDialog loading = ProgressDialog.show(this, "Removing Data", "Please Wait");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.api) + "?action=deleteRecord&Therapists=" + therapist + "&DayOfWeek=" + dayOfWeek + "&Time=" + time + "&Client=" + client,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        getScheduleDetails();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "ERROR REMOVING: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void getScheduleDetails() {
        final ProgressDialog loading = ProgressDialog.show(this, "Loading Data", "Please Wait");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.api) + "?action=getSchedule",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseItems(response);
                        loading.dismiss();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Toast.makeText(MainActivity.this, "ERROR LOADING: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

        for ( int j = 0; j < linearCards.getChildCount();  j++ ){
            View view = linearCards.getChildAt(j);
            view.setVisibility(View.GONE);
        }

        txtSlotDay.setText("Loading...");
        txtClientName.setText("Loading...");
        txtAt.setVisibility(View.GONE);
        txtSlotTime.setVisibility(View.GONE);

        try {
            JSONObject jObj = new JSONObject(jsonResponse);
            JSONArray jArray = jObj.getJSONArray("items");

            listTherapists.clear();
            listDayOfWeek.clear();
            listDate.clear();
            listTime.clear();
            listClients.clear();

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jo = jArray.getJSONObject(i);
                String therapists = jo.getString("Therapists");
                String date = jo.getString("Date");
                String dayOfWeek = jo.getString("DayOfWeek");
                String time = jo.getString("Time");
                String client = jo.getString("Client");

                if (time.equals("")) {
                    continue;
                }

                String[] tokens = time.split(":");
                int minutesToMs = Integer.parseInt(tokens[1]) * 60000;
                int hoursToMs = Integer.parseInt(tokens[0]) * 3600000;
                long total = minutesToMs + hoursToMs;

                listTherapists.add(therapists);
                listDayOfWeek.add(dayOfWeek);
                listDate.add(date);
                listTime.add("" + total);
                listClients.add(client);
            }

            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date d = new Date();
            String date = sdf.format(d);

            String[] tokens = currentTime.split(":");
            int minutesToMs = Integer.parseInt(tokens[1]) * 60000;
            int hoursToMs = Integer.parseInt(tokens[0]) * 3600000;
            long currentTimeInMS = minutesToMs + hoursToMs;

            for (int i = 0; i < listTime.size(); i++) {
                if ( (accountHolder.equals(listTherapists.get(i)) && (Integer.parseInt(String.valueOf(currentTimeInMS)) >= Integer.parseInt(listTime.get(i)))) && date.equals(listDate.get(i)) ) {

                    currentTimeSlot = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toHours(Long.parseLong(listTime.get(i))),
                            TimeUnit.MILLISECONDS.toMinutes(Long.parseLong(listTime.get(i))) % TimeUnit.HOURS.toMinutes(1));

                    txtSlotDay.setText(listDayOfWeek.get(i));
                    txtSlotTime.setText(currentTimeSlot);
                    txtClientName.setText(listClients.get(i));

                    txtAt.setVisibility(View.VISIBLE);
                    txtSlotTime.setVisibility(View.VISIBLE);

                    for ( int j = 0; j < linearCards.getChildCount();  j++ ){
                        View view = linearCards.getChildAt(j);
                        view.setVisibility(View.VISIBLE);
                    }
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

        } catch (JSONException e) {
            Toast.makeText(this, "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void showSchedule(View view) {
        Intent intent = new Intent(MainActivity.this, Schedule.class);
        //IMP
        intent.putExtra("therapistName", therapistName);
        startActivity(intent);
    }

    public void refresh(View view) {
        getScheduleDetails();
    }
}