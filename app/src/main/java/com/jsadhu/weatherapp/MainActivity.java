package com.jsadhu.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.jsadhu.weatherapp.models.WeatherRVModel;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV;
    private TextInputEditText cityNameET;
    private ImageView bgIV, searchIV, weatherIconIV;
    private RecyclerView weatherRV;
    private ArrayList<WeatherRVModel> weatherRVModelList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String currentCity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idProgressBar);
        cityNameTV = findViewById(R.id.idCityNameView);
        temperatureTV = findViewById(R.id.idTemperature);
        conditionTV = findViewById(R.id.idWeatherCondition);
        cityNameET = findViewById(R.id.idCityNameEditText);
        bgIV = findViewById(R.id.idBGView);
        searchIV = findViewById(R.id.idSearchIcon);
        weatherIconIV = findViewById(R.id.idWeatherIcon);
        weatherRV = findViewById(R.id.idRVWeather);

        weatherRVModelList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModelList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_CODE);
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        currentCity = getCurrentCity(longitude, latitude);
        getWeatherInfo(currentCity);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredCity = cityNameET.getText().toString();
                if (!enteredCity.isEmpty()) {
                    getWeatherInfo(enteredCity);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            for (int grantResult : grantResults) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Please provide the permissions", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            Toast.makeText(this, "Permissions Granted!", Toast.LENGTH_SHORT).show();
        }
    }

    private void getWeatherInfo(String cityName) {
        String url = "https://api.weatherapi.com/v1/forecast.json?key=b99041bb8fe345dbb3e193434240807&q=" + cityName + "&days=1&aqi=yes&alerts=yes";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModelList.clear();
                try {
                    JSONObject currentObj = response.getJSONObject("current");
                    JSONObject conditionObj = currentObj.getJSONObject("condition");

                    String temperature = currentObj.getString("temp_c").concat("°C");
                    int isDay = currentObj.getInt("is_day");
                    String condition = conditionObj.getString("text");
                    String iconUrl = "https:" + conditionObj.getString("icon");
                    String url;
                    if (isDay == 1)
                        url = "https://static.vecteezy.com/system/resources/previews/019/466/826/non_2x/beautiful-clouds-clear-sky-daytime-background-free-photo.jpg";
                    else
                        url = "https://wallpapers.com/images/hd/clear-night-sky-wallpaper-wallpaper-x4anebb55xdlebj7.webp";
                    Picasso.get().load(url).into(bgIV);
                    temperatureTV.setText(temperature);
                    conditionTV.setText(condition);
                    Picasso.get().load(Uri.parse(iconUrl)).into(weatherIconIV);

                    JSONObject todaySForecastObj = response.getJSONObject("forecast");
                    JSONObject obj1 = todaySForecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hoursForecast = obj1.getJSONArray("hour");
                    for (int i = 0; i < hoursForecast.length(); i++) {
                        JSONObject obj = hoursForecast.getJSONObject(i);
                        String time = obj.getString("time");
                        String temp = obj.getString("temp_c");
                        String icon = obj.getJSONObject("condition").getString("icon");
                        String windSpeed = obj.getString("wind_kph");
                        WeatherRVModel weatherRVModel = new WeatherRVModel(time, temp, icon, windSpeed);
                        weatherRVModelList.add(weatherRVModel);
                    }
                    weatherRVAdapter.notifyDataSetChanged();
//                    Log.d("weatherrv", Integer.toString(weatherRVModelList.size()));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "API parsing failed!", Toast.LENGTH_SHORT).show();
                    ;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("onErrorResponse", error.getMessage());
                Toast.makeText(MainActivity.this, "Please pass a valid city name", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private String getCurrentCity(double longitude, double latitude) {
        String locationCity = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);
            for (Address address : addresses) {
                String city = address.getLocality();
                if (city != null && !city.isEmpty()) {
                    locationCity = city;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "City Not Found", Toast.LENGTH_SHORT).show();
        }
        return locationCity;
    }

    private String formatTime(String time) {
        String timeStr = "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm aa");
            Date date = inputFormat.parse(time);
            timeStr = outputFormat.format(date);
        } catch (ParseException e) {
            Toast.makeText(this, "Couldn't get hourly time!", Toast.LENGTH_SHORT).show();
        }
        return timeStr;
    }

}