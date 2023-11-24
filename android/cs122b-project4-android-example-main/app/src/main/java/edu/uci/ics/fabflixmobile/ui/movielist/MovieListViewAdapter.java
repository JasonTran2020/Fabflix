package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class MovieListViewAdapter extends ArrayAdapter<Movie> {
    private final ArrayList<Movie> movies;

    // View lookup cache
    private static class ViewHolder {
        TextView title;
        TextView year;
        TextView director;
        TextView genres;
        TextView stars;
    }

    public MovieListViewAdapter(Context context, ArrayList<Movie> movies) {
        super(context, R.layout.movielist_row, movies);
        this.movies = movies;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the movie item for this position
        Movie movie = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.movielist_row, parent, false);
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.year = convertView.findViewById(R.id.year);
            viewHolder.director = convertView.findViewById(R.id.director);
            viewHolder.genres = convertView.findViewById(R.id.genres);
            viewHolder.stars = convertView.findViewById(R.id.stars);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (movie != null) {
            viewHolder.title.setText(movie.getName());
            viewHolder.year.setText(String.valueOf(movie.getYear()));
            viewHolder.director.setText("Director: " + movie.getDirector());
            viewHolder.genres.setText("Genres: " + formatItemList(movie.getGenres()));
            viewHolder.stars.setText("Stars: " + formatItemList(movie.getStars()));
        }

        // Return the completed view to render on screen
        return convertView;
    }

    // Helper method to format the list of items into a comma-separated string
    private String formatItemList(List<String> items) {
        // Join the first 3 items or fewer if not enough items are present
        List<String> firstThreeItems = items.subList(0, Math.min(items.size(), 3));
        return TextUtils.join(", ", firstThreeItems);
    }
}