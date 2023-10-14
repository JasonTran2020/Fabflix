
function handleMovieListResult(resultData){
    console.log("Hey, the success callback function was called");

    let testBodyElement = jQuery("#test_body");

    for (let i = 0; i < Math.min(resultData.length,20); i++){

        let jsonObject=resultData[i];
        let entryHTML = "";

        //Breaking down the stars array of json objects into a single string of their names
        let stars = jsonObject["stars"];
        let genres = jsonObject["genres"]
        let starString = limitedListLinked(stars,3,"name","id","single-star.html?id=");
        let genreString =limitedList(genres,3,"name","id","single-movie.html?id=");

        entryHTML +='<dt class="headline-medium">' + (i+1) + ". " + "<a class=" + '"movie-title" ' + "href=single-movie.html?id=" + jsonObject["movie_id"] + ">" + jsonObject["title"]  + "</a>" +' ('+jsonObject["rating"] + ')' + "<dt/>";
        entryHTML += '<div >';
        entryHTML += "<dd class='headline-small'>" + "<strong>" + 'Directed by: ' + "</strong>" + jsonObject["director"] + "</dd>";
        entryHTML += "<dd class='headline-small'>" + "<strong>" + 'Release date: ' + "</strong>" + jsonObject["year"] + "</dd>";
        entryHTML += "<dd class='headline-small'>" + "<strong>" + 'Starring: ' + "</strong>" + starString + "<dd/>";
        entryHTML += "<dd class='headline-small'>" + "<strong>" + 'Genres: '  + "</strong>" + genreString + "<dd/>";
        entryHTML += '</div>'


        testBodyElement.append(entryHTML);

    }
}


function limitedList(theList,limit,propertyName){
    let result ="";
    for (let x = 0; x <Math.min(limit, theList.length);x++){

        let name = theList[x][propertyName];
        // if there is just one genre only to be displayed
        if (x === 0 && Math.min(limit, theList.length) === 1){
            result += name;
        }

        //Condition to put and at the end if the genres is within the limit
        else if (x === Math.min(limit, theList.length)-1 && theList.length <= limit){
            result+="and "+name;
        }
        // If there are more than 3 genres
        else if (x===2){
            result+= name + "...";
        }

        else if (x === Math.min(limit, theList.length)-2) {
            result += name + " ";
        }
        else {
            result+= name +", ";
        }
    }
    return result
}
//Yea this function is a bit specific. Only useful for making links that take one parameter
function limitedListLinked(theList,limit,propertyName,idName,endPoint){

    let result ="";
    for (let x = 0; x <Math.min(limit, theList.length);x++){

        let name = theList[x][propertyName];
        let id = theList[x][idName];
        //Condition to put and at the end
        let linkedName= "<a href=" + endPoint + id + ">" + name + "<a/>";
        if (x===limit-1 && theList.length===limit){
            result+="and "+linkedName;
        }
        else if (x===2){
            result+= linkedName + "...";
        }
        else{
            result+= linkedName +", ";
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