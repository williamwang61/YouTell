package luckynine.youtell;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import luckynine.youtell.data.DataContract;

/**
 * Created by Weiliang on 6/25/2015.
 */
public class PostAdapter extends CursorAdapter {

    public PostAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_post, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView) view;
        textView.setText(convertCursorToUXFormat(cursor));
    }

    private String convertCursorToUXFormat(Cursor cursor){
        return cursor.getString(cursor.getColumnIndex(DataContract.PostEntry.COLUMN_CREATED_AT)) + " - " +
                cursor.getString(cursor.getColumnIndex(DataContract.PostEntry.COLUMN_AUTHOR)) + "\n" +
                cursor.getString(cursor.getColumnIndex(DataContract.PostEntry.COLUMN_CONTENT));
    }
}
