package edu.uci.ics.fabflixmobile.ui.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;
import edu.uci.ics.fabflixmobile.ui.mainpage.MainPageActivity; // Assuming you have this activity
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONException;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private TextView message;

    private final String host = "18.219.59.225";
    private final String port = "8443";
    private final String domain = "project1";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        username = binding.username;
        password = binding.password;
        message = binding.message;
        final Button loginButton = binding.login;

        loginButton.setOnClickListener(view -> login());
    }

    @SuppressLint("SetTextI18n")
    public void login() {
        message.setText("Trying to login");
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final StringRequest loginRequest = new StringRequest(
                Request.Method.POST,
                baseURL + "/api/login",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        if ("success".equals(status)) {
                            // Login successful, start MainPageActivity
                            Intent mainPageIntent = new Intent(LoginActivity.this, MainPageActivity.class);
                            startActivity(mainPageIntent);
                            finish();  // Close LoginActivity
                        } else {
                            // Login failed, show error message
                            String message = jsonResponse.getString("message");
                            this.message.setText(message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        this.message.setText("Error parsing response.");
                    }
                },
                error -> {
                    // Network error
                    Log.e("login.error", error.toString());
                    this.message.setText("Network error: " + error.getMessage());
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                // POST request form data
                final Map<String, String> params = new HashMap<>();
                params.put("username", username.getText().toString());
                params.put("password", password.getText().toString());
                return params;
            }
        };

        queue.add(loginRequest);
    }
}