package com.example.calendarapp.ui;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.calendarapp.MainActivity;
import com.example.calendarapp.data.Repository;
import com.example.calendarapp.data.SessionManager;
import com.example.calendarapp.databinding.ActivityLoginBinding;
import com.example.calendarapp.model.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

// initializes cloud services and navigates to main activity

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private Repository repository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new Repository();
        sessionManager = new SessionManager(this);

        if (sessionManager.getUserId() != null) {goToMain();return;}

        binding.btnLogin.setOnClickListener(v -> login());
        binding.tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    // process login request
    private void login() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();return;}

        binding.progressBar.setVisibility(View.VISIBLE);
        repository.login(email, password, new Repository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                binding.progressBar.setVisibility(View.GONE);
                if (user == null || user.id == null) {return;}
                sessionManager.saveUser(user.id, user.email, user.name);
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {repository.updateFcmToken(user.id, task.getResult());}
                    goToMain();
                });
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // goes to main activity and blocks back button
    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
