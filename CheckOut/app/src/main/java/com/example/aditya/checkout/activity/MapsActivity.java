package com.example.aditya.checkout.activity;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.aditya.checkout.R;
import com.example.aditya.checkout.adapter.ServicesAdapter;
import com.example.aditya.checkout.constant.AppConstants;
import com.example.aditya.checkout.controller.AppController;
import com.example.aditya.checkout.database.DatabaseInteraction;
import com.example.aditya.checkout.fragment.FragmentDrawer;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, PlaceSelectionListener, FragmentDrawer.FragmentDrawerListener
{
    private static final int REQUEST_SELECT_PLACE = 1000;
    private Double myLatitude, myLongitude;
    private GoogleMap mMap;
    private LatLngBounds.Builder mBounds = new LatLngBounds.Builder();
    private ArrayList<String> latitudes, longitudes, services;
    public static ArrayList<String> registrationID;
    private DatabaseInteraction db;
    private ProgressDialog pDialog;
    ArrayList<Marker> markerNear = new ArrayList<>();
    Marker marker2;

    private BottomSheetBehavior mBottomSheetBehavior;
    int flag = 0;

    private String userid = "578351e4371adbd41a6a03c3";
    private int initialSecond;

    Geocoder geocoder;
    List<Address> addresses;

    private TextView locationUser;
    private TextView cancel;
    private TextView call;
    private TextView name, expertise, contact, experience;
    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView find = (TextView) findViewById(R.id.find);
        cancel = (TextView) findViewById(R.id.cancel);
        call = (TextView) findViewById(R.id.call);
        name = (TextView) findViewById(R.id.name);
        expertise = (TextView) findViewById(R.id.expertise);
        contact = (TextView) findViewById(R.id.contact);
        experience = (TextView) findViewById(R.id.experience);
        View bottomSheet = findViewById( R.id.bottom_sheet );

        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState)
            {
                if (newState == BottomSheetBehavior.STATE_SETTLING || flag == 1) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
            }
        });

        find.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(registrationID.size() == 0) {
                    Toast.makeText(MapsActivity.this, "No nearby lawyers to contact", Toast.LENGTH_LONG).show();
                } else {
                    pDialog = new ProgressDialog(MapsActivity.this);
                    pDialog.setCancelable(false);

                    String registerToken = "";

                    for(int i = 0; i < registrationID.size(); i++) {
                        registerToken = registerToken + "," + registrationID.get(i);
                    }

                    updateStatus(userid, registerToken);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                flag = 0;
            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:09462063122"));
                startActivity(callIntent);
            }
        });


        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        // display the first navigation drawer view on app launch
        displayView(0);

        locationUser = (TextView) findViewById(R.id.location);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geocoder = new Geocoder(this, Locale.getDefault());

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MapsActivity.this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        services = new ArrayList<>();
        registrationID = new ArrayList<>();

        services.add("Registration");
        services.add("TradeMark");
        services.add("Immigration");
        services.add("Criminal");
        services.add("Driving");
        services.add("Civil");
        services.add("Family");
        services.add("Trusts");
        services.add("Estates");
        services.add("Bankruptcy");
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener()
        {
            @Override
            public boolean onMyLocationButtonClick()
            {
                mMap.clear();

                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(myLatitude, myLongitude)));
                mMap.addMarker(new MarkerOptions().position(new LatLng(myLatitude, myLongitude)).icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener()
                {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition)
                    {

                    }
                });

                getNearbyLawyers(String.valueOf(myLatitude), String.valueOf(myLongitude), "Registration");

                try {
                    addresses = geocoder.getFromLocation(myLatitude, myLongitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();

                locationUser.setText(address + ", " + city + ", " + state + ", " + country);
                return true;
            }
        });

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener()
        {
            @Override
            public void onMyLocationChange(Location location)
            {
                myLatitude = location.getLatitude();
                myLongitude = location.getLongitude();

                LatLng ll1 = new LatLng(location.getLatitude()+0.002, location.getLongitude()+0.002);
                LatLng ll2 = new LatLng(location.getLatitude()-0.002, location.getLongitude()-0.002);

                addPointToViewPort(ll1, ll2);
                // we only want to grab the location once, to allow the user to pan and zoom freely.
                mMap.setOnMyLocationChangeListener(null);

                getNearbyLawyers(String.valueOf(myLatitude), String.valueOf(myLongitude), "Registration");

                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();

                locationUser.setText(address + ", " + city + ", " + state + ", " + country);
            }
        });

        // Pad the map controls to make room for the button - note that the button may not have been laid out yet.
        final LinearLayout button = (LinearLayout) findViewById(R.id.checkout_button);
        button.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout() {
                mMap.setPadding(0, button.getHeight() + 300, 0, 0);
            }
        });
    }

    private void addPointToViewPort(LatLng newPoint1, LatLng newPoint2)
    {
        mBounds.include(newPoint1);
        mBounds.include(newPoint2);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBounds.build(), findViewById(R.id.checkout_button).getHeight()));
    }

    public void checkOut(View view)
    {
        try
        {
            Intent intent = new PlaceAutocomplete.IntentBuilder (PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(MapsActivity.this);
            Bundle bundle = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation2,R.anim.animation).toBundle();
            startActivityForResult(intent, REQUEST_SELECT_PLACE, bundle);

        } catch (GooglePlayServicesRepairableException e) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    REQUEST_SELECT_PLACE);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, "Please install Google Play Services!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPlaceSelected(final Place place)
    {
        mMap.clear();

        db = new DatabaseInteraction(MapsActivity.this);
        String tag = "";

        Cursor cursor1 = db.getAllItems();
        if (cursor1.moveToFirst())
        {
            do {
                if(Integer.parseInt(cursor1.getString(cursor1.getColumnIndex("color"))) == 1)
                {
                    tag = cursor1.getString(cursor1.getColumnIndex("name"));
                    break;
                }
            } while (cursor1.moveToNext());
        }

        final LatLng queriedLocation = place.getLatLng();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(queriedLocation));
        mMap.addMarker(new MarkerOptions().position(queriedLocation).icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition)
            {

            }
        });

        try {
            addresses = geocoder.getFromLocation(queriedLocation.latitude, queriedLocation.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0);
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();

        locationUser.setText(address + ", " + city + ", " + state + ", " + country);


        getNearbyLawyers(String.valueOf(queriedLocation.latitude), String.valueOf(queriedLocation.longitude), tag);
    }

    public void getNearbyLawyers(final String latitude, final String longitude, final String expertise)
    {
        String tag_string_req = "req_lawyers";

        latitudes = new ArrayList<>();
        longitudes = new ArrayList<>();
        registrationID.clear();

        markerNear.clear();

        /*pDialog.setMessage("Getting nearby lawyers...");
        showDialog();*/

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConstants.NEARBY, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONArray jsonArray = new JSONArray(response);

                    if(jsonArray.length() == 0) {
                        Toast.makeText(MapsActivity.this, "No nearby lawyers in your area", Toast.LENGTH_LONG).show();
                    }

                    else
                    {
                        for(int i = 0; i < jsonArray.length(); i++)
                        {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            latitudes.add(jsonObject.getString("latitude"));
                            longitudes.add(jsonObject.getString("longitude"));
                            //registrationID.add(jsonObject.getString("registerToken"));

                            LatLng queriedLocation = new LatLng(Double.parseDouble(latitudes.get(i)),
                                    Double.parseDouble(longitudes.get(i)));

                            marker2 = mMap.addMarker(new MarkerOptions().position(queriedLocation));
                            markerNear.add(marker2);
                        }
                    }

                    //hideDialog();

                    if(jsonArray.length() != 0) {
                        Toast.makeText(MapsActivity.this, jsonArray.length() + " nearby lawyers in your area", Toast.LENGTH_LONG).show();
                    }

                    mAdapter = new ServicesAdapter(MapsActivity.this, services, latitude, longitude, mMap);
                    mRecyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();

                }
                catch (JSONException e) {
                    hideDialog();
                    Toast.makeText(MapsActivity.this, "No or slow Internet connection. Please try again later", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                hideDialog();
                Toast.makeText(MapsActivity.this, "No or slow Internet connection. Please try again later",Toast.LENGTH_LONG).show();
            }
        })

        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "near");
                params.put("latitude", latitude);
                params.put("expertise", expertise);

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void updateStatus(final String userid, final String registerToken)
    {
        String tag_string_req = "req_update";

        pDialog.setMessage("Contacting lawyers near you....");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConstants.UPDATE, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if(jsonObject.getString("message").equals("Data is updated"))
                        notifyLawyers(registerToken);

                    else {
                        hideDialog();
                        Toast.makeText(getApplicationContext(), "No or slow Internet connection. Please try again later. ", Toast.LENGTH_LONG).show();
                    }
                }
                catch (Exception e) {
                    hideDialog();
                    Toast.makeText(getApplicationContext(), "No or slow Internet connection. Please try again later. ", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener()
        {

            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(getApplicationContext(), "No or slow Internet connection. Please try again later. ", Toast.LENGTH_LONG).show();
                hideDialog();
            }
        })

        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "update");
                params.put("id", userid);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void notifyLawyers(final String registerToken)
    {
        String tag_string_req = "req_notify";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConstants.NOTIFY, new Response.Listener<String>()
        {

            @Override
            public void onResponse(String response)
            {
                try {
                    Calendar c = Calendar.getInstance();
                    initialSecond = c.get(Calendar.SECOND);
                    checkStatus(userid, initialSecond);
                }
                catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "No or slow Internet connection. Please try again later. ", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener()
        {

            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(getApplicationContext(), "No or slow Internet connection. Please try again later. ", Toast.LENGTH_LONG).show();
                hideDialog();
            }
        })

        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "notify");
                params.put("registerToken", registerToken);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void checkStatus(final String userid, final int seconds)
    {
        if(seconds - initialSecond > 60) {
            hideDialog();
            Toast.makeText(MapsActivity.this, "No lawyer responded to your request. Try again", Toast.LENGTH_LONG).show();
        }

        else
        {
            String tag_string_req = "req_check";

            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConstants.CHECK, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        if(jsonObject.getString("status").equals("1")) {
                            findMyLawyer(userid);
                        }

                        else if(jsonObject.getString("status").equals("0"))
                        {
                            Calendar c = Calendar.getInstance();
                            int second = c.get(Calendar.SECOND);
                            checkStatus(userid, second);
                        }
                    }
                    catch (Exception e) {
                        hideDialog();
                        Toast.makeText(getApplicationContext(), "No or slow Internet connection. Please try again later. ", Toast.LENGTH_LONG).show();
                    }

                }
            }, new Response.ErrorListener()
            {

                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Toast.makeText(getApplicationContext(), "No or slow Internet connection. Please try again later. ", Toast.LENGTH_LONG).show();
                    hideDialog();
                }
            })

            {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String> params = new HashMap<>();
                    params.put("tag", "update");
                    params.put("id", userid);
                    return params;
                }
            };

            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    public void findMyLawyer(final String userid)
    {
        String tag_string_req = "req_find";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConstants.FIND, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("message");

                    JSONObject message = jsonArray.getJSONObject(jsonArray.length() - 1);
                    name.setText(message.getString("name"));
                    expertise.setText(message.getString("expertise"));
                    contact.setText(message.getString("contact"));
                    experience.setText(message.getString("experience"));

                    hideDialog();

                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        flag = 1;
                }
                catch (Exception e) {
                    hideDialog();
                    Toast.makeText(getApplicationContext(), "No or slow Internet connection. Please try again later. ",
                            Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener()
        {

            @Override
            public void onErrorResponse(VolleyError error)
            {
                hideDialog();
                Toast.makeText(getApplicationContext(), "No or slow Internet connection. Please try again later. ", Toast.LENGTH_LONG).show();
            }
        })

        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                params.put("tag", "find");
                params.put("id", userid);
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

    @Override
    public void onError(Status status) {
        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_SELECT_PLACE)
        {
            if (resultCode == RESULT_OK)
            {
                Place place = PlaceAutocomplete.getPlace(this, data);
                this.onPlaceSelected(place);
            }

            else if (resultCode == PlaceAutocomplete.RESULT_ERROR)
            {
                Status status = PlaceAutocomplete.getStatus(this, data);
                this.onError(status);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position)
    {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                /*fragment = new HomeFragment();
                title = getString(R.string.title_home);*/
                break;
            case 1:
                /*fragment = new FriendsFragment();
                title = getString(R.string.title_friends);*/
                break;
            case 2:
                /*fragment = new MessagesFragment();
                title = getString(R.string.title_messages);*/
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }
    }
}
