// function handleIndexServletData(resultData) {
//     // have to call other servlet now
//     // Send all item IDs in one request
//     let itemIds = resultData.previousItems.join(",");
//     jQuery.ajax({
//         dataType: "json",
//         method: "GET",
//         url: "api/cart?itemIds=" + encodeURIComponent(itemIds),
//
//         success:  (cartResult) => handleResult(cartResult)
//     });
//
// }
//
// function updateOverallTotal() {
//     let overallTotal = 0;
//     // Loop through all rows and sum up the total prices
//     $("#order_summary_body tr").each(function() {
//         let rowTotal = parseInt($(this).find(".individualTotal").text());
//         overallTotal += rowTotal;
//     });
//     $("#overallTotal").text(overallTotal);
// }
function fillRowHTML(saleNum, dataItem) {
    console.log("filling rows now");
    let orderTableBody = jQuery("#order_summary_body");
    let rowHTML = "";

    const movieId = dataItem[0].movie_id;
    const movieTitle = dataItem[0].movie_title;
    const moviePrice = parseInt(dataItem[0].movie_price); // Convert string to integer
    const movieFrequency = parseInt(dataItem[0].movie_frequency); // Convert string to integer
    const saleIdNum = saleNum.saleId;
    // Calculate total price
    const totalPrice = moviePrice * movieFrequency;

    rowHTML += "<tr>";
    rowHTML += "<th class='headline-large'>" + saleIdNum + "</th>";
    rowHTML += "<th class='headline-large'>" + movieTitle + "</th>";
    rowHTML += "<th class='headline-large'>" + movieFrequency + "</th>";
    rowHTML += "<th class='headline-large'>"  + "$" + moviePrice + "</th>";
    rowHTML += "<th class='headline-large'>"  + "$" + totalPrice + "</th>";
    rowHTML += "</tr>";



    // Append the row to the table body
    orderTableBody.append(rowHTML);


}



async function makeSequentialAjaxCalls(resultData) {
    console.log("resultData received, now time to make sequential calls");
    console.log(resultData);
    const items = resultData.previousItems;

    // make hash map -iterate through hashmap
    const movieQuantities = new Map();
    for (let i = 0; i < items.length; i++) {
        let key = items[i];
        if (movieQuantities.has(key)) {
            movieQuantities.set(key, movieQuantities.get(key) + 1);
        }
        else {
            movieQuantities.set(key, 1);
        }
    }
    let overallTotal = 0;
    for (let [movie_id, quantity] of movieQuantities.entries()) {
        console.log(movie_id);
        let dataItem = movie_id;
        try {
            let movieAttribs = await $.ajax({
                dataType: "json",
                method: "GET",
                url: "api/cart?itemIds=" + dataItem + "&quantity=" + quantity,
            });
            console.log("moveiAttribs:", movieAttribs);
            let saleNum = await $.ajax({
                dataType: "json",  // Setting return data type
                method: "POST",// Setting request method
                url: "api/purchase?id=" + dataItem + "&quantity=" + quantity, // "api/single-movie?id=" + movieId,
                // success: (saleNum) => fillRowHTML(saleNum, dataItem) // Setting callback function to handle data returned successfully by the SingleStarServlet
            });

            overallTotal = overallTotal + (quantity * parseInt(movieAttribs[0].movie_price));
            fillRowHTML(saleNum, movieAttribs);

            // Handle success
            console.log("Success for item:", dataItem);

        } catch (error) {
            // Handle error
            console.log("Error for item:", dataItem);
            console.log("Error details:", error);
        }
    }

    $("#overallTotal").text(overallTotal);
    //updateOverallTotal()
}


jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/index",
    success: (resultData) => makeSequentialAjaxCalls(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

//To create the correct back to movies link that saves all parameters from before
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/remember-movie-parameters" ,
    success: (resultData) => handleJumpBackLink("#backlink",resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});

