package com.example.shakeel.listwithdb;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends ActionBarActivity implements LocationListener {


    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    String lat;
    String provider;
    protected Double latitude, longitude;
    protected boolean gpd_enabled,network_enabled;
    public TextView elevationText;

    public TextView cloudText;

    public TextView seapressureText;

    public TextView temperatureText;

    public TextView humidityText;

    public TextView stationText;

    public TextView dewpointText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        elevationText = (TextView)findViewById(R.id.text_elevation);

        cloudText = (TextView)findViewById(R.id.text_clouds);

        seapressureText = (TextView)findViewById(R.id.text_seapressure);

        temperatureText = (TextView)findViewById(R.id.text_temperature);

        humidityText = (TextView)findViewById(R.id.text_humidity);

        stationText = (TextView)findViewById(R.id.text_station);

        dewpointText = (TextView)findViewById(R.id.text_dewpoint);
    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        Log.d("latitude: ", latitude.toString());

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public String readJsonFeed(String url){
        StringBuilder stringBuilder = new StringBuilder();
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        try{
            HttpResponse httpResponse = httpClient.execute(httpGet);
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if(statusCode == 200){
                HttpEntity httpEntity = httpResponse.getEntity();
                InputStream inputStream = httpEntity.getContent();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while((line=bufferedReader.readLine())!= null){
                    stringBuilder.append(line);
                }
                inputStream.close();
            }else{
                Log.d("Json", "Failed to download data");
            }

        }catch (Exception e){
            Log.d("ReadJsonFeed ", e.getLocalizedMessage());
        }
        return stringBuilder.toString();
    }

    private class ReadWeatherJsonFeedTask extends AsyncTask<String, Void, String>{
        protected String doInBackground(String... urls){
            return readJsonFeed(urls[0]);
        }

        protected void onPostExecute(String result){
            try{
                JSONObject jsonObject = new JSONObject(result);
                JSONObject weatherObservationItems = new JSONObject(jsonObject.getString("weatherObservation"));

                elevationText.setText(weatherObservationItems.getString("elevation"));
                cloudText.setText(weatherObservationItems.getString("clouds"));
                seapressureText.setText(weatherObservationItems.getString("seaLevelPressure"));
                temperatureText.setText(weatherObservationItems.getString("temperature"));
                humidityText.setText(weatherObservationItems.getString("humidity"));
                stationText.setText(weatherObservationItems.getString("stationName"));
                dewpointText.setText(weatherObservationItems.getString("dewPoint"));
            }catch (Exception e){
                Log.d("ReadWeatherJsontask", e.getLocalizedMessage());
            }
        }
    }

    public void getData(View view){
        if(latitude == null){
            latitude = 33.823066;
        }
        if(longitude == null){
            longitude = -84.370738;
        }
        new ReadWeatherJsonFeedTask().execute("http://ws.geonames.org/findNearByWeatherJSON?lat="+latitude.toString()+"&lng="+longitude.toString()+"&username=your_user_name_here");
    }
}
