
function handleJumpBackLink (elementName,jsonObject){
    let backLinkElement = jQuery(elementName);
    let baseurl ="";
    if (jsonObject["backpage"] === "browse"){
        baseurl = "movie-browse.html"
    }
    else{
        baseurl = "movie-search.html"

    }

    let entryHtml = "<a href=" + baseurl + jsonObject["parameters"]+"> Back to movies"+"</a>";
    backLinkElement.append(entryHtml);


}