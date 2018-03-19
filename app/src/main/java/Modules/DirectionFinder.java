package Modules;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.itshareplus.googlemapdemo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.lang.StringBuilder;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.util.AsyncListUtil;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itshareplus.googlemapdemo.ReviewActivity;

import static com.google.android.gms.internal.zzir.runOnUiThread;
import static java.lang.Math.floor;
import static java.lang.Math.random;

/**
 * Created by Mai Thanh Hiep on 4/3/2016.
 */
public class DirectionFinder {
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyBRlTaNk61pKHLzUSPBd4oAzzCkjakx-DA";
    private static final String ROADS_URL_API = "https://roads.googleapis.com/v1/snapToRoads?";
    private static final String GOOGLE_ROADS_API_KEY = "AIzaSyAQ02l5cu_T5ve5FWSg59vx6qcR5P6Mod0";
    private DirectionFinderListener listener;
    private LatLng origin;
    private LatLng destination;
    private Context mContext;
    private List<LatLng> refinedPoly;

    public DirectionFinder(DirectionFinderListener listener, LatLng origin, LatLng destination, Context mContext) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
        this.mContext = mContext;
        refinedPoly = new ArrayList<LatLng>();
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onDirectionFinderStart();
        new DownloadRawData().execute(createUrl());
    }

    private String createUrl() throws UnsupportedEncodingException {
        //String urlOrigin = URLEncoder.encode(origin, "utf-8");
        //String urlDestination = URLEncoder.encode(destination, "utf-8");

        return DIRECTION_URL_API + "origin=" + origin.latitude+","+origin.longitude + "&destination=" + destination.latitude+","+destination.longitude  + "&alternatives=true&key=" + GOOGLE_API_KEY;
    }

    private String createPlacesUrl(List<LatLng> points) {

        Log.d("Reached Places url","Reached Places url");
        StringBuilder encodedPoint = new StringBuilder();
        for(int i=0; i<points.size(); i++)
        {
            encodedPoint.append(points.get(i).latitude + "," + points.get(i).longitude);
            if(i!=points.size()-1) {
                encodedPoint.append("|");
            }
        }
        String pathUnrefined = encodedPoint.toString();
        return ROADS_URL_API + "path=" + pathUnrefined + "&key=" + GOOGLE_ROADS_API_KEY;
    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }

    private class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);

                Log.i("JSON URL", url.toString());
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

                Log.i("Place JSON", res);
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch(UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }
    }

    // TODO: REMOVE THIS
    private class SnapDownloadRawData extends AsyncTask<String, Void, String> {

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

    }

    private void parseJSon(String data) throws JSONException, UnsupportedEncodingException{
        if (data == null)
            return;

        List<Route> routes = new ArrayList<Route>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");

        Log.d("Reached parseJSON","Reached parseJSON");
        for (int i = 0; i < jsonRoutes.length(); i++) {

            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route route = new Route();

            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

            route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
            route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
            route.endAddress = jsonLeg.getString("end_address");
            route.startAddress = jsonLeg.getString("start_address");
            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.points = decodePolyLine(overview_polylineJson.getString("points"));
            routes.add(route);
        }
        snapAndDisplayPoints(routes);
    }

    private class GetJSONTask extends AsyncTask<List<Route>, Void, List<Route>> {

        protected List<Route> doInBackground(List<Route>... params) {
            // Creating new JSON Parser
            List<Route> routes = params[0];
            int i =0;
            try {

                for(Route route: routes)
                {
                    URL url = new URL(createPlacesUrl(route.points));
                    InputStream is = url.openConnection().getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }

                    route.jsonRAW = (buffer.toString());
                    i++;
                }
                return routes;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }
        protected void onPostExecute(List<Route> routes) {
            int flag = 1;
            if(routes==null)
                return;
            try {
            for(Route route : routes)
            {
                    JSONObject jsonData = new JSONObject(route.jsonRAW);
                    JSONArray jsonSnappedPoints = jsonData.getJSONArray("snappedPoints");
                    route.points.clear();
                    route.placeIds = new ArrayList<>();
                    if(jsonSnappedPoints==null)
                    {
                        Log.d("TooMuchData","TooMuchData");
                        throw new TooMuchData("x");
                    }
                    for (int i = 0; i < jsonSnappedPoints.length(); i++) {
                        JSONObject jsonSnappedPoint = jsonSnappedPoints.getJSONObject(i);
                        JSONObject location = jsonSnappedPoint.getJSONObject("location");
                        LatLng snappedPoint = new LatLng(location.getDouble("latitude"), location.getDouble("longitude"));
                        String snappedPointplaceID = jsonSnappedPoint.getString("placeId");
                        route.points.add(snappedPoint);
                        route.placeIds.add(snappedPointplaceID);
                    }
                Log.i("Route", route.startAddress);
                route.rating = rater(route.placeIds);
            }

                listener.onDirectionFinderSuccess(routes);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            catch(NullPointerException e) {
                Toast.makeText(mContext,"Please enter nearer places",Toast.LENGTH_SHORT).show();
                flag = 0;
            }
            catch(TooMuchData e) {
                Toast.makeText(mContext,"Please enter nearer places",Toast.LENGTH_SHORT).show();
                flag = 0;
            }
        }
    }

    public void snapAndDisplayPoints(List<Route> routes) {
        Log.i("Reached snapAndDisp", "Reached snapAndDisp ");
        new GetJSONTask().execute(routes);
/*
        for(Route route:routes)
        {
            new SnapToRoad(route.points, new Handler(){

                List<LatLng> responseList;
                @Override
                public void handleMessage(Message message){
                    String response = message.getData().getString("response");
                    JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
                    JsonArray jsonArray = jsonObject.getAsJsonArray("snappedPoints");
                    for(JsonElement jsonObj : jsonArray){

                        JsonObject locationData = jsonObj.getAsJsonObject();
                        double latitude = locationData.get("location").getAsJsonObject().get("latitude").getAsDouble();
                        double longitude = locationData.get("location").getAsJsonObject().get("longitude").getAsDouble();
                        responseList.add(new LatLng(latitude, longitude));
                    }
                    route.points =
                    List<Route> routes = new ArrayList<Route>();
                    routes.add(route);
                    listener.onDirectionFinderSuccess(routes);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run(){
                            partPath = new PolylineOptions().color(getResources().getColor(R.color.colorAccent)).width(15.0f);
                            partPath.addAll(responseList);
                            map.addPolyline(partPath);
                        }
                    });
                }
            }).start();

        }
*/    }

    public class SnapToRoad extends Thread {

        private boolean isUnexecutable = false;

        private Handler mHandler;

        private List<LatLng> resultData;

        private final String API_KEY = "AIzaSyAQ02l5cu_T5ve5FWSg59vx6qcR5P6Mod0";
        private final String TAG = SnapToRoad.class.getSimpleName();

        private final int CUTTING_POINT = 1;
        private final int READ_TIME_OUT = 10000;
        private final int CONNECT_TIME_OUT = 15000;

        private String tempURL;
        private String FOOTER = "&interpolate=true&key=" + API_KEY;

        public SnapToRoad(final List<LatLng> coordList, final Handler mHandler){
            if(coordList.size() < 2) isUnexecutable = true;
            resultData = new ArrayList<>();
            this.mHandler = mHandler;
            tempURL = "https://roads.googleapis.com/v1/snapToRoads?path=";
            for(LatLng latLng : coordList) tempURL += latLng.latitude + "," + latLng.longitude + "|";
            tempURL = tempURL.substring(0, tempURL.length() - CUTTING_POINT) + FOOTER;
            Log.e("Composite URL", tempURL);
        }

        private String ReadHTML(String address, int timeout) {
            String html = new String();
            try {
                URL url = new URL(address);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if (urlConnection != null) {
                    urlConnection.setConnectTimeout(timeout);
                    urlConnection.setUseCaches(false);
                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        while (true) {
                            String buf = br.readLine();
                            if (buf == null)
                                break;
                            html += buf;
                        }
                        br.close();
                        urlConnection.disconnect();
                    } else return null;
                } else return null;
            } catch (Exception ex) {return null;}
            return html;
        }

        @Override
        public void run(){
            if(isUnexecutable) return;
            String jsonString = ReadHTML(tempURL, 2000);
            if (jsonString == null) {
                jsonString = "";
            }
            Message msg = mHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("response", jsonString);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }

    }

    // Add this in the end
    //    listener.onDirectionFinderSuccess(routes);
    // snapPoints(decodePolyLine(overview_polylineJson.getString("points")));
    // route.points = refinedPoly;

    private double rater(List<String> placeIds) {
        int[] numbers ={0,0};
        double total = 0;
        Set<String> hashedPIDS = new HashSet<String>(placeIds);
        Iterator<String> itr = hashedPIDS.iterator();
        while(itr.hasNext()) {
            String placeId = itr.next();
            double rating = retrieveRating(placeId);
            if(rating<=2) {
                total += 1 * rating;
                numbers[1]++;
            }
            else{
                total += 1 * rating;
                numbers[0]++;
            }

        }
        Log.d("Total",""+total);

        double finalRating = total/(numbers[0]+numbers[1]);
        return finalRating;
    }
    private double retrieveRating(String placeId)
    {
        MyDBHandler db = new MyDBHandler(mContext, null,
                null, 1);
        double response = db.loadHandler(placeId);
        if(response<0)
            return 3;
        else
            return response;
        //Random num = new Random();
        //int showme = num.nextInt(6);
        //Log.d("Random number",""+showme);
        //return showme;
    }

    /*
    private void snapPoints(List<LatLng> points) {
        final String mURL = createPlacesUrl(points);
//        useData(points);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, mURL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject jsonData) {

                        try {
                            Log.d("jsonData",jsonData.toString());
                            JSONArray jsonSnappedPoints = jsonData.getJSONArray("snappedPoints");
                            for (int i = 0; i < jsonSnappedPoints.length(); i++) {
                                JSONObject jsonSnappedPoint = jsonSnappedPoints.getJSONObject(i);
                                JSONObject location = jsonSnappedPoint.getJSONObject("location");
                                LatLng snappedPoint = new LatLng(location.getDouble("latitude"), location.getDouble("longitude"));
                                if(i<10)
                                    Log.d("qqqq Snap Point", snappedPoint.toString());
                                refinedPoly.add(snappedPoint);
                                Log.d("Refined PolyLine", refinedPoly.toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError e) {
                        e.printStackTrace();
                    }
                });

        Log.d("Roads API URL", mURL);
        // Adding request to request queue
        VolleyController.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
        try
        {

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        Log.d("Refined PolyLine", refinedPoly.toString());
//
        //
        //try {
        //URL url = new URL(link);
        //InputStream is = url.openConnection().getInputStream();
        //StringBuffer buffer = new StringBuffer();
        //BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        //
        //String line;
        //while ((line = reader.readLine()) != null) {
        //buffer.append(line + "\n");
        //}
        //
        //return buffer.toString();
        //
        //} catch (MalformedURLException e) {
        //e.printStackTrace();
        //} catch (IOException e) {
        //e.printStackTrace();
        //}
        //return null;
        //
        //new SnapDownloadRawData(){
        //protected void onPostExecute(String res) {
        //try {
        //JSONObject jsonData = new JSONObject(res);
        //JSONArray jsonSnappedPoints = jsonData.getJSONArray("snappedPoints");
        //for (int i = 0; i < jsonSnappedPoints.length(); i++) {
        //JSONObject jsonSnappedPoint = jsonSnappedPoints.getJSONObject(i);
        //JSONObject location = jsonSnappedPoint.getJSONObject("location");
        //LatLng snappedPoint = new LatLng(location.getDouble("latitude"), location.getDouble("longitude"));
        //refined.add(snappedPoint);
        //}
        //}catch (JSONException e) {
        //e.printStackTrace();
        //}
        //}
        //}.execute(createPlacesUrl(points));
        //
    }
    */

    /*
    public interface DataCallback {
        void onSuccess(JSONObject result);
    }
    public void useData(List<LatLng> points) {
        fetchData(points, new DataCallback() {
            @Override
            public void onSuccess(JSONObject jsonData) {
                try {
                    Log.d("jsonData",jsonData.toString());
                    JSONArray jsonSnappedPoints = jsonData.getJSONArray("snappedPoints");
                    for (int i = 0; i < jsonSnappedPoints.length(); i++) {
                        JSONObject jsonSnappedPoint = jsonSnappedPoints.getJSONObject(i);
                        JSONObject location = jsonSnappedPoint.getJSONObject("location");
                        LatLng snappedPoint = new LatLng(location.getDouble("latitude"), location.getDouble("longitude"));
                        if (i < 10)
                            Log.d("qqqq Snap Point", snappedPoint.toString());
                        refinedPoly.add(snappedPoint);
                    }
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void fetchData(List<LatLng> points, final DataCallback callback) {
        String url = createPlacesUrl(points);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("penis1", response.toString());

                            callback.onSuccess(response);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d("penis3", "Error: " + error.getMessage());
                    }
                });
        VolleyController.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
    }
    */
}

