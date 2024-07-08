package com.jsadhu.weatherapp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jsadhu.weatherapp.models.WeatherRVModel;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherRVAdapter extends RecyclerView.Adapter<WeatherRVAdapter.ViewHolder> {

    private Context context;
    private ArrayList<WeatherRVModel> weatherRVModelList;

    public WeatherRVAdapter(Context context, ArrayList<WeatherRVModel> weatherRVModelList) {
        this.context = context;
        this.weatherRVModelList = weatherRVModelList;
    }

    @NonNull
    @Override
    public WeatherRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_rv_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherRVAdapter.ViewHolder holder, int position) {
        WeatherRVModel model = weatherRVModelList.get(position);
        String time = model.getTime();
        String temperature = model.getTemperature();
        String windSpeed = model.getWindSpeed();
        String weatherConditionIcon = model.getIcon();

        Uri uri = Uri.parse("https:".concat(weatherConditionIcon));

        Picasso.get().load(uri).into(holder.weatherConditionIV);
        holder.windSpeedTV.setText(windSpeed.concat("Km/h"));
        holder.temperatureTV.setText(temperature.concat("°c"));

        String timeStr = "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm aa");
            Date date = inputFormat.parse(time);
            timeStr = outputFormat.format(date);
        }
        catch(ParseException pe){
            Toast.makeText(context, pe.getMessage(), Toast.LENGTH_SHORT).show();
            pe.printStackTrace();
        }
        finally {
            holder.timeTV.setText(timeStr);
        }

    }

    @Override
    public int getItemCount() {
        return weatherRVModelList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView timeTV, temperatureTV, windSpeedTV;
        private ImageView weatherConditionIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            timeTV = itemView.findViewById(R.id.idTVTime);
            temperatureTV = itemView.findViewById(R.id.idTemperature);
            windSpeedTV = itemView.findViewById(R.id.idTVWindSpeed);
            weatherConditionIV = itemView.findViewById(R.id.idIVCondition);
        }
    }
}
