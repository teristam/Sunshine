package com.example.android.sunshine.app.data.app.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.app.data.WeatherDBHelper;

import java.util.Map;
import java.util.Set;


/**
 * Created by teristam on 13/9/14.
 */
public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void testDeleteDb() throws Throwable {
//        mContext.deleteDatabase(WeatherDBHelper.DATABASE_NAME);
        setUp();

    }

    static public String TEST_LOCATION="99705";
    static public String TEST_DATE="20141205";

    static ContentValues createLocationValues(){

        String testLocationSetting = "99705";
        String testCityName = "North Pole";
        double testLatitude = 64.7488;
        double testLongitude = -147.353;

        ContentValues values=new ContentValues();
        values.put(WeatherContract.LocationEntry.COLUMN_LOCATION,testLocationSetting);
        values.put(WeatherContract.LocationEntry.COLUMN_CITY,testCityName);
        values.put(WeatherContract.LocationEntry.COLUMN_LAT,testLatitude);
        values.put(WeatherContract.LocationEntry.COLUMN_LONG, testLongitude);

        return values;
    }

    static ContentValues createWeatherValues(long locationRowId){

        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT, "20141205");
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 321);

        return weatherValues;

    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues){
        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String,Object>> valueSet=expectedValues.valueSet();

        //Long through the value and compare it one by one
        for(Map.Entry<String,Object> entry: valueSet){

            String columnName=entry.getKey();

            int idx=valueCursor.getColumnIndex(columnName);
            assertFalse(idx==-1);

            String expectedValue=entry.getValue().toString();
            assertEquals(expectedValue,valueCursor.getString(idx));
        }
    }

    public void testInsertReadProvider(){



        WeatherDBHelper dbHelper=new WeatherDBHelper(mContext);
        SQLiteDatabase db=dbHelper.getWritableDatabase();


        //Create and insert values

        long locationRowId;
        ContentValues values=createLocationValues();
        Uri locationInsertUri=mContext.getContentResolver().insert(LocationEntry.CONTENT_URI,values);

        assertTrue(locationInsertUri!=null);
        locationRowId= ContentUris.parseId(locationInsertUri);

        Log.d(LOG_TAG, "New row id: "+locationRowId);

        //Read and validate values
        Cursor cursor=mContext.getContentResolver().query(LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        validateCursor(cursor,values);

        cursor.close();


        //Create and insert weather data
        long rowid;
        ContentValues weatherValues=createWeatherValues(locationRowId);
        Uri weatherInsertUri = mContext.getContentResolver()
                .insert(WeatherEntry.CONTENT_URI, weatherValues);
        assertTrue(weatherInsertUri != null);


        Cursor c=mContext.getContentResolver().query(WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null); //Use content provider to access the data

        //Compare the inserted values with the retrieved values
        validateCursor(c,weatherValues);

        //Testing join location data and weather API
        c=mContext.getContentResolver().query(WeatherEntry.buildWeatherLocation(TEST_LOCATION),
                null,
                null,
                null,
                null);

        validateCursor(c,weatherValues);

        c=mContext.getContentResolver().query(WeatherEntry.buildWeatherLocationWithStartDate(TEST_LOCATION,TEST_DATE),
                null,
                null,
                null,
                null);
        validateCursor(c,weatherValues);

        c=mContext.getContentResolver().query(WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION,TEST_DATE),
                null,
                null,
                null,
                null);

        validateCursor(c,weatherValues);

        c.close();
    }

    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testUpdateLocation() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestDb.createLocationValues();

        Uri locationUri = mContext.getContentResolver().
                insert(LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(LocationEntry._ID, locationRowId);
        updatedValues.put(LocationEntry.COLUMN_CITY, "Santa's Village");

        int count = mContext.getContentResolver().update(
                LocationEntry.CONTENT_URI, updatedValues, LocationEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});

        assertEquals(count, 1);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        TestDb.validateCursor(cursor, updatedValues);
    }

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecordsAtEnd() {
        deleteAllRecords();
    }

    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    public void setUp() {
        deleteAllRecords();
    }



}
