package datamodels.dbitems;

import java.util.List;

public class Movie {
    public String movieId;
    public String title;
    public int year;
    public String director;
    public List<String> genres;
    public String xmlId = null;

    public Movie(String movieId, String title, int year, String director,List<String> genres,String xmlId){
        this.movieId = movieId;
        this.title = title;
        this.year = year;
        this.director = director;
        this.genres = genres;
        this.xmlId = xmlId;
    }

    public String toString(){
        return "movieId: " + this.movieId +", " +
                "title: " + this.title + ", " +
                "year: " + this.year + ", " +
                "director: " + this.director + ", "+
                "genres: " + this.genres;
    }
    @Override
    public boolean equals(Object o){
        if (o.getClass() == Movie.class){
            Movie actual = (Movie) o;
            //Equality based on title, year, and director. Id or genres are not used
            return actual.title.equals(this.title) && actual.year==this.year && actual.director.equals(this.director);
        }
        return false;
    }

    @Override
    public int hashCode(){
        //Hash code is based on the same things used in equals()
        String allStrings = "" + this.title + this.year + this.director;
        return allStrings.hashCode();
    }

    public String generateDBIdFromHashCode(int offset){
        //Due to how many movies there are, it is very possible that two completely separate movies can have the same hash value. Offset is meant to counter this.
        return "tt"+((this.hashCode()+offset)%10000000);
    }
}
