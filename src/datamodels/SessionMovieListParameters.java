package datamodels;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

public class SessionMovieListParameters {
    public static final String sessionKeyName = "MovieListParameters";
    public static final String sessionKeyBackPage = "WhichBackMovieList";
    public static final String parameterMovieTitle= "title";
    public static final String parameterMovieYear = "year";
    public static final String parameterMovieDirector = "directory";
    public static final String parameterStarName =  "star";
    public static final String parameterBrowsing = "browsing";
    public static final String parameterGenreName = "genre";
    public static final String parameterBrowseByLetter = "char";
    public static final String parameterOrderBy = "orderby";
    public static final String parameterCurrentPage = "p";
    public static final String parameterMoviesPerPage = "pp";
    public static final String parameterFullText = "fulltext";


    public static String parseParametersStringFromRequest(HttpServletRequest req){
        List<HttpRequestAttribute<String>> allHttpRequestAttributes = new ArrayList<>();
        allHttpRequestAttributes.add(new HttpRequestAttribute<>(String.class,parameterMovieTitle));
        allHttpRequestAttributes.add(new HttpRequestAttribute<>(String.class,parameterMovieYear));
        allHttpRequestAttributes.add(new HttpRequestAttribute<>(String.class,parameterMovieDirector));
        allHttpRequestAttributes.add(new HttpRequestAttribute<>(String.class,parameterStarName));
        allHttpRequestAttributes.add(new HttpRequestAttribute<>(String.class,parameterBrowsing));
        allHttpRequestAttributes.add(new HttpRequestAttribute<>(String.class,parameterGenreName));
        allHttpRequestAttributes.add(new HttpRequestAttribute<>(String.class,parameterBrowseByLetter));
        allHttpRequestAttributes.add(new HttpRequestAttribute<>(String.class,parameterOrderBy));
        allHttpRequestAttributes.add(new HttpRequestAttribute<>(String.class,parameterCurrentPage));
        allHttpRequestAttributes.add(new HttpRequestAttribute<>(String.class,parameterMoviesPerPage));
        allHttpRequestAttributes.add(new HttpRequestAttribute<>(String.class,parameterFullText));
        String result = "";
        boolean first = true;

        for (HttpRequestAttribute<String> currentRequest : allHttpRequestAttributes){
            String currentArgument = currentRequest.get(req);
            if (currentArgument!=null && !currentArgument.isEmpty()){
                if (first){
                    first = false;
                    result = "?";
                    //URL spaces should be replaced with +
                    result += currentRequest.name + "=" + currentArgument.replace(' ','+');
                }
                else{
                    result += "&" + currentRequest.name + "=" + currentArgument.replace(' ','+');
                }
            }
        }
        return result;
    }
}
