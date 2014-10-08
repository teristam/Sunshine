package com.example.android.sunshine.app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by teristam on 21/9/14.
 */
public class WeatherProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private static final UriMatcher sUriMatcher=buildUriMatcher();

    private WeatherDBHelper mOpenHelper;

    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    static { //static initialization block, only call once

        //Make a inner join query
        sWeatherByLocationSettingQueryBuilder=new SQLiteQueryBuilder();
        sWeatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME+ " INNER JOIN "+
                        WeatherContract.LocationEntry.TABLE_NAME +
                        " ON "+ WeatherContract.WeatherEntry.TABLE_NAME +
                        "."+ WeatherContract.WeatherEntry.COLUMN_LOC_KEY+
                        " = " + WeatherContract.LocationEntry.TABLE_NAME+
                        "."+ WeatherContract.LocationEntry._ID
        );

    }

    //Selection string for location query
    private static final String sLocationSettingSelection=
            WeatherContract.LocationEntry.TABLE_NAME+"." + WeatherContract.LocationEntry.COLUMN_LOCATION+"=?";

    //Selection string for location query with a starting date
    private static final String sLocationSettingWithStartDateSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT + " >= ? ";

    private static final String sLocationSettingWithDateSelection=
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT + " =?";

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == null) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, startDate};
            selection = sLocationSettingWithStartDateSelection;
        }

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getWeatherByLocationSettingAndDate(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String date= WeatherContract.WeatherEntry.getDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (date == null) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, date};
            selection = sLocationSettingWithStartDateSelection;
        }

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );


    }



    @Override
    public boolean onCreate() {
        mOpenHelper=new WeatherDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            // "weather/*/*"
            case WEATHER_WITH_LOCATION_AND_DATE:
            {
                retCursor = getWeatherByLocationSettingAndDate(uri,projection,sortOrder);
                break;
            }
            // "weather/*"
            case WEATHER_WITH_LOCATION: {
                retCursor=getWeatherByLocationSetting(uri,projection,sortOrder);
                break;
            }
            // "weather"
            case WEATHER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location/*"
            case LOCATION_ID: {

                long id=ContentUris.parseId(uri);
                retCursor=mOpenHelper.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        WeatherContract.LocationEntry._ID+"= '"+id+"'",
                        null,
                        null,
                        null,
                        sortOrder);

                break;
            }
            // "location"
            case LOCATION: {
                retCursor=mOpenHelper.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) { //return the MIME type associated with the URI

        final int match=sUriMatcher.match(uri);

        switch(match){
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE; //Can return multiple items
            case LOCATION_ID:
                return WeatherContract.LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: "+uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final SQLiteDatabase db=mOpenHelper.getWritableDatabase();
        final int match=sUriMatcher.match(uri);
        Uri returnUri;

        switch(match){
            case WEATHER: {
                long _id=db.insert(WeatherContract.WeatherEntry.TABLE_NAME,null, contentValues);

                if(_id>0){
                    //succeed
                    returnUri= WeatherContract.WeatherEntry.buildWeatherUri(_id);
                }else{
                    throw new SQLException("Failed to insert row into "+uri);

                }
                break;
            }

            case LOCATION: {
                long _id=db.insert(WeatherContract.LocationEntry.TABLE_NAME,null,contentValues);

                if(_id>0){
                    returnUri= WeatherContract.LocationEntry.buildLocationUri(_id);
                }else{
                    throw new SQLException("Fail to insert row into "+uri);
                }

                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: "+uri);
        }

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db=mOpenHelper.getWritableDatabase();

        final int match=sUriMatcher.match(uri);
        int row=0;

        switch(match){
            case WEATHER: {
                row=db.delete(WeatherContract.WeatherEntry.TABLE_NAME,selection,selectionArgs);
                break;
            }

            case LOCATION: {

                row=db.delete(WeatherContract.LocationEntry.TABLE_NAME,selection,selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: "+uri);
        }

        if(null ==selection || 0!=row){
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return row;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final SQLiteDatabase db=mOpenHelper.getWritableDatabase();

        final int match=sUriMatcher.match(uri);
        int row=0;

        switch(match){
            case WEATHER: {
                row=db.update(WeatherContract.WeatherEntry.TABLE_NAME, contentValues,selection,selectionArgs);
                break;
            }

            case LOCATION: {

                row=db.update(WeatherContract.LocationEntry.TABLE_NAME, contentValues,selection,selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: "+uri);
        }

        return row;
    }

    private static UriMatcher buildUriMatcher(){
        //Construct a UriMatcher to match the Uri type

        UriMatcher myUriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
        final String authority=WeatherContract.CONTENT_AUTHORITY; //a short cut


        myUriMatcher.addURI(authority, WeatherContract.PATH_WEATHER, WEATHER);
        myUriMatcher.addURI(authority, WeatherContract.PATH_WEATHER+"/*",WEATHER_WITH_LOCATION);
        myUriMatcher.addURI(authority, WeatherContract.PATH_WEATHER+"/*/*", WEATHER_WITH_LOCATION_AND_DATE);

        myUriMatcher.addURI(authority, WeatherContract.PATH_LOCATION,LOCATION);
        myUriMatcher.addURI(authority, WeatherContract.PATH_LOCATION+"/#", LOCATION_ID);

        return myUriMatcher;
    }
}
