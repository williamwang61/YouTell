package luckynine.youtell;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class DashboardFragment extends Fragment {

    private final String LOG_TAG = DashboardFragment.class.getSimpleName();

    ArrayAdapter<String> postAdapter;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        postAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.list_item_post,
                R.id.list_item_post_textview,
                new ArrayList<String>()
        );
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
        FetchDataTask fetchDataTask = new FetchDataTask(getActivity(), postAdapter);
        fetchDataTask.execute();
    }

}
