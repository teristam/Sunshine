package com.example.android.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    protected ArrayAdapter<String> adapter=null;
    ListView mListView=null;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<String> listItem=new ArrayList<String>();
        listItem.add("Today-Sunny-28/29");
        listItem.add("Tomorrow-Raining-31/33");
        listItem.add("Day after Tomorrow -Cloudy-23/24");

        //Create adaptor
        adapter=new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast, R.id.list_item_forecast_textview,listItem);

        mListView=(ListView)rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(adapter);


        this.setHasOptionsMenu(true);

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String,String,String[]>{

        private final String LOG_TAG=FetchWeatherTask.class.getSimpleName(); //best practice

        @Override
        protected String[] doInBackground(String... strings) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                Uri.Builder builder=Uri.parse("http://api.openweathermap.org/data/2.5/forecast/daily?").buildUpon();
                builder.appendQueryParameter("q",strings[0])
                        .appendQueryParameter("mode", "json")
                        .appendQueryParameter("unit","metric")
                        .appendQueryParameter("cnt", "7");

                URL url=new URL(builder.build().toString());

                Log.d(LOG_TAG,url.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            Log.d(LOG_TAG,forecastJsonStr);

            String[] forecast=null;

            try{
                forecast=WeatherDataParser.getWeatherDataFromJson(forecastJsonStr,7);

            }catch (JSONException e){
                e.printStackTrace();
            }

            Log.d(LOG_TAG,forecast[0]);
            return forecast;

        }


        @Override
        protected void onPostExecute(String[] forecast) {

            if(forecast!=null){
                adapter.clear();
                adapter.addAll(forecast);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.refresh_menu){

            FetchWeatherTask task=new FetchWeatherTask();
            task.execute("94035");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
