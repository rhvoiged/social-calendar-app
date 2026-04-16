package com.example.calendarapp.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.calendarapp.databinding.ItemEventBinding;
import com.example.calendarapp.model.Event;
import java.util.ArrayList;
import java.util.List;

    // maps events onto the calendar
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    public List<Event> events = new ArrayList<>();
    public OnEventClickListener listener;

    public interface OnEventClickListener { void onDeleteClick(Event event);}

    public void setEvents(List<Event> events) {this.events = events;notifyDataSetChanged();}

    @NonNull @Override public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {return new EventViewHolder(ItemEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));}

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.binding.tvEventTitle.setText(event.title);
        holder.binding.tvEventDescription.setText(event.description);
        holder.binding.tvEventDate.setText("Date: " + event.date);
        holder.binding.tvEventTime.setText("Time: " + event.startTime + " - " + event.endTime);
        holder.binding.tvWeather.setVisibility(View.GONE);
        holder.binding.btnDeleteEvent.setOnClickListener(v -> {if (listener != null) {listener.onDeleteClick(event);}});
    }

    @Override public int getItemCount() {
        return events.size(); 
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ItemEventBinding binding;
        EventViewHolder(ItemEventBinding b) {super(b.getRoot());this.binding = b;}
    }
}
