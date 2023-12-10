package edu.uci.ics.fabflixmobile.ui.singlemovie;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import android.text.TextUtils;

public class SingleMovieActivity extends AppCompatActivity {

    private TextView movieTitleTextView;
    private TextView movieYearTextView;
    private TextView movieDirectorTextView;
    private TextView movieGenresTextView;
    private TextView movieStarsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlemovie);

        movieTitleTextView = findViewById(R.id.movieTitle);
        movieYearTextView = findViewById(R.id.movieYear);
        movieDirectorTextView = findViewById(R.id.movieDirector);
        movieGenresTextView = findViewById(R.id.movieGenres);
        movieStarsTextView = findViewById(R.id.movieStars);

        // Get the movie JSON string passed from MovieListActivity
        String movieJson = getIntent().getStringExtra("movie");
        // Deserialize the JSON string back into a Movie object
        Gson gson = new Gson();
        Movie movie = gson.fromJson(movieJson, Movie.class);

        // Populate the TextViews with the movie information
        if (movie != null) {
            movieTitleTextView.setText(movie.getName());
            movieYearTextView.setText(String.valueOf(movie.getYear()));
            movieDirectorTextView.setText(movie.getDirector());
            movieGenresTextView.setText(TextUtils.join(", ", movie.getGenres())); // Displays all genres
            movieStarsTextView.setText(TextUtils.join(", ", movie.getStars()));   // Displays all stars
        }
    }
}