package edu.uci.ics.fabflixmobile.data.model;

import java.util.List;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String name;
    private final short year;
    private final String director;
    private final List<String> genres;
    private final List<String> stars;

    // Constructor
    public Movie(String name, short year, String director, List<String> genres, List<String> stars) {
        this.name = name;
        this.year = year;
        this.director = director;
        this.genres = genres;
        this.stars = stars;
    }

    // Getters
    public String getName() {
        return name;
    }

    public short getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }

    public List<String> getGenres() {
        return genres;
    }

    public List<String> getStars() {
        return stars;
    }
}