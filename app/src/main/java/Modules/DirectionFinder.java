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
import com.itshareplus.googlemapdemo.ToDoItem;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import static com.google.android.gms.internal.zzir.runOnUiThread;
import static java.lang.Math.floor;
import static java.lang.Math.random;

/**
 * Created by Team Schwifty
 */
public class DirectionFinder {
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyBRlTaNk61pKHLzUSPBd4oAzzCkjakx-DA";
    private static final String ROADS_URL_API = "https://roads.googleapis.com/v1/snapToRoads?interpolate=true&";
    private static final String GOOGLE_ROADS_API_KEY = "AIzaSyAQ02l5cu_T5ve5FWSg59vx6qcR5P6Mod0";
    private DirectionFinderListener listener;
    private LatLng origin;
    private LatLng destination;
    private Context mContext;
    private List<LatLng> refinedPoly;
    private MobileServiceClient mClient;
    private MobileServiceTable<ToDoItem> mToDoTable;

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

    public DirectionFinder(DirectionFinderListener listener, LatLng origin, LatLng destination, Context mContext, MobileServiceClient mClient, MobileServiceTable<ToDoItem> mToDoTable) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
        this.mContext = mContext;
        refinedPoly = new ArrayList<LatLng>();
        this.mClient = mClient;
        this.mToDoTable = mToDoTable;
    }

    // execute -> DownloadUnrefinedRoads -> parseJSON -> MultiDownloadRefinedRoads -> rater -> retrieveRating
    //                                    > decodePolyline        |             for each route
    //                                                            |
    //                                                            -> DirectionFinderListener::DirectionFinderSuccess(routes)

    public void execute() throws UnsupportedEncodingException {
        listener.onDirectionFinderStart();
        new DownloadUnrefinedRoads().execute(createUrl());
    }

    // Uses DirectionsAPI to get routes between origin and destination
    private class DownloadUnrefinedRoads extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);

                Log.i("DUR Background", url.toString());
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

                Log.i("DUR Post", res);
                parseJSON(res);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch(UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }
    }

    // Creates all the unrefined routes from the JSON, collectively sends to snapAndDisplayPoints (GetJSONTask)
    private void parseJSON(String data) throws JSONException, UnsupportedEncodingException{
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
            //route.endAddress = jsonLeg.getString("end_address");
            //route.startAddress = jsonLeg.getString("start_address");
            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.points = decodePolyLine(overview_polylineJson.getString("points"));
            routes.add(route);
        }
        new MultiDownloadRefinedRoads().execute(routes);
    }

    // Puts raw JSON data of snapped points into each route in background, in post it parses the JSON, assigns rating to each route structure and collectively calls for plotting them
    private class MultiDownloadRefinedRoads extends AsyncTask<List<Route>, Void, List<Route>> {

        protected List<Route> doInBackground(List<Route>... params) {
            List<Route> routes = params[0];
            try {

                for(Route route: routes)
                {
                    route.jsonRAWArray = new ArrayList<>();
                    int PAGINATION_OVERLAP = 3, PAGE_SIZE_LIMIT = 92;
                    int offset = 0;
                    while (offset < route.points.size()) {
                        // Calculate which points to include in this request. We can't exceed the API's
                        // maximum and we want to ensure some overlap so the API can infer a good location for
                        // the first few points in each request.
                        if (offset > 0) {
                            offset -= PAGINATION_OVERLAP;   // Rewind to include some previous points.
                        }
                        int lowerBound = offset;
                        int upperBound = Math.min(offset + PAGE_SIZE_LIMIT, route.points.size());

                        // Get the data we need for this page.
                        List<LatLng> page = route.points.subList(lowerBound, upperBound);

                        URL url = new URL(createPlacesUrl(page));

                        InputStream is = url.openConnection().getInputStream();
                        StringBuffer buffer = new StringBuffer();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            buffer.append(line + "\n");
                        }

                        route.jsonRAWArray.add(buffer.toString());
                        offset = upperBound;
                    }
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
            int PAGINATION_OVERLAP = 3, PAGE_SIZE_LIMIT = 92;
            if(routes==null)
                return;
            try {
                for(Route route : routes) {
                    int size = route.points.size();
                    route.points.clear();
                    route.placeIds = new ArrayList<>();

                    int offset = 0;
                    int idx = 0;
                    while (offset < size) {
                        // Calculate which points to include in this request. We can't exceed the API's
                        // maximum and we want to ensure some overlap so the API can infer a good location for
                        // the first few points in each request.
                        if (offset > 0) {
                            offset -= PAGINATION_OVERLAP;   // Rewind to include some previous points.
                        }
                        int lowerBound = offset;
                        int upperBound = Math.min(offset + PAGE_SIZE_LIMIT, size);

                        JSONObject jsonData = new JSONObject(route.jsonRAWArray.get(idx));
                        JSONArray jsonSnappedPoints = jsonData.getJSONArray("snappedPoints");

                        if(jsonSnappedPoints==null) {
                            Log.d("DRR Post","TooMuchData");
                            throw new TooMuchData("x");
                        }

                        boolean passedOverlap = false;
                        for (int i = 0; i < jsonSnappedPoints.length(); i++) {
                            JSONObject jsonSnappedPoint = jsonSnappedPoints.getJSONObject(i);
                            JSONObject location = jsonSnappedPoint.getJSONObject("location");
                            LatLng snappedPoint = new LatLng(location.getDouble("latitude"), location.getDouble("longitude"));
                            String snappedPointPlaceID = jsonSnappedPoint.getString("placeId");
                            if (offset == 0 || i >= PAGINATION_OVERLAP - 1) {
                                passedOverlap = true;
                            }
                            if (passedOverlap) {
                                route.points.add(snappedPoint);
                                route.placeIds.add(snappedPointPlaceID);
                            }
                        }
                        idx++;
                        offset = upperBound;
                    }

                    //Log.i("DRR Route", route.startAddress);
                    //route.rating = rater(route.placeIds);                   /*SERIAL*/
                }
                new DownloadDatabase().execute(routes);
                //rater(routes);
                //listener.onDirectionFinderSuccess(routes);
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

    private class DownloadDatabase extends AsyncTask<List<Route>, Void, Temp> {

        protected Temp doInBackground(List<Route>... params) {
            List<Route> routes = params[0];
            List<ToDoItem> allEntries = null;
            try {
                Log.d("Before dbms request","X");
                allEntries = getAllFromTable();
                Log.d("After dbms request","Y");
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                Log.d("During dbms request","Z");
                e.printStackTrace();
            }
            return new Temp(routes,allEntries);
        }

        protected void onPostExecute(Temp temp) {
            rater(temp.routes,temp.allEntries);
        }
    }

    //returns rating for a single route
    private void rater(List<Route> routes, List<ToDoItem> allEntries) {
        Log.d("allEntries",allEntries+"");
        for(Route route: routes) {
            int[] numbers ={0,0};
            double total = 0;
            Set<String> hashedPIDS = new HashSet<String>(route.placeIds);
            Iterator<String> itr = hashedPIDS.iterator();
            while(itr.hasNext()) {
                String placeId = itr.next();
                double rating = retrieveRating(placeId, allEntries);
                if(rating<=2) {
                    total += 1 * rating;
                    numbers[1]++;
                }
                else{
                    total += 1 * rating;
                    numbers[0]++;
                }

            }
            double finalRating = total/(numbers[0]+numbers[1]);
            Log.d("finalRating",""+finalRating);
            route.rating = finalRating;
        }
        listener.onDirectionFinderSuccess(routes);
    }

    // Retrieve rating from the database
    private double retrieveRating(String placeId, List<ToDoItem> allEntries) {
        double response=3;
        //MyDBHandler db = new MyDBHandler(mContext, null,null, 1);
        //response = db.loadHandler(placeId);
        if(response<0)
            return 3;
        else
            return response;
        //Random num = new Random();
        //int showme = num.nextInt(6);
        //Log.d("Random number",""+showme);
        //return showme;
    }

















    public void addItem(final ToDoItem item) {
        if (mClient == null) {
            return;
        }

        // Create a new item
//        final ToDoItem item = new ToDoItem();

//        item.setmPlaceId("chandigarh");
//        item.setmRating("2");
//        item.setmId("idkman");

        // Insert the new item
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final ToDoItem entity = addItemInTable(item);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(true){
//                                mAdapter.add(entity);
                            }
                        }
                    });
                } catch (final Exception e) {
//                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

//        runAsyncTask(task);
        task.execute();
//        mTextNewToDo.setText("");
    }

    /**
     * Add an item to the Mobile Service Table
     *
     * @param item
     *            The item to Add
     */
    public ToDoItem addItemInTable(ToDoItem item) throws ExecutionException, InterruptedException {
        ToDoItem entity = mToDoTable.insert(item).get();
        return entity;
    }

    public void updateItem(final ToDoItem item) {
        if (mClient == null) {
            return;
        }

        // Create a new item
//        final ToDoItem item = new ToDoItem();

//        item.setmPlaceId("chandigarh");
//        item.setmRating("2");
//        item.setmId("idkman");

        // Insert the new item
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final ToDoItem entity = updateItemInTable(item);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(true){
//                                mAdapter.add(entity);
                            }
                        }
                    });
                } catch (final Exception e) {
//                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

//        runAsyncTask(task);
        task.execute();
//        mTextNewToDo.setText("");
    }

    /**
     * Add an item to the Mobile Service Table
     *
     * @param item
     *            The item to Add
     */
    public ToDoItem updateItemInTable(ToDoItem item) throws ExecutionException, InterruptedException {
        ToDoItem entity = mToDoTable.update(item).get();
        return entity;
    }


    private void getAllItems() {

        // Get the items that weren't marked as completed and add them in the
        // adapter

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<ToDoItem> results = getAllFromTable();

                    //Offline Sync
                    //final List<ToDoItem> results = refreshItemsFromMobileServiceTableSyncTable();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            mAdapter.clear();

//                            for (ToDoItem item : results) {
//                                mAdapter.add(item);
//                            }
                        }
                    });
                } catch (final Exception e){
//                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        task.execute();
    }

    /**
     * Refresh the list with the items in the Mobile Service Table
     */

    private List<ToDoItem> getAllFromTable() throws ExecutionException, InterruptedException, TimeoutException {
        try {
            return mToDoTable.execute().get(5000, TimeUnit.MILLISECONDS);
        } catch (MobileServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ToDoItem getIdFromTable(String placeId) {
        try {
            return mToDoTable.lookUp(placeId).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

}

class Temp {
    List<Route> routes;
    List<ToDoItem> allEntries;
    public Temp(List<Route> routes, List<ToDoItem> allEntries) {
        this.routes = routes;
        this.allEntries = allEntries;
    }
}