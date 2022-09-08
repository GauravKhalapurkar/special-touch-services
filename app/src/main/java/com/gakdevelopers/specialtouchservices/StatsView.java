package com.gakdevelopers.specialtouchservices;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class StatsView extends AppCompatActivity {

    TextView txtType;

    String type = null, therapistName = null;

    ProgressDialog loading;

    ArrayList<String> arrayList, arrayListHere, arrayListAbsent, arrayListPhoneCall, arrayListHereCount, arrayListAbsentCount, arrayListPhoneCallCount;

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_view);

        txtType = (TextView) findViewById(R.id.txtType);

        listView = (ListView) findViewById(R.id.lv_items);

        arrayList = new ArrayList<String>();
        arrayListHere = new ArrayList<String>();
        arrayListAbsent = new ArrayList<String>();
        arrayListPhoneCall = new ArrayList<String>();
        arrayListHereCount = new ArrayList<String>();
        arrayListAbsentCount = new ArrayList<String>();
        arrayListPhoneCallCount = new ArrayList<String>();

        type = getIntent().getStringExtra("type");

        //loadData();

        if (type.equals("user")) {
            therapistName = getIntent().getStringExtra("therapistName");
            txtType.setText("C L I E N T");
        } else if (type.equals("client")) {
            txtType.setText("C L I E N T");
        } else {
            txtType.setText("T H E R A P I S T");
        }

        loadData();
    }

    private void loadData() {
        loading =  ProgressDialog.show(this,"Loading","Please Wait",false,true);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.api) + "?action=getHistory",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseItems(response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(StatsView.this, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void parseItems(String jsonResponse) {

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

                if (type.equals("user")) {
                    if (Therapists.equals(therapistName)) {
                        arrayList.add("" + Client);

                        Set<String> removedRepeated = new LinkedHashSet<String>(arrayList);
                        arrayList.clear();
                        arrayList.addAll(removedRepeated);

                        if (Status.equals("Here")) {
                            arrayListHere.add("" + Client);
                        }

                        if (Status.equals("Absent")) {
                            arrayListAbsent.add("" + Client);
                        }

                        if (Status.equals("Phone Call")) {
                            arrayListPhoneCall.add("" + Client);
                        }
                    }
                } else if (type.equals("client")) {
                    arrayList.add("" + Client);

                    Set<String> removedRepeated = new LinkedHashSet<String>(arrayList);
                    arrayList.clear();
                    arrayList.addAll(removedRepeated);

                    if (Status.equals("Here")) {
                        arrayListHere.add("" + Client);
                    }

                    if (Status.equals("Absent")) {
                        arrayListAbsent.add("" + Client);
                    }

                    if (Status.equals("Phone Call")) {
                        arrayListPhoneCall.add("" + Client);
                    }
                } else {
                    arrayList.add("" + Therapists);

                    Set<String> removedRepeated = new LinkedHashSet<String>(arrayList);
                    arrayList.clear();
                    arrayList.addAll(removedRepeated);

                    if (Status.equals("Here")) {
                        arrayListHere.add("" + Therapists);
                    }

                    if (Status.equals("Absent")) {
                        arrayListAbsent.add("" + Therapists);
                    }

                    if (Status.equals("Phone Call")) {
                        arrayListPhoneCall.add("" + Therapists);
                    }
                }

            }

            for (int j = 0; j < arrayList.size(); j++) {
                int hereCount = Collections.frequency(arrayListHere, arrayList.get(j));
                arrayListHereCount.add("" + hereCount);

                int absentCount = Collections.frequency(arrayListAbsent, arrayList.get(j));
                arrayListAbsentCount.add("" + absentCount);

                int phoneCallCount = Collections.frequency(arrayListPhoneCall, arrayList.get(j));
                arrayListPhoneCallCount.add("" + phoneCallCount);
            }

            Log.d("MY_COUNT", String.valueOf(arrayList));
            Log.d("MY_COUNT_HERE", String.valueOf(arrayListHereCount));
            Log.d("MY_COUNT_ABSENT", String.valueOf(arrayListAbsentCount));
            Log.d("MY_COUNT_PHONE_CALL", String.valueOf(arrayListPhoneCallCount));

            int textSize = 0;
            textSize = (int) getResources().getDimension(R.dimen.font_size);

            TableLayout stk = (TableLayout) findViewById(R.id.tblMain);
            stk.setStretchAllColumns(true);

            stk.removeAllViews();

            TableRow tbrow0 = new TableRow(this);

            TextView tv0 = new TextView(this);
            tv0.setLayoutParams(new
                    TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv0.setGravity(Gravity.CENTER);
            tv0.setPadding(10, 15, 10, 15);
            tv0.setBackgroundColor(Color.parseColor("#f0f0f0"));
            tv0.setTextColor(Color.parseColor("#000000"));
            tv0.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

            if (type.equals("user"))
                tv0.setText("CLIENTS");
            else if (type.equals("client"))
                tv0.setText("CLIENTS");
            else
                tv0.setText("THERAPISTS");

            tbrow0.addView(tv0);

            TextView tv1 = new TextView(this);
            tv1.setLayoutParams(new
                    TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv1.setGravity(Gravity.CENTER);
            tv1.setPadding(10, 15, 10, 15);
            tv1.setBackgroundColor(Color.parseColor("#f0f0f0"));
            tv1.setTextColor(Color.parseColor("#000000"));
            tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            tv1.setText("HERE");
            tbrow0.addView(tv1);

            TextView tv2 = new TextView(this);
            tv2.setLayoutParams(new
                    TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv2.setGravity(Gravity.CENTER);
            tv2.setPadding(10, 15, 10, 15);
            tv2.setBackgroundColor(Color.parseColor("#f0f0f0"));
            tv2.setTextColor(Color.parseColor("#000000"));
            tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            tv2.setText("ABSENT");
            tbrow0.addView(tv2);

            TextView tv3 = new TextView(this);
            tv3.setLayoutParams(new
                    TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv3.setGravity(Gravity.CENTER);
            tv3.setPadding(10, 15, 10, 15);
            tv3.setBackgroundColor(Color.parseColor("#f0f0f0"));
            tv3.setTextColor(Color.parseColor("#000000"));
            tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            tv3.setText("PHONE CALL");
            tbrow0.addView(tv3);
            stk.addView(tbrow0);

            for (int i = 0; i < arrayList.size(); i++) {
                TableRow tbrow = new TableRow(this);

                TextView t1v = new TextView(this);
                t1v.setText("" + arrayList.get(i));
                t1v.setLayoutParams(new
                        TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                t1v.setGravity(Gravity.CENTER);
                t1v.setPadding(10, 15, 10, 15);
                t1v.setBackgroundColor(Color.parseColor("#f0f0f0"));
                t1v.setTextColor(Color.parseColor("#000000"));
                t1v.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                tbrow.addView(t1v);

                TextView t2v = new TextView(this);
                t2v.setText("" + arrayListHereCount.get(i));
                t2v.setLayoutParams(new
                        TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                t2v.setGravity(Gravity.CENTER);
                t2v.setPadding(10, 15, 10, 15);
                t2v.setBackgroundColor(Color.parseColor("#ffffff"));
                t2v.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                tbrow.addView(t2v);

                TextView t3v = new TextView(this);
                t3v.setText("" + arrayListAbsentCount.get(i));
                t3v.setLayoutParams(new
                        TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                t3v.setGravity(Gravity.CENTER);
                t3v.setPadding(10, 15, 10, 15);
                t3v.setBackgroundColor(Color.parseColor("#ffffff"));
                t3v.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                tbrow.addView(t3v);

                TextView t4v = new TextView(this);
                t4v.setText("" + arrayListPhoneCallCount.get(i));
                t4v.setLayoutParams(new
                        TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                t4v.setGravity(Gravity.CENTER);
                t4v.setPadding(10, 15, 10, 15);
                t4v.setBackgroundColor(Color.parseColor("#ffffff"));
                t4v.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                tbrow.addView(t4v);
                stk.addView(tbrow);
            }

        } catch (JSONException e) {
            Log.d("GET_ERROR", e.getMessage());
        }

        loading.dismiss();
    }
}