package com.dangchienhsgs.giffus.map;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.dangchienhsgs.giffus.R;
import com.dangchienhsgs.giffus.server.ServerUtilities;
import com.dangchienhsgs.giffus.utils.URLContentHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class AddLocationActivity extends Activity implements AdapterView.OnItemClickListener,
        GoogleMap.OnMarkerClickListener {

    private String TAG = "Add Location Activity";

    private AutoCompleteTextView searchBox;

    private PlacesAutoCompleteAdapter mAdapter;

    private GoogleMap googleMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        searchBox = (AutoCompleteTextView) findViewById(R.id.edit_text_auto_complete);

        mAdapter = new PlacesAutoCompleteAdapter(
                this,
                android.R.layout.simple_list_item_1
        );
        searchBox.setAdapter(mAdapter);
        searchBox.setOnItemClickListener(this);

        try {
            initizeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment))
                    .getMap();

            googleMap.setOnMarkerClickListener(this);

            if (googleMap == null) {
                Toast.makeText(this, "Unnable to create Map", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String title = mAdapter.getItem(i);
        String ref = mAdapter.getReference(i);
        new DownloadPlaceInformation(title, ref).execute();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_SHORT).show();
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
        return true;
    }

    private class DownloadPlaceInformation extends AsyncTask<String, Void, LatLng> {
        private String description;
        private String refs;

        private DownloadPlaceInformation(String description, String refs) {
            this.description = description;
            this.refs = refs;
        }

        @Override
        protected LatLng doInBackground(String... strings) {
            String url = MapServer.getPlaceDetailUrl(refs);
            Log.d(TAG, url);
            String jsonResults = URLContentHandler.getURLContent(url);
            Log.d(TAG, jsonResults);
            try {
                JSONObject places = new JSONObject(jsonResults);
                JSONObject result = places.getJSONObject("result");
                JSONObject geometry = result.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");

                Log.d(TAG, location.toString());

                LatLng latLng = new LatLng(
                        Double.parseDouble(location.getString("lat")),
                        Double.parseDouble(location.getString("lng"))
                );

                return latLng;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "JSON Exception");
                return null;
            }
        }

        @Override
        protected void onPostExecute(LatLng result) {
            MarkerOptions markerOptions = new MarkerOptions().position(result).title(description);
            googleMap.addMarker(markerOptions);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    result).zoom(12).build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }
    }
}
