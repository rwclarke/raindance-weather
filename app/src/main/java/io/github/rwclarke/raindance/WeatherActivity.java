package io.github.rwclarke.raindance;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.text.Text;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.github.rwclarke.raindance.model.DailyWeatherReport;

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    final String URL_BASE = "http://api.openweathermap.org/data/2.5/forecast";
    final String API_KEY = "&APPID=62782d3f768215474f42e7b8dc452d18";
    final String URL_UNIT = "&units=metric";
    static String URL_COORD;

    private GoogleApiClient mGoogleApiClient;
    private final int PERMISSION_LOCATION = 111;

    private ArrayList<DailyWeatherReport> weatherReportList = new ArrayList<>();

    private TextView cityLabel;
    private TextView weatherDescription;
    private TextView currentTemp;
    private TextView lowhighTemp;
    private ImageView weatherIcon;

    WeatherAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        cityLabel = (TextView) findViewById(R.id.cityLabel);
        weatherDescription = (TextView) findViewById(R.id.weatherDescription);
        currentTemp = (TextView) findViewById(R.id.currentTemp);
        lowhighTemp = (TextView) findViewById(R.id.lowhighTemp);
        weatherIcon = (ImageView) findViewById(R.id.weatherIcon);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this) // managed onStart() and onStop()
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.content_weather_reports);

        adapter = new WeatherAdapter(weatherReportList);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);

    }

    public void downloadWeatherData(Location location) {

        URL_COORD = "?lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
        //URL_COORD = "?lat=40.75&lon=-73.98";
        final String url = URL_BASE + URL_COORD + URL_UNIT + API_KEY;

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject city = response.getJSONObject("city");
                    String cityName = city.getString("name");
                    String country = city.getString("country");

                    Log.v("JSON", "City: " + city);

                    cityLabel.setText(cityName.toUpperCase());

                    JSONArray list = response.getJSONArray("list");

                    for (int i = 0; i < list.length(); i++) {
                        JSONObject obj = list.getJSONObject(i);
                        JSONObject main = obj.getJSONObject("main");
                        Double currentTemp = main.getDouble("temp");
                        Double maxTemp = main.getDouble("temp_max");
                        Double minTemp = main.getDouble("temp_min");

                        JSONArray weatherArray = obj.getJSONArray("weather");
                        JSONObject weather = weatherArray.getJSONObject(0);
                        String weatherType = weather.getString("main");
                        String rawDate = obj.getString("dt_txt");

                        DailyWeatherReport report = new DailyWeatherReport(cityName, country, currentTemp.intValue(), maxTemp.intValue(), minTemp.intValue(), weatherType, rawDate);

                        weatherReportList.add(report);

                    }

                } catch(JSONException e) {
                    Log.v("ERROR", e.getLocalizedMessage());
                }

                updateUI();
                adapter.notifyDataSetChanged();

            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError  error) {
                Log.v("FUN", "Error: " + error.getLocalizedMessage());
            }
        });

        Volley.newRequestQueue(this).add(jsonRequest);
    }

    public void updateUI() {
        if (weatherReportList.size() > 0) {
            DailyWeatherReport report = weatherReportList.get(0);


            switch (report.getWeather()) {
                case DailyWeatherReport.WEATHER_TYPE_CLEAR:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_CLOUDS:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_RAIN:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));;
                    break;
                default:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
            }

            weatherDescription.setText(report.getWeather().toUpperCase());
            currentTemp.setText(Integer.toString(report.getCurrentTemp()) + "°");
            lowhighTemp.setText("▼ " + Integer.toString(report.getMinTemp()) + "° ▲ " + Integer.toString(report.getMaxTemp()) + "°");
            cityLabel.setText(report.getCityName().toUpperCase());
        }
    }

    public void startLocationServices() {
        try {
            LocationRequest req = LocationRequest.create().setPriority(LocationRequest.PRIORITY_NO_POWER);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, req, this);
        } catch(SecurityException exception) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case PERMISSION_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationServices();
                } else {
                    Toast.makeText(this, "I can't run your location dummy - You Denied Permission!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public class WeatherAdapter extends RecyclerView.Adapter<WeatherReportViewHolder> {
        private ArrayList<DailyWeatherReport> mDailyWeatherReports;

        public WeatherAdapter(ArrayList<DailyWeatherReport> mDailyWeatherReports) {
            this.mDailyWeatherReports = mDailyWeatherReports;
        }

        @Override
        public void onBindViewHolder(WeatherReportViewHolder holder, int position) {
            DailyWeatherReport report =  mDailyWeatherReports.get(position);
            holder.updateUI(report);
        }

        @Override
        public int getItemCount() {
            return  mDailyWeatherReports.size();
        }

        @Override
        public WeatherReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_weather, parent, false);
            return new WeatherReportViewHolder(card);
        }
    }

    public class WeatherReportViewHolder extends RecyclerView.ViewHolder {

        private ImageView lweatherIcon;
        private TextView lweatherDate;
        private TextView lweatherDescription;
        private TextView ltempHigh;
        private TextView ltempLow;



        public WeatherReportViewHolder(View itemView) {
            super(itemView);

            lweatherIcon = (ImageView)itemView.findViewById(R.id.weather_icon);
            lweatherDate = (TextView)itemView.findViewById(R.id.weather_day);
            lweatherDescription = (TextView)itemView.findViewById(R.id.weather_description);
            ltempHigh = (TextView)itemView.findViewById(R.id.weather_temp_high);
            ltempLow = (TextView)itemView.findViewById(R.id.weather_temp_low);
        }

        public void updateUI(DailyWeatherReport report) {

            lweatherDate.setText(report.getFormattedDate());
            lweatherDescription.setText(report.getWeather());
            ltempHigh.setText(Integer.toString(report.getMaxTemp()));
            ltempLow.setText(Integer.toString(report.getMinTemp()));

            switch (report.getWeather()) {
                case DailyWeatherReport.WEATHER_TYPE_CLEAR:
                    lweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_CLOUDS:
                    lweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_RAIN:
                    lweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.rainy_mini));;
                    break;
                default:
                    lweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny_mini));
            }
        }
    }

    // Interface Delegate Methods

    @Override
    public void onLocationChanged(Location location) {
        downloadWeatherData(location);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            downloadWeatherData(mLastLocation);
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
            } else {
                startLocationServices();
            }
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
