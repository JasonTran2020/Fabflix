/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#star_info");

    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<h1>" +"<strong>" + resultData[0]["star_name"] + "</strong>" + "</h1>" +
        "<p>Date Of Birth: " + resultData[0]["star_dob"] + "</p>");

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    let rowHTML = "";

    for (i = 0; i < resultData[0]["movies"].length; i++)
    {
        rowHTML += "<tr>";
        rowHTML += "<div >";
        //REMEMEMBER, THE URL LITERALLY HAS TO BE IN DOUBLE QUOTES. Hence, we use single quotes to allow double quotes in the string itself
        // the url has to ("single-movie.html?id=wlo"), not just (single-movie.html?id=wlo)
        //There has to be a better way than applying headline-large to every single tow
        rowHTML += "<th class='headline-large'>" + '<a href="single-movie.html?id=' + resultData[0]["movies"][i]["movie_id"] + '">' + resultData[0]["movies"][i]["movie_title"] + "</a>" + "</th>";
        rowHTML += "<th class='headline-large'>" + resultData[0]["movies"][i]["movie_year"] + "</th>";
        rowHTML += "<th class='headline-large'>" + resultData[0]["movies"][i]["movie_director"] + "</th>";
        rowHTML += "<th><button type='button' class='btn btn-outline-primary' id='" + resultData[i]["movie_id"] + "'>Add</button></th>";
        rowHTML += "<div/>"
        rowHTML += "</tr>";

    }
    // Append the row created to the table body, which will refresh the page
    movieTableBodyElement.append(rowHTML);
    movieTableBodyElement.on("click", ".btn-outline-primary", handleAddButtonClick); // Event delegation

}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});