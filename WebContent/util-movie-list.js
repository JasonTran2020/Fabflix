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
    let page = parseInt(paramFromCurrent("p","1"));
    let perPage = parseInt(paramFromCurrent("pp","25"));

    for (let i = 0; i < resultData.length; i++){

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
        //Bit of a bunch of math for the movie position, but basically having to count based on what page we are plus how many movies are on each page, and then minus 1 because page 1 shouldn't count, and of course +1 because a movie at position 0 doesn't make much sense to a user
        entryHTML += "<td class='headline-medium'>" + "#" + ((perPage*(page-1))+i+1) + "</td>";
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

function buildSortingAndPaginationForm(formName){
    //Should probably combine this with buildPaginationLinks
    let inputFormElement = jQuery(formName);

    let entryHtml = "    <select name=\"pp\">\n" +
        "        <option value=\"10\">10</option>\n" +
        "        <option value=\"25\" selected=\"selected\">25</option>\n" +
        "        <option value=\"50\">50</option>\n" +
        "        <option value=\"100\">100</option>\n" +
        "    </select>\n" +
        "     results per page, sort by\n" +
        "    <select name=\"orderby\">\n" +
        "        <option value=\"rating\" selected=\"selected\">rating</option>\n" +
        "        <option value=\"title\">title</option>\n" +
        "    </select>\n" +
        "     in the\n" +
        "    <select name=\"direction\" >\n" +
        "        <option value=\"asc\">asc</option>\n" +
        "        <option value=\"desc\" selected=\"selected\">desc</option>\n" +
        "    </select>\n" +
        "     order.\n" +
        "    <input name='p' hidden='hidden' value='1'>"


    inputFormElement.append(entryHtml)


}

function buildPaginationLinks(containerName,parameterName){
    let currentPage= 1;
    let originalLink = window.location.href;
    let backLink = "";
    let nextLink = "";
    if (originalLink.includes("?")){
        let params = new URLSearchParams(window.location.search);
        if (params.get(parameterName)!=null){
            currentPage = parseInt(params.get(parameterName));
        }
        params.set(parameterName,currentPage-1);
        backLink = originalLink.slice(0,originalLink.indexOf("?"))+"?"+params.toString();
        params.set(parameterName,currentPage+1);
        nextLink = originalLink.slice(0,originalLink.indexOf("?")) +"?"+params.toString();
    }
    else{
        backLink += "?"+parameterName+"="+(currentPage-1);
        nextLink += "?"+parameterName+"="+(currentPage+1);
    }
    let containerElement = jQuery(containerName);
    let entryHtml = "";

    if (currentPage <= 1){
        entryHtml += "<span> &ltPrev </span>";
    }
    else{
        entryHtml += "<a href=" + backLink +">" + "&ltPrev " + "</a>";
    }

    entryHtml += "<a href=" + nextLink + ">" + " Next&gt" + "</a>";

    containerElement.append(entryHtml)

}

function paramFromCurrent(paramName,defaultValue){
    let params = new URLSearchParams(window.location.search);
    if (params.get(paramName)!=null){
        return params.get(paramName);
    }
    return defaultValue;
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
    let max = theList.length;
    //So use -1 as the limit if you don't want a limit
    if (limit!==-1){
        max = Math.min(limit, theList.length);
    }
    for (let x = 0; x <max;x++){

        let name = theList[x][propertyName];
        let id = theList[x][idName];
        //Condition to put and at the end
        let linkedName= "<a href=" + endPoint + id + ">" + name + "<a/>";
        //First item in the list
        if (x === 0){
            result += linkedName + " ";
        }
        //Last item in the list itself
        else if (x===max-1 && theList.length===max){
            result+=", and "+linkedName;
        }
        //Last item before we hit max but we still had more items in the list
        else if (x===(max-1) && max!==theList.length){
            result+= ", " + linkedName + "...";
        }
        // Items in the middle of the list
        else{
            result+= ", " + linkedName;
        }
    }
    return result

}
