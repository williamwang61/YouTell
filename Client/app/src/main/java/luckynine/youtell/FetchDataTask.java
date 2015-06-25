package luckynine.youtell;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

import luckynine.youtell.data.DataContract;
import luckynine.youtell.data.Post;

/**
 * Created by Weiliang on 6/24/2015.
 */
public class FetchDataTask extends AsyncTask<Void, Void, String[]> {

    private final String LOG_TAG = FetchDataTask.class.getSimpleName();

    private ArrayAdapter<String> postAdapter;
    private final Context context;

    public FetchDataTask(Context context, ArrayAdapter<String> arrayAdapter){
        this.context = context;
        this.postAdapter = arrayAdapter;
    }

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

            return addPosts(posts);

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

    private String[] addPosts(Post[] posts){

        ArrayList<ContentValues> contentValuesToInsert = new ArrayList<ContentValues>();

        for(int i = 0; i< posts.length; i++){


            Cursor postCursor = context.getContentResolver().query(
                    DataContract.PostEntry.CONTENT_URI,
                    new String[]{DataContract.PostEntry.COLUMN_ID},
                    DataContract.PostEntry.COLUMN_ID + " = ?",
                    new String[]{posts[i]._id},
                    null);

            if (!postCursor.moveToFirst()) {
                ContentValues newPost = new ContentValues();

                newPost.put(DataContract.PostEntry.COLUMN_ID, posts[i]._id);
                newPost.put(DataContract.PostEntry.COLUMN_AUTHOR, posts[i].author);
                newPost.put(DataContract.PostEntry.COLUMN_CONTENT, posts[i].content);
                newPost.put(DataContract.PostEntry.COLUMN_CREATED_AT, posts[i].createdAt.toString());

                contentValuesToInsert.add(newPost);
            }
        }

        if(contentValuesToInsert.size() > 0) {
            context.getContentResolver().bulkInsert(
                    DataContract.PostEntry.CONTENT_URI,
                    contentValuesToInsert.toArray(new ContentValues[contentValuesToInsert.size()]));
        }

        //display inserted data
        Cursor cursor = context.getContentResolver().query(
                DataContract.PostEntry.CONTENT_URI,
                null,
                null,
                null,
                DataContract.PostEntry.COLUMN_CREATED_AT + " DESC");

        ContentValues[] insertedData = new ContentValues[cursor.getCount()];
        if(cursor.moveToFirst()){
            int index = 0;
            do{
                ContentValues value = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, value);
                insertedData[index] = value;
                index++;
            }while(cursor.moveToNext());
        }

        return convertContentValuesToUXFormat(insertedData);
    }

    private String[] convertContentValuesToUXFormat(ContentValues[] contentValuesList){
        String[] results = new String[contentValuesList.length];
        for ( int i = 0; i < contentValuesList.length; i++ ) {
            ContentValues values = contentValuesList[i];
            results[i] = values.getAsString(DataContract.PostEntry.COLUMN_CREATED_AT) + " - " +
                    values.getAsString(DataContract.PostEntry.COLUMN_AUTHOR) + "\n" +
                    values.getAsString(DataContract.PostEntry.COLUMN_CONTENT);
        }
        return results;
    }
}
