package edu.uci.ics.fabflixmobile.ui.movielist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MovieListActivity extends AppCompatActivity {

    private static final int PAGE_SIZE = 10;
    private ArrayList<Movie> allMovies;
    private int currentPage = 0;
    private Button previousButton;
    private Button nextButton;
    private ListView listView;
    private MovieListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);

        listView = findViewById(R.id.list);
        previousButton = findViewById(R.id.previousButton);
        nextButton = findViewById(R.id.nextButton);
        allMovies = new ArrayList<>();

        setupButtonListeners();
        fetchMoviesFromJson();
        setupListViewClickListener();
    }

    private void setupButtonListeners() {
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage > 0) {
                    currentPage--;
                    updateListViewForPage(currentPage);
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((currentPage + 1) * PAGE_SIZE < allMovies.size()) {
                    currentPage++;
                    updateListViewForPage(currentPage);
                }
            }
        });
    }

    private void setupListViewClickListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie selectedMovie = (Movie) parent.getItemAtPosition(position);
                Intent intent = new Intent(MovieListActivity.this, edu.uci.ics.fabflixmobile.ui.singlemovie.SingleMovieActivity.class);
                Gson gson = new Gson();
                String movieJson = gson.toJson(selectedMovie);
                intent.putExtra("movie", movieJson);
                startActivity(intent);
            }
        });
    }

    private void fetchMoviesFromJson() {
        String moviesJson = getIntent().getStringExtra("movies");
        try {
            JSONArray moviesArray = new JSONArray(moviesJson);
            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject movieJson = moviesArray.getJSONObject(i);
                Movie movie = createMovieFromJson(movieJson);
                if (movie != null) {
                    allMovies.add(movie);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing movie data", Toast.LENGTH_LONG).show();
        }

        updateListViewForPage(currentPage);
    }

    private void updateListViewForPage(int page) {
        int startIndex = page * PAGE_SIZE;
        int endIndex = Math.min((page + 1) * PAGE_SIZE, allMovies.size());

        ArrayList<Movie> pageMovies = new ArrayList<>(allMovies.subList(startIndex, endIndex));
        adapter = new MovieListViewAdapter(this, pageMovies);
        listView.setAdapter(adapter);

        previousButton.setEnabled(page > 0);
        nextButton.setEnabled(endIndex < allMovies.size());
    }

    private Movie createMovieFromJson(JSONObject movieJson) {
        try {
            String title = movieJson.getString("title");
            short year = (short) movieJson.getInt("year");
            String director = movieJson.getString("director");

            List<String> genres = extractNames(movieJson.getJSONArray("genres"));
            List<String> stars = extractNames(movieJson.getJSONArray("stars"));

            return new Movie(title, year, director, genres, stars);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<String> extractNames(JSONArray jsonArray) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                names.add(jsonArray.getJSONObject(i).getString("name"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return names;
    }
}