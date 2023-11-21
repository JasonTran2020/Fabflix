
function handleJumpBackLink (elementName,jsonObject){
    let backLinkElement = jQuery(elementName);
    let baseurl ="";
    if (jsonObject["backpage"] === "browse"){
        baseurl = "movie-browse.html"
    }
    else if(jsonObject["backpage"]==="fulltext"){
        baseurl = "movie-fulltext-search.html"
    }
    else{
        baseurl = "movie-search.html"
    }

    let entryHtml = "<a href=" + baseurl + jsonObject["parameters"]+"> Back to movies"+"</a>";
    backLinkElement.append(entryHtml);


}