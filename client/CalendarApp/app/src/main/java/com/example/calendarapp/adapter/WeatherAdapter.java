package com.example.calendarapp.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.calendarapp.databinding.ItemWeatherBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// converts from raw data to easy to read forecast
public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {

    private List<Map<String, String>> weatherList = new ArrayList<>();

    public void setWeatherList(List<Map<String, String>> weatherList) {this.weatherList = weatherList;notifyDataSetChanged();}

    @NonNull @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWeatherBinding binding = ItemWeatherBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new WeatherViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        Map<String, String> item = weatherList.get(position);
        String date = item.get("date");
        String info = item.get("info");

        holder.binding.tvWeatherDate.setText(date);

        if (info != null && !info.isEmpty()) {
            String emoji = getWeatherEmoji(info.toLowerCase());
            holder.binding.tvWeatherDesc.setText(emoji + " " + info.split(",")[0].trim());
            
            if (info.contains(",")) {holder.binding.tvWeatherTemp.setText(info.split(",")[1].trim());}
            else {holder.binding.tvWeatherTemp.setText("");}
        }
        else {
            holder.binding.tvWeatherDesc.setText("No data");
            holder.binding.tvWeatherTemp.setText("-");
        }

        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {((ViewGroup.MarginLayoutParams) lp).setMargins(16, 8, 16, 8);}
        holder.itemView.setLayoutParams(lp);
    }

    private String getWeatherEmoji(String desc) {
        if (desc.contains("clear")) {return "☀️";}
        if (desc.contains("cloud")) {return "☁️";}
        if (desc.contains("rain")) {return "🌧️";}
        if (desc.contains("thunder")) {return "⛈️";}
        if (desc.contains("snow")) {return "❄️";}
        if (desc.contains("mist") || desc.contains("fog")) {return "🌫️";}
        return "🌡️";
    }

    @Override public int getItemCount() {
        return weatherList.size();
    }

    static class WeatherViewHolder extends RecyclerView.ViewHolder {
        ItemWeatherBinding binding;
        WeatherViewHolder(ItemWeatherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
