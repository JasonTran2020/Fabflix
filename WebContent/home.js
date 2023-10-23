function handleGenreTable(resultData, tableName){
    console.log("Hey, the success callback function was called");

    let testBodyElement = jQuery(tableName);
    //Empty out the element of its children. Could be useful for the SearchServlet depending on implementation
    testBodyElement.empty()
    let count = 0;
    let max = 5;
    while(count < resultData.length){
        let rowPos = 0;
        let entryHTML = "<tr>";
        while (rowPos < max && count < resultData.length){
            let jsonObject=resultData[count];
            entryHTML+="<td class='headline-medium'>" + "<a href=movie-search.html?genre=" + jsonObject["name"] + ">" + jsonObject["name"] + "</a>" + "</td>"
            rowPos+=1
            count+=1
        }

        entryHTML+="</tr>"
        testBodyElement.append(entryHTML)
    }


}

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/genres",
    success: (resultData) => handleGenreTable(resultData,"#genre_table") // Setting callback function to handle data returned successfully by the SingleStarServlet
});