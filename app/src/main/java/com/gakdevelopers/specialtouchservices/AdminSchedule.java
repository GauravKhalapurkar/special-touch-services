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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AdminSchedule extends AppCompatActivity {

    String therapistName = null;

    ToggleButton tglBtn;

    ListView listView;

    ProgressDialog loading;

    SimpleAdapter adapter;

    TextView txtNoData;

    FloatingActionButton fabExport;

    Spinner spinnerClient, spinnerTherapist;

    ArrayList<String> arrayListClient, arrayListTherapist;

    ArrayAdapter<String> arrayAdapterClient, arrayAdapterTherapist;

    ProgressBar progressBarClientSpinner, progressBarTherapistSpinner;

    ArrayList<HashMap<String, String>> list = new ArrayList<>();

    TextView txtAppointmentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_schedule);

        //IMP
        therapistName = getIntent().getStringExtra("therapistName");

        spinnerClient = (Spinner) findViewById(R.id.spinnerClient);
        spinnerTherapist = (Spinner) findViewById(R.id.spinnerTherapist);
        progressBarClientSpinner = (ProgressBar) findViewById(R.id.progressBarClientSpinner);
        progressBarTherapistSpinner = (ProgressBar) findViewById(R.id.progressBarTherapistSpinner);

        arrayListClient = new ArrayList<>();
        arrayAdapterClient = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, arrayListClient);
        spinnerClient.setAdapter(arrayAdapterClient);

        arrayListTherapist = new ArrayList<>();
        arrayAdapterTherapist = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, arrayListTherapist);
        spinnerTherapist.setAdapter(arrayAdapterTherapist);

        tglBtn = (ToggleButton) findViewById(R.id.tglBtn);

        listView = (ListView) findViewById(R.id.lv_items);
        listView.setVisibility(View.GONE);

        txtNoData = (TextView) findViewById(R.id.txtNoData);
        txtNoData.setVisibility(View.GONE);

        fabExport = (FloatingActionButton) findViewById(R.id.fabExport);

        txtAppointmentDate = (TextView) findViewById(R.id.txtAppointmentDate);

        txtAppointmentDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int i1, int i2) {
                AdminSchedule.this.adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        tglBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tglBtn.getText().toString().equals("SCHEDULE")) {
                    spinnerClient.setSelection(0);
                    spinnerTherapist.setSelection(0);
                    loadSchedule();
                    txtAppointmentDate.setText("");
                    loading.dismiss();
                }

                if (tglBtn.getText().toString().equals("HISTORY")) {
                    spinnerClient.setSelection(0);
                    spinnerTherapist.setSelection(0);
                    loadHistory();
                    txtAppointmentDate.setText("");
                    loading.dismiss();
                }
            }
        });

        loadSchedule();

        spinnerTherapist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                if (tglBtn.getText().toString().equals("SCHEDULE")) {

                    if (position != 0 || !spinnerClient.getSelectedItem().equals("---All clients---")) {
                        loadSelectedScheduleClients();
                        loading.dismiss();
                    }

                    if (position == 0  && !spinnerClient.getSelectedItem().equals("---All clients---")) {
                        loadSelectedScheduleClients();
                        loading.dismiss();
                    }

                    if (position == 0 && spinnerClient.getSelectedItem().equals("---All clients---")) {
                        loadSchedule();
                        loading.dismiss();
                    }

                }

                if (tglBtn.getText().toString().equals("HISTORY")) {

                    if (position != 0 || !spinnerClient.getSelectedItem().equals("---All clients---")) {
                        loadSelectedHistoryClients();
                        loading.dismiss();
                    }

                    if (position == 0  && !spinnerClient.getSelectedItem().equals("---All clients---")) {
                        loadSelectedHistoryClients();
                        loading.dismiss();
                    }

                    if (position == 0 && spinnerClient.getSelectedItem().equals("---All clients---")) {
                        loadHistory();
                        loading.dismiss();
                    }

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        spinnerClient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                if (tglBtn.getText().toString().equals("SCHEDULE")) {

                    if (position != 0 || !spinnerTherapist.getSelectedItem().equals("---All therapists---")) {
                        loadSelectedScheduleClients();
                        loading.dismiss();
                    }

                    if (position == 0  && !spinnerTherapist.getSelectedItem().equals("---All therapists---")) {
                        loadSelectedScheduleClients();
                        loading.dismiss();
                    }

                    if (position == 0 && spinnerTherapist.getSelectedItem().equals("---All therapists---")) {
                        loadSchedule();
                        loading.dismiss();
                    }

                }

                if (tglBtn.getText().toString().equals("HISTORY")) {

                    if (position != 0 || !spinnerTherapist.getSelectedItem().equals("---All therapists---")) {
                        loadSelectedHistoryClients();
                        loading.dismiss();
                    }

                    if (position == 0  && !spinnerTherapist.getSelectedItem().equals("---All therapists---")) {
                        loadSelectedHistoryClients();
                        loading.dismiss();
                    }

                    if (position == 0 && spinnerTherapist.getSelectedItem().equals("---All therapists---")) {
                        loadHistory();
                        loading.dismiss();
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
                        Toast.makeText(AdminSchedule.this, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                        progressBarClientSpinner.setVisibility(View.GONE);
                        progressBarTherapistSpinner.setVisibility(View.GONE);
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
                        Toast.makeText(AdminSchedule.this, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                        progressBarClientSpinner.setVisibility(View.GONE);
                        progressBarTherapistSpinner.setVisibility(View.GONE);
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
                        Toast.makeText(AdminSchedule.this, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(AdminSchedule.this, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                        progressBarClientSpinner.setVisibility(View.GONE);
                        progressBarTherapistSpinner.setVisibility(View.GONE);
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

        //ArrayList<HashMap<String, String>> list = new ArrayList<>();

        list.clear();

        arrayListClient.add("---All clients---");
        arrayListTherapist.add("---All therapists---");

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

                if (!Client.equals("")) {
                    arrayListClient.add(Client);
                    arrayListTherapist.add(Therapists);
                }

                Set<String> ayc = new LinkedHashSet<String>(arrayListClient);
                Set<String> ayt = new LinkedHashSet<String>(arrayListTherapist);

                arrayListClient.clear();
                arrayListTherapist.clear();

                arrayListClient.addAll(ayc);
                arrayListTherapist.addAll(ayt);

                HashMap<String, String> item = new HashMap<>();
                item.put("Therapists", Therapists);
                item.put("Date", Date);
                item.put("DayOfWeek", DayOfWeek);
                item.put("Time", Time);
                item.put("Client", Client);

                list.add(item);
            }

            arrayAdapterClient.notifyDataSetChanged();
            arrayAdapterTherapist.notifyDataSetChanged();

        } catch (JSONException e) {
            Log.d("GET_ERROR", e.getMessage());
        }

        progressBarClientSpinner.setVisibility(View.GONE);
        progressBarTherapistSpinner.setVisibility(View.GONE);

        if (list.isEmpty()) {
            txtNoData.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            txtNoData.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            adapter = new SimpleAdapter(this, list, R.layout.admin_schedule_items,
                    new String[]{"Therapists", "Date", "DayOfWeek", "Time", "Client"}, new int[]{R.id.txtTherapistName, R.id.txtDate, R.id.txtDayOfWeek, R.id.txtTime, R.id.txtClient});

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
                String Date = jo.getString("Date");
                String DayOfWeek = jo.getString("DayOfWeek");
                String Time = jo.getString("Time");
                String Client = jo.getString("Client");

                HashMap<String, String> item = new HashMap<>();
                item.put("Therapists", Therapists);
                item.put("Date", Date);
                item.put("DayOfWeek", DayOfWeek);
                item.put("Time", Time);
                item.put("Client", Client);

                if ((!spinnerTherapist.getSelectedItem().equals("---All therapists---") && !spinnerClient.getSelectedItem().equals("---All clients---")) &&
                        (Therapists.equals(spinnerTherapist.getSelectedItem()) && Client.equals(spinnerClient.getSelectedItem()))) {
                    list.add(item);
                }

                if (Therapists.equals(spinnerTherapist.getSelectedItem()) && spinnerClient.getSelectedItem().equals("---All clients---"))
                    list.add(item);

                if (Client.equals(spinnerClient.getSelectedItem()) && spinnerTherapist.getSelectedItem().equals("---All therapists---"))
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
            adapter = new SimpleAdapter(this, list, R.layout.admin_schedule_items,
                    new String[]{"Therapists", "Date", "DayOfWeek", "Time", "Client"}, new int[]{R.id.txtTherapistName, R.id.txtDate, R.id.txtDayOfWeek, R.id.txtTime, R.id.txtClient});

            listView.setAdapter((android.widget.ListAdapter) adapter);
        }

        loading.dismiss();
    }

    private void parseHistoryItems(String jsonResponse) {

        //ArrayList<HashMap<String, String>> list = new ArrayList<>();

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
                item.put("Therapists", Therapists);
                item.put("Date", Date);
                item.put("DayOfWeek", DayOfWeek);
                item.put("Time", Time);
                item.put("Client", Client);
                item.put("Status", Status);
                item.put("Message", Message);

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
            adapter = new SimpleAdapter(this, list, R.layout.admin_history_items,
                    new String[]{"Therapists", "Date", "DayOfWeek", "Time", "Client", "Status", "Message"}, new int[]{R.id.txtTherapistName, R.id.txtDate, R.id.txtDayOfWeek, R.id.txtTime, R.id.txtClient, R.id.txtStatus, R.id.txtMessage});

            listView.setAdapter((android.widget.ListAdapter) adapter);
        }

        loading.dismiss();
    }

    private void parseSelectedHistoryClients(String jsonResponse) {

        //ArrayList<HashMap<String, String>> list = new ArrayList<>();

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
                item.put("Therapists", Therapists);
                item.put("Date", Date);
                item.put("DayOfWeek", DayOfWeek);
                item.put("Time", Time);
                item.put("Client", Client);
                item.put("Status", Status);
                item.put("Message", Message);

                if ((!spinnerTherapist.getSelectedItem().equals("---All therapists---") && !spinnerClient.getSelectedItem().equals("---All clients---")) &&
                        (Therapists.equals(spinnerTherapist.getSelectedItem()) && Client.equals(spinnerClient.getSelectedItem()))) {
                    list.add(item);
                }

                if (Therapists.equals(spinnerTherapist.getSelectedItem()) && spinnerClient.getSelectedItem().equals("---All clients---"))
                    list.add(item);

                if (Client.equals(spinnerClient.getSelectedItem()) && spinnerTherapist.getSelectedItem().equals("---All therapists---"))
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
            adapter = new SimpleAdapter(this, list, R.layout.admin_history_items,
                    new String[]{"Therapists", "Date", "DayOfWeek", "Time", "Client", "Status", "Message"}, new int[]{R.id.txtTherapistName, R.id.txtDate, R.id.txtDayOfWeek, R.id.txtTime, R.id.txtClient, R.id.txtStatus, R.id.txtMessage});

            listView.setAdapter((android.widget.ListAdapter) adapter);
        }

        loading.dismiss();
    }

    public void viewTherapistStats(View view) {
        Intent intent = new Intent(AdminSchedule.this, StatsView.class);
        intent.putExtra("type", "therapist");
        startActivity(intent);
    }

    public void viewClientStats(View view) {
        Intent intent = new Intent(AdminSchedule.this, StatsView.class);
        intent.putExtra("type", "client");
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
        new DatePickerDialog(AdminSchedule.this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    public void clearTherapists(View view) {
        spinnerTherapist.setSelection(0);
    }

    public void clearClients(View view) {
        spinnerClient.setSelection(0);
    }

    public void clearDate(View view) {
        txtAppointmentDate.setText("");
    }
}