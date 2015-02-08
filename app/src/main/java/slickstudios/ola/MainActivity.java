package slickstudios.ola;

import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements View.OnClickListener, GoogleMap.OnMarkerClickListener {

    private Toolbar toolbar;
    private ImageButton fab_add,fab_loc;
    private GoogleMap map;
    private Marker currentMarker=null,nodeMarker;
    private static final int REQUEST_CODE=1512;
    private int count=0;
    private List<Node> nodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initStuffs();
    }

    private void initStuffs() {
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        map=((MapFragment)getFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setOnMarkerClickListener(this);

        fab_add=(ImageButton)findViewById(R.id.fab_add);
        fab_loc=(ImageButton)findViewById(R.id.fab_location);
        fab_loc.setOnClickListener(this);
        fab_add.setOnClickListener(this);

        nodes=new ArrayList<>();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK&&requestCode==REQUEST_CODE){
            Bundle bundle=data.getExtras();
            Node node=new Node();
            node.name=bundle.getString("name");
            node.location=bundle.getString("location");
            node.duration=bundle.getString("duration");
            node.hours=bundle.getInt("hours");
            node.mins=bundle.getInt("minutes");
            node.priority=bundle.getInt("priority");
            node.lat=bundle.getDouble("latitude");
            node.lng=bundle.getDouble("longitude");
            node.id=++count;
            nodes.add(node);
            markMap(node.lat,node.lng,node.priority,node.name);
        }
    }

    private void markMap(double lat, double lng, int priority, String name) {
        float color=BitmapDescriptorFactory.HUE_YELLOW;
        switch(priority){
            case 0:
                color=BitmapDescriptorFactory.HUE_AZURE;
                break;
            case 1:
                color=BitmapDescriptorFactory.HUE_BLUE;
                break;
            case 2:
                color=BitmapDescriptorFactory.HUE_YELLOW;
                break;
            case 3:
                color=BitmapDescriptorFactory.HUE_ORANGE;
                break;
            case 4:
                color=BitmapDescriptorFactory.HUE_RED;
                break;
        }
        nodeMarker=map.addMarker(new MarkerOptions()
        .title(name)
        .position(new LatLng(lat,lng))
        .icon(BitmapDescriptorFactory.defaultMarker(color)));
        zoomTo(lat, lng);
    }

    private void zoomTo(double lat, double lng) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),10));

        map.animateCamera(CameraUpdateFactory.zoomTo(15 ),2000,null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_add:
                Intent i=new Intent(this,GetNodeDetails.class);
                startActivityForResult(i,REQUEST_CODE);
                break;
            case R.id.fab_location:
                moveToCurrent();
                break;
        }
    }

    private void moveToCurrent() {
        Location location = map.getMyLocation();
        LatLng myLocation;
        if(location!=null){
            myLocation=new LatLng(location.getLatitude(),location.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        LatLng latlng=marker.getPosition();
        moveTo(latlng.latitude, latlng.longitude);
        return true;
    }

    private void moveTo(double latitude, double longitude) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
    }

    private class Node{
        public String name,location,duration;
        public int hours,mins,priority,id;
        public double lat,lng;
    }
}
