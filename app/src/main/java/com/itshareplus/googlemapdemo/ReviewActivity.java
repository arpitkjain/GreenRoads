package com.itshareplus.googlemapdemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import Modules.DirectionFinderListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import android.widget.Toast;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.view.View.OnClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Modules.MyDBHandler;
//import Modules.NearestRoad;

public class ReviewActivity extends FragmentActivity implements OnMapReadyCallback {
    public class NearestRoad {
        private static final String ROADS_URL_API = "https://roads.googleapis.com/v1/nearestRoads?";
        private static final String GOOGLE_ROADS_API_KEY = "AIzaSyAwfTIhnQx7SuvmspQEjNgeelNyEU3Gw4w";
        private LatLng point;
        private String placeID;
        private float ratingValue;
        private Context cntx;

        public NearestRoad(Context cntx){
            this.cntx=cntx;
        }

        public NearestRoad(LatLng point, float ratingValue) {
            this.point = point;
            this.ratingValue = ratingValue;
        }

        private String createPlacesUrl(LatLng point) throws UnsupportedEncodingException {
            Log.d("Reached url","Reached url");
            StringBuilder encodedPoint = new StringBuilder();
            encodedPoint.append(point.latitude + "," + point.longitude);
            String pointUnrefined = encodedPoint.toString();
            return ROADS_URL_API + "points=" + pointUnrefined + "&key=" + GOOGLE_ROADS_API_KEY;
        }

        private class SendToServer extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                String link = params[0];
                try {
                    URL url = new URL(link);
                    InputStream is = url.openConnection().getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }

                    return buffer.toString();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String res){
                try {
                    JSONObject jsonData = new JSONObject(res);
                    Log.d("jsonData",jsonData.toString());
                    JSONArray jsonSnappedPoints = jsonData.getJSONArray("snappedPoints");
                    JSONObject jsonSnappedPoint = jsonSnappedPoints.getJSONObject(0);
                    placeID = jsonSnappedPoint.getString("placeId");
                    sendToServer(placeID, ratingValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        public void execute() throws UnsupportedEncodingException {
            String placesURL = createPlacesUrl(point);
            new SendToServer().execute(placesURL);
        }
        public void sendToServer(String placeId, float ratingValue) {
            MyDBHandler db = new MyDBHandler(ReviewActivity.this, null,
                    null, 1);
            db.addHandler(placeId, ratingValue);
            Log.d("readfromdb", Float.toString(db.loadHandler(placeId)));
        }
    }

    private GoogleMap mMap;
    private List<Marker> originMarkers = new ArrayList<>();
    private LatLng point = null;
    private RatingBar ratingBar;
    private float ratingValue;
    private Button btnSubmit;
    public PlaceAutocompleteFragment autocompleteFragmentS;
    private LatLng search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.review_map);
        mapFragment.getMapAsync(this);
        addListenerOnRatingBar();
        addListenerOnButton();
        autocompleteFragmentS = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.etSearch);
        autocompleteFragmentS.setHint("Enter address");
        autocompleteFragmentS.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                search = place.getLatLng();
                Log.i("Place", "Search: " + place.getName());
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(search);

                // Setting the title for the marker.
                // This will be displayed on taping the marker
                markerOptions.title(search.latitude + " : " + search.longitude);

                // Clears the previously touched position
                mMap.clear();

                // Animating to the touched position
                mMap.animateCamera(CameraUpdateFactory.newLatLng(search));

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);

                point = search;
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Place", "An error occurred: " + status);
            }
        });
    }
    public void addListenerOnRatingBar() {

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        //if rating value is changed,
        //display the current rating value in the result (textview) automatically
        ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ratingValue = rating;
            }
        });
    }

    public void addListenerOnButton() {
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        //if click on me, then display the current rating value.
        btnSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(point==null)
                {
                    Toast.makeText(ReviewActivity.this, "Long press on map to select location", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(ReviewActivity.this, "Rating Registered: " + Float.toString(ratingValue), Toast.LENGTH_SHORT).show();
                try {
                    NearestRoad nearestRoad = new NearestRoad(point, ratingValue);
                    nearestRoad.execute();
                    MyDBHandler dbHandler = new MyDBHandler(ReviewActivity.this, null,
                            null, 1);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(ReviewActivity.this, MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
//                finishActivity(ReviewActivity.this);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng hcmus = new LatLng(22.3185141, 87.2987007);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcmus, 18));
        originMarkers.add(mMap.addMarker(new MarkerOptions()
                .title("Azad Hall of Residence")
                .position(hcmus)));

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
        mMap.setOnMapLongClickListener(new OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng newPoint) {
                // Toast.makeText(ReviewActivity.this,point.latitude+" : "+point.longitude,Toast.LENGTH_SHORT).show();
                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(newPoint);

                // Setting the title for the marker.
                // This will be displayed on taping the marker
                markerOptions.title(newPoint.latitude + " : " + newPoint.longitude);

                // Clears the previously touched position
                mMap.clear();

                // Animating to the touched position
                mMap.animateCamera(CameraUpdateFactory.newLatLng(newPoint));

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);

                point = newPoint;
            }
        });
    }
}