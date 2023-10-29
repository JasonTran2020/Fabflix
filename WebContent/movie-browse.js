let url = window.location.href;
let index = url.indexOf("?")
let parameters=""
//Needed due to potential bug where there is no ? and we slice at -1
if (index!=-1){
    parameters = url.slice(index)
}

jQuery("#genreid").val(getParameterByName("genre"))
jQuery("#charid").val(getParameterByName("char"))

buildSortingAndPaginationForm("#sorting-form");

function onSuccess(resultData,name){
    handleMovieListResult(resultData["movies"],name)
    buildPaginationLinks("#paginator", "p", resultData["isLastPage"])

}
// Makes the HTTP GET request and registers on success callback function handleMovieListResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/search-movie" + parameters, // Setting request url + arguments, which there can be many forms. Simply going to slice of the original url's parameters and cocatentate them to the API url
    //Not a huge fan of doing that, as that requires the names from HTML to match the parameters name expected in the servlet.
    success: (resultData) =>  onSuccess(resultData, "#movie_table") // Setting callback function to handle data returned successfully by the StarsServlet
});