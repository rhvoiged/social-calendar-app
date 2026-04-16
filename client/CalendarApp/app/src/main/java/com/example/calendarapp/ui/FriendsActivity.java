package com.example.calendarapp.ui;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.calendarapp.adapter.UserAdapter;
import com.example.calendarapp.data.Repository;
import com.example.calendarapp.data.SessionManager;
import com.example.calendarapp.databinding.ActivityFriendsBinding;
import com.example.calendarapp.model.User;
import java.util.ArrayList;
import java.util.List;

// view friend list / friend requests / searching for new friends

public class FriendsActivity extends AppCompatActivity {
    private ActivityFriendsBinding binding;
    private Repository repository;
    private UserAdapter friendsAdapter, requestsAdapter;
    private Integer currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        repository = new Repository();
        currentUserId = new SessionManager(this).getUserId();
        
        friendsAdapter = new UserAdapter();
        friendsAdapter.listener = f -> showFriendOptions(f);
        friendsAdapter.longClickListener = f -> showDeleteFriendDialog(f);
        
        binding.rvFriends.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFriends.setAdapter(friendsAdapter);

        requestsAdapter = new UserAdapter();
        requestsAdapter.isRequestList = true;
        requestsAdapter.actionListener = new UserAdapter.OnActionClickListener() {
            @Override public void onAccept(User u) {
                acceptFriend(u.id); 
            }
            @Override public void onDecline(User u) {
                declineFriend(u.id); 
            }
        };
        
        binding.rvRequests.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRequests.setAdapter(requestsAdapter);

        refreshData();
        binding.fabAddFriend.setOnClickListener(v -> showSearchUserDialog());
    }

    // refreshes the friend list and friend requests list (used after accepting/declining a friend request)
    private void refreshData() {
        if (currentUserId == null) {return;}
        
        binding.progressBar.setVisibility(View.VISIBLE);
        repository.getFriends(currentUserId, new Repository.RepositoryCallback<List<User>>() {
            @Override public void onSuccess(List<User> r) {binding.progressBar.setVisibility(View.GONE);friendsAdapter.setUsers(r);}
            @Override public void onError(String e) {binding.progressBar.setVisibility(View.GONE);}
        });
        
        repository.getFriendRequests(currentUserId, new Repository.RepositoryCallback<List<User>>() {
            @Override 
            public void onSuccess(List<User> r) {
                if (r != null && !r.isEmpty()) { 
                    binding.layoutRequests.setVisibility(View.VISIBLE); 
                    requestsAdapter.setUsers(r); 
                } else {binding.layoutRequests.setVisibility(View.GONE);}
            }
            @Override public void onError(String e) {binding.layoutRequests.setVisibility(View.GONE);}
        });
    }

    // accepts friend request
    private void acceptFriend(Integer rid) {
        repository.acceptFriend(currentUserId, rid, new Repository.RepositoryCallback<Void>() {
            @Override public void onSuccess(Void r) {Toast.makeText(FriendsActivity.this, "Friend accepted!", Toast.LENGTH_SHORT).show();refreshData();}
            @Override public void onError(String e) {Toast.makeText(FriendsActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();}
        });
    }

    // declines friend request
    private void declineFriend(Integer rid) {
        repository.declineFriend(currentUserId, rid, new Repository.RepositoryCallback<Void>() {
            @Override public void onSuccess(Void r) {Toast.makeText(FriendsActivity.this, "Friend declined", Toast.LENGTH_SHORT).show();refreshData();}
            @Override public void onError(String e) {Toast.makeText(FriendsActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();}
        });
    }

    // confirmation dialog when deleting a friend
    private void showDeleteFriendDialog(User f) {new AlertDialog.Builder(this).setTitle("Remove Friend").setMessage("Are you sure you want to remove " + f.name + "?").setPositiveButton("Remove", (d, w) -> removeFriend(f.id)).setNegativeButton("Cancel", null).show();}

    // deletes a friend
    private void removeFriend(Integer fid) {
        binding.progressBar.setVisibility(View.VISIBLE);
        repository.removeFriend(currentUserId, fid, new Repository.RepositoryCallback<Void>() {
            @Override public void onSuccess(Void r) {binding.progressBar.setVisibility(View.GONE);refreshData();}
            @Override 
            public void onError(String e) { 
                binding.progressBar.setVisibility(View.GONE); 
                Toast.makeText(FriendsActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show(); 
            }
        });
    }

    // option to choose between viewing a friend's calendar or overlapping events
    private void showFriendOptions(User f) {
        String[] options = {"View Calendar", "See Overlap"};
        new AlertDialog.Builder(this).setTitle(f.name).setItems(options, (dialog, index) -> {
                    Intent intent = new Intent(this, index == 0 ? FriendCalendarActivity.class : OverlapActivity.class);
                    intent.putExtra("FRIEND_ID", f.id); 
                    intent.putExtra("FRIEND_NAME", f.name);
                    startActivity(intent);
                }).show();
    }

    // searching for new users
    private void showSearchUserDialog() {
        LinearLayout layout = new LinearLayout(this); 
        layout.setOrientation(LinearLayout.VERTICAL); 
        layout.setPadding(50, 40, 50, 10);
        
        EditText et = new EditText(this); 
        et.setHint("Search by name");
        layout.addView(et);
        
        RecyclerView rv = new RecyclerView(this); 
        rv.setLayoutParams(new LinearLayout.LayoutParams(-1, 600)); 
        layout.addView(rv);
        
        UserAdapter adapter = new UserAdapter(); 

        adapter.isSearchMode = true; // search mode removes action buttons

        rv.setLayoutManager(new LinearLayoutManager(this)); 
        rv.setAdapter(adapter);
        
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Find New Friends").setView(layout).setNegativeButton("Close", null).create();
                
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override 
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                String q = s.toString().trim();
                if (q.length() >= 1) {
                    repository.searchUsers(q, currentUserId, new Repository.RepositoryCallback<List<User>>() {
                        @Override 
                        public void onSuccess(List<User> r) {
                            List<User> filtered = new ArrayList<>(); 
                            if (r != null) {for (User u : r) {if (!u.id.equals(currentUserId)) {filtered.add(u);}}}
                            adapter.setUsers(filtered);
                        }
                        @Override public void onError(String e) {}
                    });
                } else {adapter.setUsers(new ArrayList<>());}
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        adapter.listener = u -> new AlertDialog.Builder(this).setTitle("Add Friend").setMessage("Do you want to add " + u.name + "?").setPositiveButton("Send Request", (d, w) -> {addFriendRequest(u.id);dialog.dismiss();}).setNegativeButton("No", null).show();dialog.show();
    }

    // sends friend request
    private void addFriendRequest(Integer fid) {
        repository.addFriend(currentUserId, fid, new Repository.RepositoryCallback<Void>() {
            @Override public void onSuccess(Void r) {Toast.makeText(FriendsActivity.this, "Request sent!", Toast.LENGTH_SHORT).show();}
            @Override public void onError(String e) {Toast.makeText(FriendsActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();}
        });
    }
}
