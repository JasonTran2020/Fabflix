
let url = window.location.href;
let index = url.indexOf("?")
let parameters=""
//Needed due to potential bug where there is no ? and we slice at -1
if (index!=-1){
    parameters = url.slice(index)
}

buildSortingAndPaginationForm("#sorting-form");
buildPaginationLinks("#paginator", "p")

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/top-20-movie-list" + parameters, // Setting request url, which is mapped by MovieListSerLet in MovieListSerLet.java
    success: (resultData) => handleMovieListResult(resultData, "#movie_table") // Setting callback function to handle data returned successfully by the StarsServlet
});