let add_star_form = $("#add_star_form");

// $(document).ready(function() {
//     $('#add_star_form').on('submit', function(e) {
//         e.preventDefault(); // Prevent the default form submission
//
//         let formData = $(this).serialize(); // Serialize the form data
//
//         $.ajax({
//             type: 'POST',
//             url: 'api/addstar', // Replace with the URL to your servlet
//             data: formData,
//             success: function(response) {
//                 // Handle success
//                 alert('Star added successfully!');
//                 // Optionally, clear the form or update the UI
//                 let resultDataJson = JSON.parse(response);
//                 $("#add_star_message").text(resultDataJson["message"]);
//             },
//             error: function(jqXHR, textStatus, errorThrown) {
//                 // Handle error
//                 console.error('Error occurred: ' + textStatus, errorThrown);
//                 alert('Error occurred: ' + textStatus);
//                 $("#add_star_message").text(resultDataJson["message"]);
//             }
//         });
//     });
// });


function handleStarResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);
    console.log("handle star response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);
    // TODO - the AddStar Servlet with the messages - check if it actually inserted into db too w/ customer view
    if (resultDataJson["status"] === "success") {
        //
        console.log("show success message");
        console.log(resultDataJson["message"]);
        $("#add_star_message").text(resultDataJson["message"]);
        //
        // give a success message with the info
    } else {
        // If login fails, the web page will display

        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#add_star_message").text(resultDataJson["message"]);
    }
}
function submitStarForm(formSubmitEvent) {
    console.log("submit star form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    let formData = $(this).serialize(); // Serialize the form data

    $.ajax(
        "api/addstar", {
            method: "POST",
            // Serialize the lo gin form to the data sent by POST request
            data: formData,
            success: handleStarResult
        }
    );


}


add_star_form.submit(submitStarForm);