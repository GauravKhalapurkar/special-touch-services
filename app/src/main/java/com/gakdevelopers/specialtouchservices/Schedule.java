package com.gakdevelopers.specialtouchservices;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Schedule extends AppCompatActivity {

    String therapistName = null;

    ToggleButton tglBtn;

    ListView listView;

    ProgressDialog loading;

    SimpleAdapter adapter;

    TextView txtNoData;

    FloatingActionButton fabExport;

    Spinner spinnerName;
    ArrayList<String> arrayListName;
    ArrayAdapter<String> arrayAdapterName;
    ProgressBar progressBarName;

    ArrayList<String> arrayListData;

    ArrayList<HashMap<String, String>> list = new ArrayList<>();

    TextView txtAppointmentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        //IMP
        therapistName = getIntent().getStringExtra("therapistName");

        spinnerName = (Spinner) findViewById(R.id.spinnerName);
        progressBarName = (ProgressBar) findViewById(R.id.progressBarName);

        arrayListData = new ArrayList<>();

        arrayListName = new ArrayList<>();
        arrayAdapterName = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, arrayListName);
        spinnerName.setAdapter(arrayAdapterName);

        tglBtn = (ToggleButton) findViewById(R.id.tglBtn);

        listView = (ListView) findViewById(R.id.lv_items);
        listView.setVisibility(View.GONE);

        txtNoData = (TextView) findViewById(R.id.txtNoData);
        txtNoData.setVisibility(View.GONE);

        txtAppointmentDate = (TextView) findViewById(R.id.txtAppointmentDate);

        txtAppointmentDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int i1, int i2) {
                Schedule.this.adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        fabExport = (FloatingActionButton) findViewById(R.id.fabExport);

        tglBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tglBtn.getText().toString().equals("SCHEDULE")) {
                    spinnerName.setSelection(0);
                    loadSchedule();
                    txtAppointmentDate.setText("");
                    loading.dismiss();
                }

                if (tglBtn.getText().toString().equals("HISTORY")) {
                    spinnerName.setSelection(0);
                    loadHistory();
                    txtAppointmentDate.setText("");
                    loading.dismiss();
                }
            }
        });

        loadSchedule();

        spinnerName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                if (tglBtn.getText().toString().equals("SCHEDULE")) {
                    if (position == 0) {
                        loadSchedule();
                    }

                    if (position != 0) {
                        loadSelectedScheduleClients();
                    }
                }

                if (tglBtn.getText().toString().equals("HISTORY")) {
                    if (position == 0) {
                        loadHistory();
                    }

                    if (position != 0) {
                        loadSelectedHistoryClients();
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

    }

    private void loadSchedule() {
        loading =  ProgressDialog.show(this,"Loading","Please Wait",false,true);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.api) + "?action=getSchedule",
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

    private void loadSelectedScheduleClients() {
        loading =  ProgressDialog.show(this,"Loading","Please Wait",false,true);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.api) + "?action=getSchedule",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseSelectedScheduleClients(response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Schedule.this, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        loading.dismiss();
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

    private void loadSelectedHistoryClients() {
        loading =  ProgressDialog.show(this,"Loading","Please Wait",false,true);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.api) + "?action=getHistory",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseSelectedHistoryClients(response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Schedule.this, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        loading.dismiss();
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

    private void loadHistory() {
        loading =  ProgressDialog.show(this,"Loading","Please Wait",false,true);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.api) + "?action=getHistory",
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

        list.clear();

        arrayListName.add("---All clients---");

        try {
            JSONObject jObj = new JSONObject(jsonResponse);
            JSONArray jArray = jObj.getJSONArray("items");

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jo = jArray.getJSONObject(i);
                String Therapists = jo.getString("Therapists");
                String Date = jo.getString("Date");
                String DayOfWeek = jo.getString("DayOfWeek");
                String Time = jo.getString("Time");
                String Client = jo.getString("Client");

                if (!Client.equals(""))
                    arrayListName.add(Client);

                Set<String> ay = new LinkedHashSet<String>(arrayListName);

                arrayListName.clear();

                arrayListName.addAll(ay);

                HashMap<String, String> item = new HashMap<>();
                item.put("Date", Date);
                item.put("DayOfWeek", DayOfWeek);
                item.put("Time", Time);
                item.put("Client", Client);

                if (Therapists.equals(therapistName))
                    list.add(item);
            }

            arrayAdapterName.notifyDataSetChanged();

        } catch (JSONException e) {
            Log.d("GET_ERROR", e.getMessage());
        }

        progressBarName.setVisibility(View.GONE);

        if (list.isEmpty()) {
            txtNoData.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            txtNoData.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            adapter = new SimpleAdapter(this, list, R.layout.schedule_items,
                    new String[]{"Date", "DayOfWeek", "Time", "Client"}, new int[]{R.id.txtDate, R.id.txtDayOfWeek, R.id.txtTime, R.id.txtClient});

            listView.setAdapter((android.widget.ListAdapter) adapter);
        }

        loading.dismiss();
    }

    private void parseSelectedScheduleClients(String jsonResponse) {

        list.clear();

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

                if (Therapists.equals(therapistName) && Client.equals(spinnerName.getSelectedItem()))
                    list.add(item);
            }

        } catch (JSONException e) {
            Log.d("GET_ERROR", e.getMessage());
        }

        if (list.isEmpty()) {
            txtNoData.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            txtNoData.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            adapter = new SimpleAdapter(this, list, R.layout.schedule_items,
                    new String[]{"DayOfWeek", "Time", "Client"}, new int[]{R.id.txtDayOfWeek, R.id.txtTime, R.id.txtClient});

            listView.setAdapter((android.widget.ListAdapter) adapter);
        }

        loading.dismiss();
    }

    private void parseSelectedHistoryClients(String jsonResponse) {

        list.clear();

        try {
            JSONObject jObj = new JSONObject(jsonResponse);
            JSONArray jArray = jObj.getJSONArray("items");

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jo = jArray.getJSONObject(i);
                String Therapists = jo.getString("Therapists");
                String Date = jo.getString("Date");
                String DayOfWeek = jo.getString("DayOfWeek");
                String Time = jo.getString("Time");
                String Client = jo.getString("Client");
                String Status = jo.getString("Status");
                String Message = jo.getString("Message");

                HashMap<String, String> item = new HashMap<>();
                item.put("Date", Date);
                item.put("DayOfWeek", DayOfWeek);
                item.put("Time", Time);
                item.put("Client", Client);
                item.put("Status", Status);
                item.put("Message", Message);

                if (Therapists.equals(therapistName) && Client.equals(spinnerName.getSelectedItem()))
                    list.add(item);
            }

        } catch (JSONException e) {
            Log.d("GET_ERROR", e.getMessage());
        }

        if (list.isEmpty()) {
            txtNoData.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            txtNoData.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            adapter = new SimpleAdapter(this, list, R.layout.history_items,
                    new String[]{"Date", "DayOfWeek", "Time", "Client", "Status", "Message"}, new int[]{R.id.txtDate, R.id.txtDayOfWeek, R.id.txtTime, R.id.txtClient, R.id.txtStatus, R.id.txtMessage});

            listView.setAdapter((android.widget.ListAdapter) adapter);
        }

        loading.dismiss();
    }

    private void parseHistoryItems(String jsonResponse) {

        list.clear();

        try {
            JSONObject jObj = new JSONObject(jsonResponse);
            JSONArray jArray = jObj.getJSONArray("items");

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jo = jArray.getJSONObject(i);
                String Therapists = jo.getString("Therapists");
                String Date = jo.getString("Date");
                String DayOfWeek = jo.getString("DayOfWeek");
                String Time = jo.getString("Time");
                String Client = jo.getString("Client");
                String Status = jo.getString("Status");
                String Message = jo.getString("Message");

                HashMap<String, String> item = new HashMap<>();
                item.put("Date", Date);
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

        if (list.isEmpty()) {
            txtNoData.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            txtNoData.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            adapter = new SimpleAdapter(this, list, R.layout.history_items,
                    new String[]{"Date", "DayOfWeek", "Time", "Client", "Status", "Message"}, new int[]{R.id.txtDate, R.id.txtDayOfWeek, R.id.txtTime, R.id.txtClient, R.id.txtStatus, R.id.txtMessage});

            listView.setAdapter((android.widget.ListAdapter) adapter);
        }

        loading.dismiss();
    }

    public void viewStats(View view) {
        Intent intent = new Intent(Schedule.this, StatsView.class);
        intent.putExtra("type", "user");
        intent.putExtra("therapistName", therapistName);
        startActivity(intent);
    }

    public void orderDatePicker(View view) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONDAY, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                txtAppointmentDate.setText(simpleDateFormat.format(calendar.getTime()));
            }
        };
        new DatePickerDialog(Schedule.this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    public void clearClients(View view) {
        spinnerName.setSelection(0);
    }

    public void clearDate(View view) {
        txtAppointmentDate.setText("");
    }
}