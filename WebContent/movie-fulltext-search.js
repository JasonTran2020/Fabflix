


function populateSearchForm(){
    populateInput("#title","title");
}

function onSuccess(resultData,name){
    handleMovieListResult(resultData["movies"],name);
    buildPaginationLinks("#paginator","p",resultData["isLastPage"]);
}

let url = window.location.href;
let index = url.indexOf("?");
let parameters="";
//Needed due to potential bug where there is no ? and we slice at -1
if (index!==-1){
    parameters = url.slice(index);
    //Instead of storing the parameter that represents browing, search, or full-text searching in a Session, an alternative is to just add those parameters
    //In the JS file right here, since if this js file is running, that means they are fulltext searching
    //parameters += "&fulltext=true";
}

buildSortingAndPaginationForm("#sorting-form");
populateSortingAndPaginationForm();
populateSearchForm();

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/search-movie" + parameters,
    success: (resultData) => onSuccess(resultData, "#movie_table")
});