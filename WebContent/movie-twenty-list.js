
function handleMovieListResult(resultData){
    console.log("Hey, the success callback function was called");

    let testBodyElement = jQuery("#test_body");

    for (let i = 0; i< resultData.length; i++){

        let name = "";
        name +=resultData[i]['movie_id'];
        name +=" ";
        testBodyElement.append(name);

    }
}

// Makes the HTTP GET request and registers on success callback function handleMovieListResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/top-20-movie-list", // Setting request url, which is mapped by MovieListSerLet in MovieListSerLet.java
    success: (resultData) => handleMovieListResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});