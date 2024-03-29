

function handleMovieResult(resultData){
    console.log("handleResult: populating movie info from resultData");
    let movieName = jQuery("#movie_name");
    movieName.append("<h1>" + "<strong>" + resultData[0]["movie_title"]  + "</strong>"  + ' ('+resultData[0]["rating"] + ')' + "<h1>" );
    //    starInfoElement.append("<h1>" +"<strong>" + resultData[0]["star_name"] + "</strong>" + "</h1>" +
    //         "<p>Date Of Birth: " + resultData[0]["star_dob"] + "</p>");

    console.log("handleResult: populating movie info from resultData");

    let entryHTML = "";

    let testBodyElement = jQuery("#movie_info")
    //Breaking down the stars array of json objects into a single string of their names
    let stars = resultData[0]["stars"];
    let genres = resultData[0]["genres"];
    let starString = getList(stars,"name", 1);
    // let genreString= getList(genres,"name", 0);
    let genreString = limitedListLinked(genres,-1,"name","name","movie-browse.html?browsing=true&genre=");
    entryHTML += "<tr>";
    entryHTML += "<div >";
    entryHTML += "<th class='headline-large'>" + resultData[0]["movie_director"] + "</th>";
    entryHTML += "<th class='headline-large'>" + resultData[0]["movie_year"] + "</th>";
    entryHTML += "<th class='headline-large'>" + genreString + "</th>";
    entryHTML += "<th class='headline-large'>" + starString + "</th>";
    entryHTML += "<th><button type='button' class='btn btn-outline-primary' id='" + resultData[0]["movie_id"] + "'>Add</button></th>";
    entryHTML += "<div/>";
    entryHTML += "</tr>";
    //         rowHTML += "<th class='headline-large'>" + '<a href="single-movie.html?id=' + resultData[0]["movies"][i]["movie_id"] + '">' + resultData[0]["movies"][i]["movie_title"] + "</a>" + "</th>";
    //         rowHTML += "<th class='headline-large'>" + resultData[0]["movies"][i]["movie_year"] + "</th>";
    //         rowHTML += "<th class='headline-large'>" + resultData[0]["movies"][i]["movie_director"] + "</th>";
    //         rowHTML += "<div/>"
    //         rowHTML += "</tr>";

    testBodyElement.append(entryHTML);

    testBodyElement.on("click", ".btn-outline-primary", handleAddButtonClick); // Event delegation
}

function getList(theList, propertyName) {
    let result = "";
    for (let x = 0; x < theList.length; x++) {
        let name = theList[x][propertyName];
        let link = '<a href="single-star.html?id=' + theList[x]["id"] + '">' + name + '</a>';

        if (x === theList.length - 1 && theList.length > 1) {
            // For the last item, add 'and' if there's more than one item in the list
            result += ' and ' + link;
        } else if (x > 0) {
            // For items other than the first, add a comma before the link
            result += ', ' + link;
        } else {
            // For the first item, just add the link
            result += link;
        }
    }
    return result;
}

let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleMovieListResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by MovieListSerLet in MovieListSerLet.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/remember-movie-parameters" ,
    success: (resultData) => handleJumpBackLink("#backlink",resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});


