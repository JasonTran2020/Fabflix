package edu.uci.ics.fabflixmobile.ui.mainpage;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import edu.uci.ics.fabflixmobile.R;

public class MainPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainpage);

        EditText searchBox = findViewById(R.id.searchBox);
        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(v.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void performSearch(String query) {
        // Implement your search logic here
        // Example: Send a request to your backend server and handle the response
        Toast.makeText(getApplicationContext(), "Searching for: " + query, Toast.LENGTH_SHORT).show();
    }
}