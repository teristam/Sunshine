package com.example.android.sunshine.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;


/**
 * Created by teristam on 13/9/14.
 */
public class WeatherDBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "weather.db";

    public WeatherDBHelper(Context context){
        super(context,DATABASE_NAME,null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //Create the location table
        final String SQL_CREATE_LOCATION_TABLE=" CREATE TABLE " +LocationEntry.TABLE_NAME + " (" +
        LocationEntry._ID + " INTEGER PRIMARY KEY," +
        LocationEntry.COLUMN_CITY + " TEXT NOT NULL, " +
        LocationEntry.COLUMN_LAT + " TEXT NOT NULL, " +
        LocationEntry.COLUMN_LONG + " TEXT NOT NULL, " +
        LocationEntry.COLUMN_LOCATION + " TEXT UNIQUE NOT NULL, " +
        "UNIQUE (" + LocationEntry.COLUMN_LOCATION +") ON CONFLICT IGNORE );";


        //Create the weather table
        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME +" (" +
        WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
        WeatherEntry.COLUMN_LOC_KEY+ " INTEGER NOT NULL, " +
        WeatherEntry.COLUMN_DATETEXT+" TEXT NOT NULL, " +
        WeatherEntry.COLUMN_SHORT_DESC+ " TEXT NOT NULL, "+
        WeatherEntry.COLUMN_WEATHER_ID+ " INTEGER NOT NULL, " +

        WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
        WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +

        WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " +
        WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
        WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
        WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " +

        //Adding constraints

        //Set up a foreign key so that every weather entry must be associated with a city
        " FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
        LocationEntry.TABLE_NAME + " (" + LocationEntry._ID+ "), "+

        //Make sure there is only one weather entry per day per location
        " UNIQUE (" + WeatherEntry.COLUMN_DATETEXT + ", " +
        WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
