
function submitSearchForm(){
    console.log("Removing empty parameters from searchform");
    let quizSearchForm = jQuery("#searchForm");
    //disable empty fields so they don't clutter up the url
    console.log("Logging form is " + quizSearchForm.attr("id"));
    // console.log("Calling find of the form got this" + quizSearchForm.find(':input[value=""]'));
    // console.log(JSON.stringify(quizSearchForm.find(':input'), null, 4));

    //quizSearchForm.find('input[vdajiwndnaiwd=""]').attr('disabled', true);
    console.log(JSON.stringify($('input[value=""]').attr('value'), null, 4));
    //$('input[value=""]').attr('disabled', true);
    //$('input:not([value])').attr('disabled', true);
    //This took WAY too long to figure out. Things I learned from this
    //1. If you don't specify the value attribute in the HTML, then it doesn't exist. It won't even have the value ""
    //2. The attribute value that you would get from the selector 'input[value=""]' or .attr('value') IS NOT LIVE, it is the value that came with the HTML
    //3. You get the live value by calling .val(). That person who said that .val() and .attr('value'). WRONG, DEAD WRONG.
    //4. Yea even the stack overflows with good ratings sometimes aren't right for your case

    //https://stackoverflow.com/questions/8312820/jquery-val-vs-attrvalue The better answer
    $('input').each( function(index){
        console.log( index + ": " + $( this ).val() );
        //By disabling the input, it does not go into the URL, preventing the clutter up
        if ($(this).val() === ""){
            $(this).attr('disabled',true);
        }
    })
    //quizSearchForm.submit();
}

function populateSearchForm(){
    populateInput("#title","title");
    populateInput("#director","director");
    populateInput("#year","year");
    populateInput("#star","star");
}

function onSuccess(resultData,name){
    handleMovieListResult(resultData["movies"],name)
    buildPaginationLinks("#paginator","p",resultData["isLastPage"])
}

let url = window.location.href;
let index = url.indexOf("?")
let parameters=""
//Needed due to potential bug where there is no ? and we slice at -1
if (index!=-1){
    parameters = url.slice(index)
}

buildSortingAndPaginationForm("#sorting-form");
populateSortingAndPaginationForm();
populateSearchForm();
// Makes the HTTP GET request and registers on success callback function handleMovieListResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/search-movie" + parameters, // Setting request url + arguments, which there can be many forms. Simply going to slice of the original url's parameters and cocatentate them to the API url
    //Not a huge fan of doing that, as that requires the names from HTML to match the parameters name expected in the servlet.
    success: (resultData) => onSuccess(resultData, "#movie_table") // Setting callback function to handle data returned successfully by the StarsServlet
});