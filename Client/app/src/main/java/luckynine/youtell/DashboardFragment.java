package luckynine.youtell;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {

    ArrayAdapter<String> postAdapter;

    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String[] dummydata = {
            "dummy data - 1",
            "dummy data - 2"
        };

        List<String> dummyList = new ArrayList<String>(Arrays.asList(dummydata));

        postAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_post,
                R.id.list_item_post_textview,
                dummyList
        );

        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.list_item_posts);
        listView.setAdapter(postAdapter);

        return rootView;
    }


}
