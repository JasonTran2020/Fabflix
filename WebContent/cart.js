function goToPaymentPage() {
    let overallTotal = $("#overallTotal").text();
    window.location.href = "payment.html?total=" + overallTotal;
}

function updateOverallTotal() {
    let overallTotal = 0;
    // Loop through all rows and sum up the total prices
    $("#shopping_cart_body tr").each(function() {
        let rowTotal = parseInt($(this).find(".individualTotal").text());
        overallTotal += rowTotal;
    });
    $("#overallTotal").text(overallTotal);
}
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

    console.log("handleResult: populating cart info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let cart_name = jQuery("#cart_info");

    // append two html <p> created to the h3 body, which will refresh the page
    cart_name.append("<h1>" +"<strong>" + "Shopping Cart" + "</strong>" + "</h1>");

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let cartTableBody = jQuery("#shopping_cart_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    let rowHTML = "";

    resultData.forEach( movie => {
        const movieId = movie.movie_id;
        const movieTitle = movie.movie_title;
        const moviePrice = parseInt(movie.movie_price); // Convert string to integer
        const movieFrequency = parseInt(movie.movie_frequency); // Convert string to integer

        // Calculate total price
        const totalPrice = moviePrice * movieFrequency;

        rowHTML += "<tr>";
        rowHTML += "<div >";
        rowHTML += "<th class='headline-large'>" + movieTitle + "</th>";
        rowHTML += "<td class='headline-large'>";
        rowHTML += "<button class='decrement' data-movieid='" + movieId + "'>-</button>";
        rowHTML += "<span>" + movieFrequency + "</span>";  // <-- This is the span element
        rowHTML += "<button class='increment' data-movieid='" + movieId + "'>+</button>";
        rowHTML += "</td>";
        rowHTML += "<th><button type='button' class='btn btn-outline-primary delete' data-movieid='" + movieId + "'>Delete</button></th>";
        rowHTML += "<th class='headline-large'>" + "$" + moviePrice + "</th>";
        rowHTML += "<th class='headline-large individualTotal'>"  + totalPrice + "</th>";
        rowHTML += "<div/>"
        rowHTML += "</tr>";

    });

    // // Append the row created to the table body, which will refresh the page
    cartTableBody.append(rowHTML);
    updateOverallTotal();
    //cartTableBody.on("click", ".btn-outline-primary", handleAddButtonClick); // Event delegation
    // Increment Event
    cartTableBody.on('click', '.increment', function() {
        let currentQuantity = parseInt($(this).siblings('span').text());
        currentQuantity += 1;
        $(this).siblings('span').text(currentQuantity);

        // Optional AJAX request to update the server
        let movieId = $(this).data('movieid');

        jQuery.ajax({
            dataType: "json",
            method: "POST",
            url: "api/index?movieid=" + movieId + "&action=add",
            success: function(response) {
                console.log("Quantity incremented")
            }
        });
        updateOverallTotal();

    });

    // Decrement Event
    cartTableBody.on('click', '.decrement', function() {
        let currentQuantity = parseInt($(this).siblings('span').text());
        if(currentQuantity > 1) {  // Prevent quantity from going below 1
            currentQuantity -= 1;
        }
        $(this).siblings('span').text(currentQuantity);

        // Optional AJAX request to update the server
        let movieId = $(this).data('movieid');
        let moviePrice = $(this).data('price');
        jQuery.ajax({
            dataType: "json",
            method: "POST",
            url: "api/index?movieid=" + movieId + "&action=remove",
            success: function(response) {
                console.log("quantity decremented")
            }
        });
        updateOverallTotal();


    });

    // Delete Event
    cartTableBody.on('click', '.delete', function() {
        let movieId = $(this).data('movieid');
        // Remove the row from the table
        $(this).closest('tr').remove();

        jQuery.ajax({
            dataType: "json",
            method: "POST",
            url: "api/index?movieid=" + movieId + "&action=clear",
            success: function(response) {
                console.log("quantity cleared")
            }
        });
        updateOverallTotal();
    });

}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

function handleIndexServletData(resultData) {
    // have to call other servlet now
    // Send all item IDs in one request
    let itemIds = resultData.previousItems.join(",");

    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/cart?itemIds=" + encodeURIComponent(itemIds) + "&quantity=0",

        success:  (cartResult) => handleResult(cartResult)
    });

}

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/index",
    success: (resultData) => handleIndexServletData(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});