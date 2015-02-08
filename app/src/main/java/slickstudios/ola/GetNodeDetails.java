package slickstudios.ola;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SeekBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class GetNodeDetails extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String LOG_TAG ="GetNode" ;
    private Toolbar toolbar;
    private Button bTime,bCancel,bDone;
    private EditText etName,etHrs,etMins;
    private AutoCompleteTextView atvLocation;
    private SeekBar sPriority;

    private String sName,sTime,sLocation;
    private int progress,hours,mins;
    private double lat,lng;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyAcYvLO9N57HETwWosP9xbipwNUDh-7HFQ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_node_details);
        initVars();
    }

    private void initVars() {
        toolbar=(Toolbar)findViewById(R.id.toolbar_node);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bTime=(Button)findViewById(R.id.bNodeTimePicker);
        bCancel=(Button)findViewById(R.id.buttonNotDone);
        bDone=(Button)findViewById(R.id.buttonDone);

        bTime.setOnClickListener(this);
        bCancel.setOnClickListener(this);
        bDone.setOnClickListener(this);

        etName=(EditText)findViewById(R.id.eTNodeName);
        etHrs=(EditText)findViewById(R.id.eTNodeHour);
        etMins=(EditText)findViewById(R.id.eTNodeMinutes);

        sPriority=(SeekBar)findViewById(R.id.seekBarPriority);

        atvLocation=(AutoCompleteTextView)findViewById(R.id.atvNodeLocation);
        atvLocation.setAdapter(new PlacesAutoCompleteAdapter(new ContextThemeWrapper(this,R.style.Theme_AppCompat_Light_DarkActionBar), android.R.layout.simple_list_item_1));
        atvLocation.setOnItemClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bNodeTimePicker:
                launchTime();
                break;
            case R.id.buttonDone:
                checkAndExit();
                break;
            case R.id.buttonNotDone:
                finish();
                break;
        }
    }

    private void checkAndExit() {
        populate();
        if(sName.contentEquals("")||sLocation.contentEquals("")||(hours==0&&mins==0)||sName==null||sLocation==null){
            Toast.makeText(this,"Please fill all the fields",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this,sName,Toast.LENGTH_SHORT).show();
            Intent back=new Intent();
            Bundle backpack=new Bundle();
            backpack.putString("name",sName);
            backpack.putString("location",sLocation);
            backpack.putString("duration",sTime);
            backpack.putInt("hours",hours);
            backpack.putInt("minutes",mins);
            backpack.putDouble("latitude",lat);
            backpack.putDouble("longitude",lng);
            backpack.putInt("priority",progress);
            back.putExtras(backpack);
            setResult(RESULT_OK,back);
            finish();
        }
    }

    private void populate() {
        sName=etName.getText().toString();
        sLocation=atvLocation.getText().toString();
        progress=sPriority.getProgress();
        if(!etHrs.getText().toString().contentEquals(""))
        hours= Integer.parseInt(etHrs.getText().toString());
        else
        hours=0;
        if(!etMins.getText().toString().contentEquals(""))
        mins= Integer.parseInt(etMins.getText().toString());
        else
        mins=0;
    }


    private void launchTime() {
        // TODO Auto-generated method stub
        Calendar mcurrentTime = Calendar.getInstance();
        int mHour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int mMinute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog tpd = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        String t=hourOfDay + ":" + minute;
                        bTime.setText(t);
                        sTime=t;
                    }
                }, mHour, mMinute, false);
        tpd.show();
    }

    private ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:in");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String location= (String) parent.getItemAtPosition(position);
        atvLocation.setText(location);
        getLatLongFromPlace(location);

    }

    private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

        private ArrayList<String> resultList;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected Filter.FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};
            return filter;
        }



    }

 public void getLatLongFromPlace(String place) {
        try {
            Geocoder selected_place_geocoder = new Geocoder(this);
            List<Address> address;

            address = selected_place_geocoder.getFromLocationName(place, 5);

            if (address != null){
                Address location = address.get(0);
                lat= location.getLatitude();
                lng = location.getLongitude();
            }

        } catch (Exception e) {
            e.printStackTrace();
            fetchLatLongFromService fetch_latlng_from_service_abc = new fetchLatLongFromService(
                    place.replaceAll("\\s+", ""));
            fetch_latlng_from_service_abc.execute();

        }

    }

    public class fetchLatLongFromService extends
            AsyncTask<Void, Void, StringBuilder> {
        String place;


        public fetchLatLongFromService(String place) {
            super();
            this.place = place;
            this.place=this.place.replaceAll(" ","%20");
            this.place=this.place.replaceAll(",","%2C");


        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
            this.cancel(true);
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try {
                HttpURLConnection conn = null;
                StringBuilder jsonResults = new StringBuilder();
                String googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json?address="
                        + this.place + "&sensor=false";

                URL url = new URL(googleMapUrl);
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(
                        conn.getInputStream());
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
                String a = "";
                return jsonResults;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(StringBuilder result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            try {
                JSONObject jsonObj = new JSONObject(result.toString());
                JSONArray resultJsonArray = jsonObj.getJSONArray("results");

                // Extract the Place descriptions from the results
                // resultList = new ArrayList<String>(resultJsonArray.length());

                JSONObject before_geometry_jsonObj = resultJsonArray
                        .getJSONObject(0);

                JSONObject geometry_jsonObj = before_geometry_jsonObj
                        .getJSONObject("geometry");

                JSONObject location_jsonObj = geometry_jsonObj
                        .getJSONObject("location");

                String lat_helper = location_jsonObj.getString("lat");
                lat = Double.valueOf(lat_helper);


                String lng_helper = location_jsonObj.getString("lng");
                lng = Double.valueOf(lng_helper);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }
        }
    }


}
