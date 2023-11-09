let login_form = $("#login_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        window.location.replace("metadata.html");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#login_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();
    var recaptchaResponse = grecaptcha.getResponse();
    if(recaptchaResponse.length === 0) {
        // ReCAPTCHA not solved; show error message
        $("#login_error_message").text("Please verify that you are not a robot.");

    }
    else {
        $.ajax(
            "api/login", {
                method: "POST",
                // Serialize the lo gin form to the data sent by POST request
                data: login_form.serialize(),
                success: handleLoginResult
            }
        );
    }

}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);