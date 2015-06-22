package luckynine.youtell;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

import luckynine.youtell.data.Post;


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
        FetchDataTask fetchDataTask = new FetchDataTask();
        fetchDataTask.execute();
    }

    public class FetchDataTask extends AsyncTask<Void, Void, String[]>{

        private final String LOG_TAG = FetchDataTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(Void... voids) {

            try {
                final String url = "http://10.0.2.2:3000/api/posts";
                RestTemplate restTemplate = new RestTemplate();

                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
                messageConverter.setObjectMapper(mapper);
                restTemplate.getMessageConverters().add(messageConverter);

                ResponseEntity<Post[]> responseEntity = restTemplate.getForEntity(url, Post[].class);

                Log.d(LOG_TAG, String.format("HTTP Request: GET %s. Returns %s", url, responseEntity.getStatusCode()));
                Post[] posts = responseEntity.getBody();

                String[] items = new String[posts.length];
                for(int i = 0; i< posts.length; i++){
                    items[i] = posts[i].author + " - " + posts[i].title + "\n" + posts[i].content;
                }
                return items;

            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] results) {
            super.onPostExecute(results);

            if(results != null){
                postAdapter.clear();
                postAdapter.addAll(results);
            }
        }
    }

}
