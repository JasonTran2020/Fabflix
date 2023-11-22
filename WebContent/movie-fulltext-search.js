


function populateSearchForm(){
    populateInput("#title","title");
}

function onSuccess(resultData,name){
    handleMovieListResult(resultData["movies"],name);
    buildPaginationLinks("#paginator","p",resultData["isLastPage"]);
}
function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion

    console.log("User selected the movie " + suggestion["value"]+" with an id of " +suggestion["id"]);
    window.location.replace('single-movie.html?id='+suggestion["id"])
}
function handleLookupAjaxSuccess(data, query, doneCallback, start) {

    console.log("lookup ajax successful")

    // parse the string into JSON
    //var jsonData = JSON.parse(data);
    console.log(data)
    //What we get back is a jsonobject, one of the attribues being "movies", which is a jsonarray
    doneCallback( { suggestions: data["movies"] } );
    let end = Date.now();
    console.log("autocomplete finished at: "+ end);
    console.log(`Execution time: ${end - start} ms`);
    //Deal with saving the response as a string to be stored in sessionStorage
    sessionStorage.setItem(query,JSON.stringify(data));
}

function handleLookupCache(dataString, query, doneCallback, start) {
    // parse the string into JSON, as we stored session values as strings, not Json objects like what we get back when using ajax
    var jsonData = JSON.parse(dataString);
    console.log(jsonData)
    //What we get back is a jsonobject, one of the attribues being "movies", which is a jsonarray
    doneCallback( { suggestions: jsonData["movies"] } );
    let end = Date.now();
    console.log("autocomplete finished at: "+ end);
    console.log(`Execution time: ${end - start} ms`);
}

function handleLookup(query, doneCallback) {

    let start = Date.now();
    console.log("autocomplete initiated at: "+ start);
    if (sessionStorage.getItem(query)!==null){
        console.log("autocomplete query for \"" + query + "\" found in cache, skipping ajax call to api");
        let dataString = sessionStorage.getItem(query);
        handleLookupCache(dataString,query,doneCallback,start);
        return
    }
    console.log("sending AJAX request to backend Java Servlet for autocomplete query \"" + query + "\"");
    jQuery.ajax({
        dataType: "json",
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "api/autocomplete?phrase=" + encodeURI(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback,start)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    });
}

let url = window.location.href;
let index = url.indexOf("?");
let parameters="?fulltext=true";
//Needed due to potential bug where there is no ? and we slice at -1
if (index!==-1){
    parameters = url.slice(index) + "&fulltext=true";
    //Instead of storing the parameter that represents browing, search, or full-text searching in a Session, an alternative is to just add those parameters
    //In the JS file right here, since if this js file is running, that means they are fulltext searching
    //parameters += "&fulltext=true";
}


buildSortingAndPaginationForm("#sorting-form");
populateSortingAndPaginationForm();
populateSearchForm();



//Work to set up autocomplete on searchbox
$('#title').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
       handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    //Min characters before the lookup function is called
    minChars: 3

});

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/search-movie" + parameters,
    success: (resultData) => onSuccess(resultData, "#movie_table")
});