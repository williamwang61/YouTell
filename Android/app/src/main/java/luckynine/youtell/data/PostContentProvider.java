package luckynine.youtell.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by Weiliang on 6/22/2015.
 */
public class PostContentProvider extends ContentProvider{

    private static final UriMatcher uriMatcher = buildUriMatcher();

    static final int URI_POST = 100;
    static final int URI_POST_WITH_LOCATIONID = 101;
    static final int URI_LOCATION = 200;

    private PostDBHelper postDbHelper;

    @Override
    public boolean onCreate() {
        postDbHelper = new PostDBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch(uriMatcher.match(uri)) {
            case URI_POST:
                return DataContract.PostEntry.CONTENT_DIR_TYPE;
            case URI_POST_WITH_LOCATIONID:
                return DataContract.PostEntry.CONTENT_DIR_TYPE;
            case URI_LOCATION:
                return DataContract.LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unsupported uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursorToReturn;
        switch(uriMatcher.match(uri)) {
            case URI_POST:
                cursorToReturn = postDbHelper.getReadableDatabase().query(
                        DataContract.PostEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case URI_POST_WITH_LOCATIONID:
                cursorToReturn = postDbHelper.getReadableDatabase().query(
                        DataContract.PostEntry.TABLE_NAME,
                        projection,
                        DataContract.PostEntry.COLUMN_LOCATION_ID + "= ?",
                        new String[]{DataContract.PostEntry.getLocationIdFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case URI_LOCATION:
                cursorToReturn = postDbHelper.getReadableDatabase().query(
                        DataContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unsupported uri: " + uri);
        }
        cursorToReturn.setNotificationUri(getContext().getContentResolver(), uri);
        return cursorToReturn;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = postDbHelper.getWritableDatabase();
        Uri uriToReturn;

        switch (uriMatcher.match(uri)) {
            case URI_POST: {
                long _id = db.insert(DataContract.PostEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    uriToReturn = DataContract.PostEntry.buildPostUriWithId(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            }
            case URI_LOCATION: {
                long _id = db.insert(DataContract.LocationEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    uriToReturn = DataContract.LocationEntry.buildLocationUriWithId(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unsupported uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return uriToReturn;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = postDbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case URI_POST:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DataContract.PostEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = postDbHelper.getWritableDatabase();
        int rowDeleted;

        switch (uriMatcher.match(uri)) {
            case URI_POST: {
                rowDeleted = db.delete(
                        DataContract.PostEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case URI_LOCATION: {
                rowDeleted = db.delete(
                        DataContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unsupported uri: " + uri);
        }
        if (rowDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = postDbHelper.getWritableDatabase();
        int rowUpdated;

        switch (uriMatcher.match(uri)) {
            case URI_POST:
                rowUpdated = db.update(DataContract.PostEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case URI_LOCATION:
                rowUpdated = db.update(DataContract.LocationEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowUpdated;
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, DataContract.PATH_POST, URI_POST);
        matcher.addURI(authority, DataContract.PATH_POST + "/*", URI_POST_WITH_LOCATIONID);
        matcher.addURI(authority, DataContract.PATH_LOCATION, URI_LOCATION);

        return matcher;
    }
}
