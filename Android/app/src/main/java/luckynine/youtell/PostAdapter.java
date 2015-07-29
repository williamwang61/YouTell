package luckynine.youtell;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.HashMap;

import luckynine.youtell.data.DataContract;

/**
 * Created by Weiliang on 6/25/2015.
 */
public class PostAdapter extends CursorAdapter {

    private final String LOG_TAG = PostAdapter.class.getSimpleName();
    private final static String FACEBOOK_USER_PROFILE_PHOTO_URL_FORMAT = "https://graph.facebook.com/%s/picture?width=100&height=100";

    private HashMap<String, Bitmap> profilePhotoCache = new HashMap<>();

    public PostAdapter(Context context, Cursor c, int flags){
        super(context, c, flags);
    }

    public static class PostViewHolder {
        public final ImageView authorPhotoView;
        public final TextView authorView;
        public final TextView contentView;
        public final TextView timeView;

        public PostViewHolder(View view) {
            authorPhotoView = (ImageView) view.findViewById(R.id.list_item_author_photo);
            authorView = (TextView) view.findViewById(R.id.list_item_author_textview);
            contentView = (TextView) view.findViewById(R.id.list_item_content_textview);
            timeView = (TextView) view.findViewById(R.id.list_item_time_textview);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_post, viewGroup, false);
        PostViewHolder viewHolder = new PostViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        PostViewHolder postViewHolder = (PostViewHolder) view.getTag();

        String user_id = cursor.getString(cursor.getColumnIndex(DataContract.PostEntry.COLUMN_AUTHOR_ID));
        if(profilePhotoCache.containsKey(user_id))
            postViewHolder.authorPhotoView.setImageBitmap(profilePhotoCache.get(user_id));
        else{
            FetchImageFromUrlTask fetchImageFromUrlTask = new FetchImageFromUrlTask(postViewHolder.authorPhotoView, user_id);
            fetchImageFromUrlTask.execute();
        }

        postViewHolder.authorView.setText(
                "@" + cursor.getString(
                        cursor.getColumnIndex(DataContract.PostEntry.COLUMN_AUTHOR)));

        postViewHolder.contentView.setText(
                cursor.getString(
                        cursor.getColumnIndex(DataContract.PostEntry.COLUMN_CONTENT)));

        String timeText = cursor.getString(cursor.getColumnIndex(DataContract.PostEntry.COLUMN_CREATED_AT));
        Timestamp timestamp = Utilities.ConvertStringToTimestamp(timeText);

        postViewHolder.timeView.setText(Utilities.GetTimeDifference(timestamp));
    }

    public class FetchImageFromUrlTask extends AsyncTask<Void, Void, Bitmap>{

        private final String LOG_TAG = FetchImageFromUrlTask.class.getSimpleName();

        private String userId;
        private ImageView imageContainer;

        public FetchImageFromUrlTask(ImageView imageContainer, String userId){
            this.imageContainer = imageContainer;
            this.userId = userId;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                URL author_photo_url = new URL(
                        String.format(
                                FACEBOOK_USER_PROFILE_PHOTO_URL_FORMAT,
                                userId));
                return BitmapFactory.decodeStream(author_photo_url.openConnection().getInputStream());
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(bitmap != null) {
                imageContainer.setImageBitmap(bitmap);
                profilePhotoCache.put(userId, bitmap);
            }
        }
    }
}
