//Due to SearchMovie also performing very similarily to MovieTwenty, I moved these functions in a standalone file
//Therefore, this file itself does nothing, it just provideds methods for creating a html table
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleMovieListResult(resultData, tableName){
    console.log("Hey, the success callback function was called");

    let testBodyElement = jQuery(tableName);
    //Empty out the element of its children. Could be useful for the SearchServlet depending on implementation
    testBodyElement.empty()

    for (let i = 0; i < Math.min(resultData.length,20); i++){

        let jsonObject=resultData[i];
        let entryHTML = "";

        //Breaking down the stars array of json objects into a single string of their names
        let stars = jsonObject["stars"];
        let genres = jsonObject["genres"]
        let starString = limitedListLinked(stars,3,"name","id","single-star.html?id=");
        let genreString =limitedListLinked(genres,3,"name","name","movie-browse.html?browsing=true&genre=");

        //Old way that used description list. Honestly not that good given the lack of a movie image, so all the content ends up being on the left with an empty center and right
        //Could center it, but doesn't make a ton of sense either given the lack of contenent and yet huge amount of vertical space taken
        // entryHTML +='<dt class="headline-medium">' + (i+1) + ". " + "<a class=" + '"movie-title" ' + "href=single-movie.html?id=" + jsonObject["movie_id"] + ">" + jsonObject["title"]  + "</a>" +' ('+jsonObject["rating"] + ')' + "<dt/>";
        // entryHTML += '<div >';
        // entryHTML += "<dd class='headline-small'>" + "<strong>" + 'Directed by: ' + "</strong>" + jsonObject["director"] + "</dd>";
        // entryHTML += "<dd class='headline-small'>" + "<strong>" + 'Release date: ' + "</strong>" + jsonObject["year"] + "</dd>";
        // entryHTML += "<dd class='headline-small'>" + "<strong>" + 'Starring: ' + "</strong>" + starString + "<dd/>";
        // entryHTML += "<dd class='headline-small'>" + "<strong>" + 'Genres: '  + "</strong>" + genreString + "<dd/>";
        // entryHTML += '</div>'
        entryHTML += "<tr>";
        entryHTML += "<td class='headline-medium'>" + "#" + (i+1) + "</td>";
        entryHTML += "<td class='headline-medium'>" + "<a class=" + '"movie-title" ' + "href=single-movie.html?id=" + jsonObject["movie_id"] + ">" + jsonObject["title"]  + "</a>" + "</td>";
        entryHTML += "<td class='headline-medium'>" + "&#9733 "+jsonObject["rating"] + "</td>";
        entryHTML += "<td class='headline-medium'>" + jsonObject["year"] + "</td>";
        entryHTML += "<td class='headline-medium'>" + jsonObject["director"] + "</td>";
        entryHTML += "<td class='headline-medium'>" + genreString + "</td>";
        entryHTML += "<td class='headline-medium'>" + starString + "</td>";

        entryHTML += "</tr>";



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