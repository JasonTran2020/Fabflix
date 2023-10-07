
function handleMovieListResult(resultData){
    console.log("Hey, the success callback function was called");

    let testBodyElement = jQuery("#test_body");

    for (let i = 0; i < Math.min(resultData.length,20); i++){

        let jsonObject=resultData[i];
        let entryHTML = "";

        //Breaking down the stars array of json objects into a single string of their names
        let stars = jsonObject["stars"];
        let genres = jsonObject["genres"]
        let starString = limitedList(stars,3,"name");
        let genreString =limitedList(genres,3,"name");

        entryHTML +="<dt>" + jsonObject["title"] + ' ('+jsonObject["rating"] + ')' + "<dt/>";
        entryHTML += "<dd>" + 'Directed by: ' + jsonObject["director"] + "</dd>";
        entryHTML += "<dd>" + 'Release date: ' + jsonObject["year"] + "</dd>";
        entryHTML += "<dd>" + 'Starring: ' + starString + "<dd/>";
        entryHTML += "<dd>" + 'Genres: ' + genreString + "<dd/>";


        testBodyElement.append(entryHTML);

    }
}

function limitedList(theList,limit,propertyName){

    let result ="";
    for (let x = 0; x <Math.min(limit, theList.length);x++){

        let name = theList[x][propertyName]
        //Condition to put and at the end
        if (x===limit-1 && theList.length===limit){
            result+="and "+name;
        }
        else if (x===2){
            result+=name + "...";
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
    url: "api/top-20-movie-list", // Setting request url, which is mapped by MovieListSerLet in MovieListSerLet.java
    success: (resultData) => handleMovieListResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});