package luckynine.youtell;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import luckynine.youtell.data.DataContract;


public class DashboardFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private final String LOG_TAG = DashboardFragment.class.getSimpleName();

    private static final int POST_LOADER_ID = 0;
    PostAdapter postAdapter;

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

        getLoaderManager().initLoader(POST_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Cursor cursor = getActivity().getContentResolver().query(
                DataContract.PostEntry.CONTENT_URI,
                null,
                null,
                null,
                DataContract.PostEntry.COLUMN_CREATED_AT + " DESC");
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
            Intent post_intent = new Intent(getActivity(), PostActivity.class);
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
                null,
                null,
                DataContract.PostEntry.COLUMN_CREATED_AT + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        postAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        postAdapter.swapCursor(null);

    }
}
