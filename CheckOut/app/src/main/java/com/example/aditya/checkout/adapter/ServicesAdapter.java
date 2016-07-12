package com.example.aditya.checkout.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.aditya.checkout.R;
import com.example.aditya.checkout.activity.MapsActivity;
import com.example.aditya.checkout.constant.AppConstants;
import com.example.aditya.checkout.controller.AppController;
import com.example.aditya.checkout.database.DatabaseInteraction;
import com.example.aditya.checkout.model.ColorList;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.ViewHolder>
{
    Context context;
    static ArrayList<String> services;
    String latitude, longitude;
    GoogleMap mMap;
    ProgressDialog pDialog;
    ArrayList<Marker> markerList;
    Marker marker;

    public ServicesAdapter(Context context, ArrayList<String> services, String latitude, String longitude,GoogleMap mMap)
    {
        this.context = context;
        this.services = services;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mMap = mMap;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_services, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)
    {
        holder.service.setText(services.get(position));

        Cursor cursor1 = holder.db.getItemDetail(services.get(position));
        if (cursor1.moveToFirst())
        {
            do {
                holder.id = Integer.parseInt(cursor1.getString(cursor1.getColumnIndex("color")));

                if(holder.id == 1)
                    holder.service.setTextColor(Color.parseColor("#232c3c"));

                else holder.service.setTextColor(Color.parseColor("#F9A825"));

            } while (cursor1.moveToNext());
        }


        holder.service.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(holder.service.getCurrentTextColor() == Color.parseColor("#232c3c"));

                else
                {
                    pDialog = new ProgressDialog(view.getContext());
                    pDialog.setCancelable(false);
                    getNearbyLawyers(latitude, holder.service.getText().toString(), holder.db);
                }
            }
        });
    }

    public void getNearbyLawyers(final String latitude, final String service, final DatabaseInteraction db)
    {
        String tag_string_req = "req_lawyers";

        markerList = new ArrayList<>();
        mMap.clear();
        MapsActivity.registrationID.clear();

        mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(latitude),
                Double.parseDouble(longitude))).icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        pDialog.setMessage("Getting nearby lawyers...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConstants.NEARBY, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONArray jsonArray = new JSONArray(response);

                    if(jsonArray.length() == 0)
                        Toast.makeText(context, "No nearby lawyers in your area.", Toast.LENGTH_LONG).show();

                    else
                    {
                        for(int i = 0; i < jsonArray.length(); i++)
                        {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            //MapsActivity.registrationID.add(jsonObject.getString("registerToken"));

                            LatLng queriedLocation = new LatLng(Double.parseDouble(jsonObject.getString("latitude")),
                                    Double.parseDouble(jsonObject.getString("longitude")));
                            marker = mMap.addMarker(new MarkerOptions().position(queriedLocation));
                            markerList.add(marker);
                        }
                    }

                    hideDialog();

                    if(jsonArray.length() != 0)
                        Toast.makeText(context, jsonArray.length() + " nearby lawyers in your area.",
                            Toast.LENGTH_LONG).show();

                    db.updateItemOne(service, "1");
                    notifyDataSetChanged();
                }
                catch (JSONException e) {
                    Toast.makeText(context, "No or slow Internet connection. Please try again later. ", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener()
        {

            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(context, "No or slow Internet connection. Please try again later. ", Toast.LENGTH_LONG).show();
                hideDialog();
            }
        })

        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "near");
                params.put("latitude", latitude);
                params.put("expertise", service);

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog()
    {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog()
    {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView service;
        int [] c = new int[10];
        DatabaseInteraction db;
        int id = -1;

        public ViewHolder(View itemView)
        {
            super(itemView);
            service = (TextView) itemView.findViewById(R.id.services);
            db = new DatabaseInteraction(itemView.getContext());

            c[0] = 1;
            db.addItem(new ColorList("1", services.get(0)));

            for(int i = 1; i < 10; i++)
            {
                c[i] = 0;
                db.addItem(new ColorList("0", services.get(i)));
            }
        }
    }
}
