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

function handleAddButtonClick(event) {
    // Get the movie_id from the button's id attribute
    let movieId = event.target.id;
    console.log("Add button clicked for movie: " + movieId);
    addToCart(event, movieId);

}
function handleMovieListResult(resultData, tableName){

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
        entryHTML += "<td><button type='button' class='btn btn-outline-primary' id='" + resultData[i]["movie_id"] + "'>Add</button></td>";
        entryHTML += "</tr>";



        testBodyElement.append(entryHTML);

    }
    testBodyElement.on("click", ".btn-outline-primary", handleAddButtonClick); // Event delegation

}

function buildSortingAndPaginationForm(formName){
    //Should probably combine this with buildPaginationLinks
    let inputFormElement = jQuery(formName);

    let entryHtml = "    <select id=\"pp\" name=\"pp\">\n" +
        "        <option value=\"10\">10</option>\n" +
        "        <option value=\"25\" selected=\"selected\">25</option>\n" +
        "        <option value=\"50\">50</option>\n" +
        "        <option value=\"100\">100</option>\n" +
        "    </select>\n" +
        "     results per page, sort by\n" +
        "    <select id=\"orderby\" name=\"orderby\">\n" +
        "        <option value=\"rata\">rating asc, title asc</option>\n" +
        "        <option value=\"ratd\">rating asc, title desc</option>\n" +
        "        <option value=\"rdta\" selected=\"selected\" >rating desc, title asc</option>\n" +
        "        <option value=\"rdtd\">rating desc, title desc</option>\n" +
        "        <option value=\"tara\">title asc, rating asc</option>\n" +
        "        <option value=\"tard\">title asc, rating desc</option>\n" +
        "        <option value=\"tdra\">title desc, rating asc</option>\n" +
        "        <option value=\"tdrd\">title desc, rating desc</option>\n" +

        "     order.\n" +
        "    <input name='p' hidden='hidden' value='1'>";


    inputFormElement.append(entryHtml)


}

function populateSortingAndPaginationForm(){
    //Cannot use populateInput as these select elements
    let ppParam=getParameterByName("pp");
    if (ppParam!==""){
        jQuery("option[value="+ppParam+"]").prop("selected","selected")
        // populateInput("option[value="+getParameterByName("pp")+"]","pp");
    }

    let pageParam = getParameterByName("orderby");
    if (pageParam!==""){
        jQuery("option[value="+pageParam+"]").prop("selected","selected");
    }

}
function populateInput(elementId, parameterName){
    let inputElement = jQuery(elementId);
    let value = getParameterByName(parameterName);
    if (value!==""){
        inputElement.val(value)
    }
}
function buildPaginationLinks(containerName,parameterName,isLastPage){
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
    if (isLastPage===true){
        entryHtml += "<span> Next&gt </span>"
    }
    else{
        entryHtml += "<a href=" + nextLink + ">" + " Next&gt" + "</a>";
    }


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
function limitedListLinked(theList, limit, propertyName, idName, endPoint) {
    let result = "";
    let max = theList.length;
    // So use -1 as the limit if you don't want a limit
    if (limit !== -1) {
        max = Math.min(limit, theList.length);
    }
    for (let x = 0; x < max; x++) {
        let name = theList[x][propertyName];
        let id = encodeURIComponent(theList[x][idName]); // URL-encode the id
        let linkedName = "<a href=\"" + endPoint + id + "\">" + name + "</a>"; // Fixed closing tag
        if (x === 0) {
            result += linkedName + " ";
        } else if (x === max - 1 && theList.length === max) {
            result += ", and " + linkedName;
        } else if (x === max - 1 && max !== theList.length) {
            result += ", " + linkedName + "...";
        } else {
            result += ", " + linkedName;
        }
    }
    return result;
}
