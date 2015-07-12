package luckynine.youtell;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import luckynine.youtell.data.DataContract;
import luckynine.youtell.data.PostLocation;


public class DashboardFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,LocationListener{

    private final String LOG_TAG = DashboardFragment.class.getSimpleName();
    private final static int LOGIN_REQUEST = 1;

    private static final int POST_LOADER_ID = 0;
    private PostAdapter postAdapter;

    private LocationManager locationManager;
    private PostLocation currentLocation = new PostLocation();
    private long currentLocationId;

    private static final String PREF_START_LOGIN = "start_login";

    private MenuItem logInOutMenuItem;

    public DashboardFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());

        boolean start_login = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(PREF_START_LOGIN, true);
        if(AccessToken.getCurrentAccessToken() == null && start_login){
            startActivityForResult(new Intent(getActivity(), LoginActivity.class), LOGIN_REQUEST);
        }
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
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.list_item_posts);
        postAdapter = new PostAdapter(getActivity(), null, 0);
        listView.setAdapter(postAdapter);

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                UpdateDashboard();
                if (swipeRefreshLayout.isRefreshing())
                    swipeRefreshLayout.setRefreshing(false);
            }
        });
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_dashboard_fragment, menu);
        logInOutMenuItem = menu.findItem(R.id.action_loginoff);
        if(AccessToken.getCurrentAccessToken() == null){
            logInOutMenuItem.setTitle(R.string.action_login);
        }
        else{
            logInOutMenuItem.setTitle(R.string.action_logout);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        if (itemId == R.id.action_add) {

            if(AccessToken.getCurrentAccessToken() == null){
                Toast.makeText(getActivity(), "You have to log in to post news.", Toast.LENGTH_SHORT).show();
                startActivityForResult(new Intent(getActivity(), LoginActivity.class), LOGIN_REQUEST);
                return true;
            }

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

        if(itemId == R.id.action_loginoff){
            if(AccessToken.getCurrentAccessToken() != null){
                LoginManager.getInstance().logOut();
                item.setTitle(getString(R.string.action_login));
            }
            else{
                startActivityForResult(new Intent(getActivity(), LoginActivity.class), LOGIN_REQUEST);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LoginActivity.callbackManager.onActivityResult(requestCode, resultCode, data);

        if(requestCode == LOGIN_REQUEST){
            if(resultCode == getActivity().RESULT_OK){
                logInOutMenuItem.setTitle(R.string.action_logout);
            }
            else{
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                editor.putBoolean(PREF_START_LOGIN, false);
                editor.commit();
            }
        }
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
