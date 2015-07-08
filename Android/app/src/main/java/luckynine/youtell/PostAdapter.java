package luckynine.youtell;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.sql.Timestamp;

import luckynine.youtell.data.DataContract;

/**
 * Created by Weiliang on 6/25/2015.
 */
public class PostAdapter extends CursorAdapter {

    public PostAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public static class PostViewHolder {
        public final TextView authorView;
        public final TextView contentView;
        public final TextView timeView;

        public PostViewHolder(View view) {
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
}
