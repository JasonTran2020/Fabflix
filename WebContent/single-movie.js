
function handleMovieResult(resultData){
    console.log("Hey, the success callback function was called");

    let testBodyElement = jQuery("#test_body");

    let jsonObject=resultData[i];
    let entryHTML = "";

    //Breaking down the stars array of json objects into a single string of their names
    let stars = jsonObject["stars"];
    let genres = jsonObject["genres"]
    let starString = getList(stars,"name");
    let genreString= getList(genres,"name");

    entryHTML +="<dt>" + jsonObject["title"] + ' ('+jsonObject["rating"] + ')' + "<dt/>";
    entryHTML += "<dd>" + 'Directed by: ' + jsonObject["director"] + "</dd>";
    entryHTML += "<dd>" + 'Release date: ' + jsonObject["year"] + "</dd>";
    entryHTML += "<dd>" + 'Rating: ' + jsonObject["rating"] + "<dd/>";
    entryHTML += "<dd>" + 'Starring: ' + starString + "<dd/>";
    entryHTML += "<dd>" + 'Genres: ' + genreString + "<dd/>";


    testBodyElement.append(entryHTML);


}

function getList(theList, propertyName){
    let result ="";
    for (let x = 0; x < theList.length;x++){

        let name = theList[x][propertyName]
        //Condition to put and at the end for last star/genre
        if (x == theList.length - 1){
            result+="and "+name;
        }
        else{
            result+=name+", ";
        }
    }
    return result

}



// Makes the HTTP GET request and registers on success callback function handleMovieListResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/single-movie", // Setting request url, which is mapped by MovieListSerLet in MovieListSerLet.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});