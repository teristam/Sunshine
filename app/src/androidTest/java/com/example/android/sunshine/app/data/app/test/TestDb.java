package com.example.android.sunshine.app.data.app.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherDBHelper;

import java.util.Map;
import java.util.Set;


/**
 * Created by teristam on 13/9/14.
 */
public class TestDb extends AndroidTestCase {
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable { //will only run tests start with "test"
        mContext.deleteDatabase(WeatherDBHelper.DATABASE_NAME);
        SQLiteDatabase db=new WeatherDBHelper(this.mContext).getWritableDatabase();
        assertEquals(true,db.isOpen());
        db.close();

    }

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

    public void testInsertReadDb(){



        WeatherDBHelper dbHelper=new WeatherDBHelper(mContext);
        SQLiteDatabase db=dbHelper.getWritableDatabase();


        //Create and insert values

        long locationRowId;
        ContentValues values=createLocationValues();
        locationRowId=db.insert(WeatherContract.LocationEntry.TABLE_NAME,null, values);

        assertTrue(locationRowId!=-1);
        Log.d(LOG_TAG, "New row id: "+locationRowId);

        //Read and validate values
        Cursor cursor=db.query(WeatherContract.LocationEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        validateCursor(cursor,values);


        //Create and insert weather data
        long rowid;
        ContentValues weatherValues=createWeatherValues(locationRowId);
        rowid=db.insert(WeatherContract.WeatherEntry.TABLE_NAME,null,weatherValues);

        assertTrue(rowid!=-1);


        Cursor c=db.query(WeatherContract.WeatherEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        //Compare the inserted values with the retrieved values
        validateCursor(c,weatherValues);


    }

}
