let add_movie_form = $("#add_movie_form");

function handleMovieResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);
    console.log("handle movie response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
        //
        console.log("show success message");
        console.log(resultDataJson["message"]);
        $("#add_movie_message").text(resultDataJson["message"]);
        //
        // give a success message with the info
    } else {
        // If login fails, the web page will display
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#add_movie_message").text(resultDataJson["message"]);
    }
}
function submitMovieForm(formSubmitEvent) {
    console.log("submit movie form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    let formData = $(this).serialize(); // Serialize the form data

    $.ajax(
        "api/addmovie", {
            method: "POST",
            // Serialize the lo gin form to the data sent by POST request
            data: formData,
            success: handleMovieResult
        }
    );


}


add_movie_form.submit(submitMovieForm);