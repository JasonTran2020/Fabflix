package datamodels.dbitems;

import java.util.Objects;

//Synonymous to actor
public class Star {
    public String starId;
    public String name;
    public Integer birthYear;

    public Star(String starId, String name, int birthYear) {
        this.starId = starId;
        this.name = name;
        this.birthYear = birthYear;
    }

    @Override
    public String toString() {
        return "Star{" +
                "starId='" + starId + '\'' +
                ", name='" + name + '\'' +
                ", birthYear=" + birthYear +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Star star = (Star) o;
        return birthYear == star.birthYear && Objects.equals(name, star.name);
    }
    @Override
    public int hashCode() {
        String allString = "" + name + birthYear;
        return allString.hashCode();
    }

    public String generateDBIdFromHashCode(int offset){
        return "tt"+((this.hashCode()+offset)%10000000);
    }
}
