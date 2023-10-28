
function handleMovieResult(resultData){
    console.log("Hey, the success callback function was called");
    let movieName = jQuery("#movie_name");
    movieName.append("<strong>" + resultData[0]["movie_title"] + "</strong>" + ' ('+resultData[0]["rating"] + ')' );

    let entryHTML = "";

    let testBodyElement = jQuery("#movie_info")
    //Breaking down the stars array of json objects into a single string of their names
    let stars = resultData[0]["stars"];
    let genres = resultData[0]["genres"]
    let starString = getList(stars,"name", 1);
    // let genreString= getList(genres,"name", 0);
    let genreString = limitedListLinked(genres,-1,"name","name","movie-browse.html?browsing=true&genre=");

    entryHTML += "<dd>" + 'Directed by: ' + resultData[0]["movie_director"] + "</dd>";
    entryHTML += "<dd>" + 'Release date: ' + resultData[0]["movie_year"] + "</dd>";
    entryHTML += "<dd>" + 'Genres: ' + genreString + "<dd/>";
    entryHTML += "<dd>" + 'Starring: ' + starString + "<dd/>";


    testBodyElement.append(entryHTML);


}

function getList(theList, propertyName, flag){
    let result ="";
    for (let x = 0; x < theList.length;x++){

        let name = theList[x][propertyName]

        if (x == 0 &&  theList.length == 1) {
            result += name;
        }
        //Condition to put and at the end for last star/genre
        else if (x == theList.length - 1){
            if (flag ==  1) {
                result += "and ";
                result += '<a href="single-star.html?id=' + theList[x]["id"] + '">' + name + "</a>";
            }
            else{
                result+="and "+name;
            }

        }
        else{
            if (flag == 1) {
                result += '<a href="single-star.html?id=' + theList[x]["id"] + '">' + name + "</a>";
                result += ", ";
            }
            else {
                if (x == theList.length - 2){
                    result += name + " ";
                }
                else {
                    result += name + ", ";
                }
            }
        }
    }
    return result

}

let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleMovieListResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by MovieListSerLet in MovieListSerLet.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});


