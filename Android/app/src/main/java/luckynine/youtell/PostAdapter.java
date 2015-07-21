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

import luckynine.youtell.data.DataContract;

/**
 * Created by Weiliang on 6/25/2015.
 */
public class PostAdapter extends CursorAdapter {

    private final String LOG_TAG = PostAdapter.class.getSimpleName();
    private final static String FACEBOOK_USER_PROFILE_PHOTO_URL_FORMAT = "https://graph.facebook.com/%s/picture?type=normal";

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

        try {
            URL author_photo_url = new URL(
                    String.format(
                            FACEBOOK_USER_PROFILE_PHOTO_URL_FORMAT,
                            cursor.getString(cursor.getColumnIndex(DataContract.PostEntry.COLUMN_AUTHOR_ID))));
            FetchImageFromUrlTask fetchImageFromUrlTask = new FetchImageFromUrlTask(postViewHolder.authorPhotoView);
            fetchImageFromUrlTask.execute(author_photo_url);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
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

    public class FetchImageFromUrlTask extends AsyncTask<URL, Void, Bitmap>{

        private final String LOG_TAG = FetchImageFromUrlTask.class.getSimpleName();

        private ImageView imageContainer;

        public FetchImageFromUrlTask(ImageView imageContainer){
            this.imageContainer = imageContainer;
        }

        @Override
        protected Bitmap doInBackground(URL... url) {
            try {
                return BitmapFactory.decodeStream(url[0].openConnection().getInputStream());
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imageContainer.setImageBitmap(bitmap);
        }
    }
}
