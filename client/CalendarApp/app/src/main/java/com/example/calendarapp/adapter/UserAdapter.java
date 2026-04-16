package com.example.calendarapp.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.calendarapp.databinding.ItemUserBinding;
import com.example.calendarapp.model.User;
import java.util.ArrayList;
import java.util.List;

 // friends lists in 3 modes via flags: request list, search mode, friends list
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    public List<User> users = new ArrayList<>();
    public OnUserClickListener listener;
    public OnActionClickListener actionListener;
    public OnUserLongClickListener longClickListener;
    

    public boolean isRequestList = false;    // friend request list - accept / decline
    public boolean isSearchMode = false;    // search mode list


    public interface OnUserClickListener { void onUserClick(User user);} // click to view calendar
    public interface OnUserLongClickListener { void onUserLongClick(User user);}    // long press to delete friend
    public interface OnActionClickListener { void onAccept(User user);void onDecline(User user);}   // accept/decline friend request

    public void setUsers(List<User> users) {this.users = users;notifyDataSetChanged();}

    @NonNull @Override public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {return new UserViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));}

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.binding.tvUserName.setText(user.name);

        if (isRequestList) {
            holder.binding.btnAction1.setVisibility(View.VISIBLE);
            holder.binding.btnAction2.setVisibility(View.VISIBLE);
            holder.binding.btnViewCalendar.setVisibility(View.GONE);
            holder.binding.btnAction1.setOnClickListener(v -> {if (actionListener != null) {actionListener.onAccept(user);}});
            holder.binding.btnAction2.setOnClickListener(v -> {if (actionListener != null) {actionListener.onDecline(user);}
            });
        } else if (isSearchMode) {
            holder.binding.btnAction1.setVisibility(View.GONE);
            holder.binding.btnAction2.setVisibility(View.GONE);
            holder.binding.btnViewCalendar.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v -> {if (listener != null) {listener.onUserClick(user);}});
        } else {
            holder.binding.btnAction1.setVisibility(View.GONE);
            holder.binding.btnAction2.setVisibility(View.GONE);
            holder.binding.btnViewCalendar.setVisibility(View.VISIBLE);
            holder.binding.btnViewCalendar.setOnClickListener(v -> {if (listener != null) {listener.onUserClick(user);}});
            holder.itemView.setOnLongClickListener(v -> {if (longClickListener != null) {longClickListener.onUserLongClick(user);return true;}return false;});
        }
    }
    @Override public int getItemCount() {return users.size();}

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ItemUserBinding binding;
        UserViewHolder(ItemUserBinding b) {super(b.getRoot());this.binding = b;}
    }
}
