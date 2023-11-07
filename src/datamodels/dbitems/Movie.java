package datamodels.dbitems;

import java.util.List;

public class Movie {
    public String movieId;
    public String title;
    public int year;
    public String director;
    public List<String> genres;

    public Movie(String movieId, String title, int year, String director,List<String> genres){
        this.movieId = movieId;
        this.title = title;
        this.year = year;
        this.director = director;
        this.genres = genres;
    }

    public String toString(){
        return "movieId: " + this.movieId +", " +
                "title: " + this.title + ", " +
                "year: " + this.year + ", " +
                "director: " + this.director + ", "+
                "genres: " + this.genres;
    }
}
