package Modules;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.lang.StringBuilder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.util.Log;
import android.widget.Toast;

public class NearestRoad {
    private static final String ROADS_URL_API = "https://roads.googleapis.com/v1/nearestRoads?";
    private static final String GOOGLE_ROADS_API_KEY = "AIzaSyAwfTIhnQx7SuvmspQEjNgeelNyEU3Gw4w";
    private LatLng point;
    private String placeID;
    private float ratingValue;

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

    }
}


