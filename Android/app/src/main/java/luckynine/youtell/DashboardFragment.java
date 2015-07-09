package luckynine.youtell;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import luckynine.youtell.data.DataContract;
import luckynine.youtell.data.PostLocation;


public class DashboardFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,LocationListener {

    private final String LOG_TAG = DashboardFragment.class.getSimpleName();

    private static final int POST_LOADER_ID = 0;

    PostAdapter postAdapter;

    private LocationManager locationManager;
    private PostLocation currentLocation = new PostLocation("Victoria", "Canada");
    long currentLocationId;

    public DashboardFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
    }


    @Override
    public void onResume() {
        super.onResume();

        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        locationManager.requestLocationUpdates(bestProvider, 0, 0, this);
        onLocationChanged(locationManager.getLastKnownLocation(bestProvider));

        getLoaderManager().initLoader(POST_LOADER_ID, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        postAdapter = new PostAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.list_item_posts);
        listView.setAdapter(postAdapter);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_dashboard_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        if (itemId == R.id.action_add) {

            if(currentLocation.name == null || currentLocation.country == null){
                Toast.makeText(getActivity(), "You can't post news because your location is not available.", Toast.LENGTH_SHORT).show();
                return true;
            }

            Intent post_intent = new Intent(getActivity(), PostActivity.class);
            Bundle locationInfo = new Bundle();
            locationInfo.putString("LocationName", currentLocation.name);
            locationInfo.putString("LocationCountry", currentLocation.country);
            post_intent.putExtras(locationInfo);
            startActivity(post_intent);
            return true;
        }

        if(itemId == R.id.action_refresh){
            UpdateDashboard();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        UpdateDashboard();
    }

    private void UpdateDashboard(){
        FetchDataTask fetchDataTask = new FetchDataTask(getActivity());
        fetchDataTask.execute();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        return new CursorLoader(
                getActivity(),
                DataContract.PostEntry.CONTENT_URI,
                null,
                DataContract.PostEntry.COLUMN_LOCATION_ID + " = ?",
                new String[]{Long.toString(currentLocationId)},
                "datetime(" + DataContract.PostEntry.COLUMN_CREATED_AT + ") DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        postAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        postAdapter.swapCursor(null);

    }

    @Override
    public void onLocationChanged(Location location) {

        TextView locationView = (TextView) getActivity().findViewById(R.id.place_textview);

        if(location == null){
            locationView.setText("Location Not Available");
            return;
        }

        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() <= 0){
            Toast.makeText(getActivity(), "Unable to locate your city.", Toast.LENGTH_SHORT).show();
            return;
        }

        Address address = addresses.get(0);
        currentLocation.name = address.getLocality();
        currentLocation.country = address.getCountryName();

        locationView.setText(currentLocation.name);

        Cursor locationCursor = getActivity().getContentResolver().query(
                DataContract.LocationEntry.CONTENT_URI,
                new String[]{DataContract.LocationEntry.COLUMN_ID},
                DataContract.LocationEntry.COLUMN_NAME + " = ? AND " + DataContract.LocationEntry.COLUMN_COUNTRY + " = ?",
                new String[]{currentLocation.name, currentLocation.country},
                null);


        if(!locationCursor.moveToFirst()){
            ContentValues newLocation = new ContentValues();

            newLocation.put(DataContract.LocationEntry.COLUMN_NAME, currentLocation.name);
            newLocation.put(DataContract.LocationEntry.COLUMN_COUNTRY, currentLocation.country);

            Uri locationUri = getActivity().getContentResolver().insert(DataContract.LocationEntry.CONTENT_URI, newLocation);
            currentLocationId = ContentUris.parseId(locationUri);
        }
        else{
            currentLocationId = locationCursor.getLong(
                    locationCursor.getColumnIndex(DataContract.LocationEntry.COLUMN_ID));
        }
        getActivity().getContentResolver().notifyChange(DataContract.PostEntry.CONTENT_URI, null);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
