package edu.uci.ics.fabflixmobile.ui.mainpage;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivityMainpageBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.view.View;

public class MainPageActivity extends AppCompatActivity {

    private EditText searchBox;
    private final String host = "18.219.59.225";
    private final String port = "8443";
    private final String domain = "project1";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainpageBinding binding = ActivityMainpageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        searchBox = binding.searchBox;
        final Button searchButton = binding.searchButton;

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve the search query at the moment the button is clicked
                String query = searchBox.getText().toString();
                performSearch(query); // Perform the search with the current query
            }
        });
    }
    private void performSearch(String query) {
        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        // URL encode the query
        final String encodedQuery = Uri.encode(query);

        // Prepare the search URL
        final String searchURL = baseURL + "/api/search-movie?title=" + encodedQuery + "&fulltext=true";

        // Request a string response from the provided URL
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                searchURL,
                response -> {
                    try {
                        // Parse the JSON response
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray moviesArray = jsonResponse.getJSONArray("movies");

                        // Assuming you want to send the entire movies array to the MovieListActivity
                        Intent movieListIntent = new Intent(MainPageActivity.this, MovieListActivity.class);
                        movieListIntent.putExtra("movies", moviesArray.toString());
                        startActivity(movieListIntent);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainPageActivity.this, "Error parsing JSON data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Log and display any errors
                    Log.e("search.error", error.toString());
                    Toast.makeText(MainPageActivity.this, "Search error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );


        queue.add(searchRequest);
    }
}