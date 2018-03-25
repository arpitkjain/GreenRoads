package com.itshareplus.googlemapdemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputEditText;
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

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.view.View.OnClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Modules.MyDBHandler;
//import Modules.NearestRoad;
import com.microsoft.windowsazure.mobileservices.*;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOperations;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.*;
public class SafeActivity extends FragmentActivity implements OnMapReadyCallback {
//    public class NearestRoad {
//        private static final String ROADS_URL_API = "https://roads.googleapis.com/v1/nearestRoads?";
//        private static final String GOOGLE_ROADS_API_KEY = "AIzaSyAwfTIhnQx7SuvmspQEjNgeelNyEU3Gw4w";
//        private LatLng point;
//        private String placeID;
//        private float ratingValue;
//        private Context cntx;
//        private MobileServiceClient mClient;
//
//        public NearestRoad(Context cntx){
//            this.cntx=cntx;
//        }
//
//        public NearestRoad(LatLng point, float ratingValue) {
//            this.point = point;
//            this.ratingValue = ratingValue;
//        }
//
//        private String createPlacesUrl(LatLng point) throws UnsupportedEncodingException {
//            Log.d("Reached url","Reached url");
//            StringBuilder encodedPoint = new StringBuilder();
//            encodedPoint.append(point.latitude + "," + point.longitude);
//            String pointUnrefined = encodedPoint.toString();
//            return ROADS_URL_API + "points=" + pointUnrefined + "&key=" + GOOGLE_ROADS_API_KEY;
//        }
//
//        private class SendToServer extends AsyncTask<String, Void, String> {
//
//            @Override
//            protected String doInBackground(String... params) {
//                String link = params[0];
//                try {
//                    URL url = new URL(link);
//                    InputStream is = url.openConnection().getInputStream();
//                    StringBuffer buffer = new StringBuffer();
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        buffer.append(line + "\n");
//                    }
//
//                    return buffer.toString();
//
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(String res){
//                try {
//                    JSONObject jsonData = new JSONObject(res);
//                    Log.d("jsonData",jsonData.toString());
//                    JSONArray jsonSnappedPoints = jsonData.getJSONArray("snappedPoints");
//                    JSONObject jsonSnappedPoint = jsonSnappedPoints.getJSONObject(0);
//                    placeID = jsonSnappedPoint.getString("placeId");
//                    sendToServer(placeID, ratingValue);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        public void execute() throws UnsupportedEncodingException {
//            String placesURL = createPlacesUrl(point);
//            new SendToServer().execute(placesURL);
//        }
//        public void sendToServer(String placeId, float ratingValue) {
//            MyDBHandler db = new MyDBHandler(SafeActivity.this, null,
//                    null, 1);
//            db.addHandler(placeId, ratingValue);
//            Log.d("readfromdb", Float.toString(db.loadHandler(placeId)));
//        }
//    }

    private GoogleMap mMap;
    private List<Marker> originMarkers = new ArrayList<>();
    private LatLng point = null;
    private RatingBar ratingBar;
    private float ratingValue;
    private Button btnSubmit;
    public PlaceAutocompleteFragment autocompleteFragmentS;
    private LatLng search;
    private MobileServiceClient mClient;
    BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        try {
//            mClient = new MobileServiceClient(
//                    "https://greenroads.azurewebsites.net",
//                    this
//            );
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }

        bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        setContentView(R.layout.activity_safe);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.safe_map);
        mapFragment.getMapAsync(this);
        autocompleteFragmentS = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.etSafe);
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
                bottomSheetDialog.show();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Place", "An error occurred: " + status);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
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
//
//        }
        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            mMap.setMyLocationEnabled(true);
        }
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
//                Log.d("penis","penis");

                point = newPoint;
                TextView Too = (TextView) bottomSheetDialog.findViewById(R.id.textoo);
                Too.setText("You are safe!");
                bottomSheetDialog.show();

//                Toast.makeText(SafeActivity.this,point.latitude+" : "+point.longitude,Toast.LENGTH_SHORT).show();
//
//                BottomSheetDialog bsd = new BottomSheetDialog(SafeActivity.this);
//                View bsv = getLayoutInflater().inflate(R.layout.bottom_sheet,null);
            }
        });
//        BottomSheetBehavior behavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
//        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                if (newState == BottomSheetBehavior.STATE_HIDDEN){
//                    Toast.makeText(SafeActivity.this,"Arpit",Toast.LENGTH_SHORT).show();
//                }
//            }

        }
}