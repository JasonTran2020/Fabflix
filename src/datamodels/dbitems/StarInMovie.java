package datamodels.dbitems;

import java.util.Objects;

public class StarInMovie {
    public String xmlMovieId;
    public String xmlStarId;

    public StarInMovie(String xmlMovieId, String xmlStarId) {
        this.xmlMovieId = xmlMovieId;
        this.xmlStarId = xmlStarId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StarInMovie that = (StarInMovie) o;
        return Objects.equals(xmlMovieId, that.xmlMovieId) && Objects.equals(xmlStarId, that.xmlStarId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(xmlMovieId, xmlStarId);
    }
    @Override
    public String toString() {
        return "StarInMovie{" +
                "xmlMovieId='" + xmlMovieId + '\'' +
                ", xmlStarId='" + xmlStarId + '\'' +
                '}';
    }
}
