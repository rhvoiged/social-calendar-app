package com.example.calendarapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.calendarapp.MainActivity;
import com.example.calendarapp.data.Repository;
import com.example.calendarapp.data.SessionManager;
import com.example.calendarapp.databinding.ActivityRegisterBinding;
import com.example.calendarapp.model.User;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Activitatea de înregistrare pentru noi utilizatori.
 * Gestionează crearea contului și salvarea sesiunii inițiale.
 */
public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private Repository repository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        repository = new Repository();
        sessionManager = new SessionManager(this);
        
        binding.btnRegister.setOnClickListener(v -> register());
        binding.tvBackToLogin.setOnClickListener(v -> finish());
    }

    // process register request
    private void register() {
        String n = binding.etName.getText().toString().trim();
        String e = binding.etEmail.getText().toString().trim(); 
        String p = binding.etPassword.getText().toString().trim();
        String cp = binding.etConfirmPassword.getText().toString().trim();

        if (n.isEmpty() || e.isEmpty() || p.isEmpty()) {Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();return;}
        if (p.length() < 6) {binding.etPassword.setError("Min 6 characters");return;}
        if (!p.equals(cp)) {binding.etConfirmPassword.setError("Passwords do not match");return;}

        binding.progressBar.setVisibility(View.VISIBLE);
        User u = new User(null, e, n); 
        u.password = p;
        
        repository.register(u, new Repository.RepositoryCallback<User>() {
            @Override 
            public void onSuccess(User res) {
                binding.progressBar.setVisibility(View.GONE);
                sessionManager.saveUser(res.id, res.email, res.name);
                
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {repository.updateFcmToken(res.id, task.getResult());}
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
            }
            @Override 
            public void onError(String err) {
                binding.progressBar.setVisibility(View.GONE); 
                Toast.makeText(RegisterActivity.this, "Registration failed: " + err, Toast.LENGTH_LONG).show(); 
            }
        });
    }
}
