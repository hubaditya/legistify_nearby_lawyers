package com.example.aditya.gcmtest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ResponseActivity extends AppCompatActivity
{
    String userID;
    String status;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
            userID = bundle.getString("userId");

        Button send = (Button) findViewById(R.id.send);

        if (send != null) {
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pDialog = new ProgressDialog(ResponseActivity.this);
                    pDialog.setCancelable(false);
                    checkUserStatus(userID);
                }
            });
        }
    }

    public void checkUserStatus(final String userID)
    {
        String tag_string_req = "req_check";

        status = "0";

        pDialog.setMessage("Sending response...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConstants.CHECK_STATUS, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    status = jsonObject.getString("status");

                    if(status.equals("1")) {
                        hideDialog();
                        Toast.makeText(ResponseActivity.this, "Client was responded by another lawyer", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ResponseActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else if(status.equals("0")) {
                        status = "1";
                        updateStatus(status);
                    }
                }
                catch (Exception e) {
                    Toast.makeText(ResponseActivity.this, "No or slow Internet connection. Please try again later", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                hideDialog();
                Toast.makeText(ResponseActivity.this, "No or slow Internet connection. Please try again later",Toast.LENGTH_LONG).show();
            }
        })

        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "check");
                params.put("id", userID);

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void updateStatus(final String status)
    {
        String tag_string_req = "req_update";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConstants.UPDATE_STATUS, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getString("message").equals("Data is updated"))
                        insertInteraction("Aditya Srivastava", "TradeMark", "09462063122", "6 years");

                    else {
                        hideDialog();
                        Toast.makeText(ResponseActivity.this, "No or slow Internet connection. Please try again later", Toast.LENGTH_LONG)
                                .show();
                    }
                }
                catch (Exception e) {
                    Toast.makeText(ResponseActivity.this, "No or slow Internet connection. Please try again later", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                hideDialog();
                Toast.makeText(ResponseActivity.this, "No or slow Internet connection. Please try again later",Toast.LENGTH_LONG).show();
            }
        })

        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "update");
                params.put("status", status);

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void insertInteraction(final String name, final String expertise, final String contact, final String experience)
    {
        String tag_string_req = "req_insert";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConstants.INTERACT, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try {
                    hideDialog();

                    JSONObject jsonObject = new JSONObject(response);

                    if(jsonObject.getString("message").equals("Data added")) {
                        Toast.makeText(ResponseActivity.this, "Client has received your response", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ResponseActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    else Toast.makeText(ResponseActivity.this, "No or slow Internet connection. Please try again later",
                            Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    Toast.makeText(ResponseActivity.this, "No or slow Internet connection. Please try again later", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                Toast.makeText(ResponseActivity.this, "No or slow Internet connection. Please try again later",Toast.LENGTH_LONG).show();
            }
        })

        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "update");
                params.put("userid", userID);
                params.put("name", name);
                params.put("expertise", expertise);
                params.put("contact", contact);
                params.put("experience", experience);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
