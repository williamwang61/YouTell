package luckynine.youtell;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import luckynine.youtell.data.Post;
import luckynine.youtell.data.PostLocation;
import luckynine.youtell.data.UserStatus;


public class PostActivity extends AppCompatActivity {

    private PostLocation currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Bundle locationInfo = getIntent().getExtras();
        currentLocation = new PostLocation(
                locationInfo.getString("LocationName"),
                locationInfo.getString("LocationCountry"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_send) {
            TextView textView = (TextView) findViewById(R.id.post_edittext);
            String postText = textView.getText().toString();
            if(postText.isEmpty()) {
                Toast.makeText(this, "Can't send empty post.", Toast.LENGTH_SHORT).show();
                return true;
            }

            Post postToSend = new Post();
            postToSend.author_id = UserStatus.getUserId(getApplicationContext());
            postToSend.author = UserStatus.getFirstName(getApplicationContext());
            postToSend.content = postText;
            postToSend.location = currentLocation;

            SendDataTask sendDataTask = new SendDataTask(this, postToSend);
            sendDataTask.execute();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SendDataTask extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = SendDataTask.class.getSimpleName();

        private final Context context;
        private Post postToSend;
        private String postResult;

        public SendDataTask(Context context, Post postToSend){
            this.context = context;
            this.postToSend = postToSend;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                RestTemplate restTemplate = new RestTemplate();

                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
                messageConverter.setObjectMapper(mapper);
                restTemplate.getMessageConverters().add(messageConverter);

                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                headers.add("AccessToken", UserStatus.getAccessToken(getApplicationContext()));
                headers.add("UserId", postToSend.author_id);
                HttpEntity<Post> entity = new HttpEntity<>(postToSend, headers);
                ResponseEntity<Post> responseEntity = restTemplate.exchange(Utilities.SERVER_URL, HttpMethod.POST, entity, Post.class);

                HttpStatus responseStatus = responseEntity.getStatusCode();

                Log.d(LOG_TAG, String.format("HTTP Request: POST %s. Returns %s", Utilities.SERVER_URL, responseStatus));

                if(responseStatus.equals(HttpStatus.CREATED))
                    postResult = "Post sent successfully!";
                else
                    postResult = "Failed to send post!";

            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                postResult = "Failed to send post!";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(context, postResult, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
