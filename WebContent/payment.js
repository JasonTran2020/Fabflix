// function validatePayment(ccId, callback) {
//     $.ajax({
//         type: "POST",
//         url: "/validatePayment",
//         data: JSON.stringify(ccId),
//         success: function(response) {
//             callback(null, response);
//         },
//         error: function(error) {
//             callback(error);
//         }
//     });
// }
function getParameterByName(name, url = window.location.href) {
    name = name.replace(/[\[\]]/g, '\\$&');
    let params = new URLSearchParams(new URL(url).search);
    return params.get(name);
}

let total = getParameterByName('total');
console.log(total);
document.getElementById('totalPriceDisplay').textContent = "Total: $" + total;

$(document).ready(function() {
    $("#payment_form").submit(function(event) {
        // Prevent the form from submitting
        event.preventDefault();

        // Retrieve credit card number entered by user
        let ccId = $("input[name='cardNumber']").val();
        let firstName = $("input[name='firstName']").val();
        let lastName = $("input[name='lastName']").val();
        let expiryDate = $("input[name='expiryDate']").val();

        let paymentData = {
            "ccId": ccId,
            "firstName": firstName,
            "lastName": lastName,
            "expiryDate": expiryDate
        };

        console.log(paymentData);
        $.ajax({
            type: "POST",
            url: "api/payment",
            data: JSON.stringify(paymentData),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function() {
                window.location.href = "confirmation.html";
            },
            error: function() {
                alert("Invalid payment info. Please check and re-enter.");
            }
        });

    });
});



