package edu.uci.ics.fabflixmobile.ui.movielist;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class MovieListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);

        ArrayList<Movie> movies = new ArrayList<>();
        String moviesJson = getIntent().getStringExtra("movies");
        try {
            JSONArray moviesArray = new JSONArray(moviesJson);
            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject movieJson = moviesArray.getJSONObject(i);
                Movie movie = createMovieFromJson(movieJson);
                if (movie != null) {
                    movies.add(movie);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing movie data", Toast.LENGTH_LONG).show();
        }

        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
    }

    private Movie createMovieFromJson(JSONObject movieJson) {
        try {
            String title = movieJson.getString("title");
            short year = (short) movieJson.getInt("year");
            String director = movieJson.getString("director");

            List<String> genres = extractNames(movieJson.getJSONArray("genres"), 3);
            List<String> stars = extractNames(movieJson.getJSONArray("stars"), 3);

            return new Movie(title, year, director, genres, stars);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Helper method to extract up to 'limit' names from a JSONArray (always 3)
    private List<String> extractNames(JSONArray jsonArray, int limit) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < jsonArray.length() && i < limit; i++) {
            try {
                names.add(jsonArray.getJSONObject(i).getString("name"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return names;
    }
}