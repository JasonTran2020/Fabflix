function addToCart(event, movie_id) {
    console.log("adding one movie to cart");

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: "api/index?movieid=" + movie_id + "&action=add",
        success: function() {
            alert("Movie added to cart successfully!");
        }
    });

}