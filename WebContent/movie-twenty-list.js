jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/top-20-movie-list", // Setting request url, which is mapped by MovieListSerLet in MovieListSerLet.java
    success: (resultData) => handleMovieListResult(resultData, "#movie_table") // Setting callback function to handle data returned successfully by the StarsServlet
});