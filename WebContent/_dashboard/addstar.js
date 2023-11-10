$(document).ready(function() {
    $('#add_star_form').on('submit', function(e) {
        e.preventDefault(); // Prevent the default form submission

        var formData = $(this).serialize(); // Serialize the form data

        $.ajax({
            type: 'POST',
            url: 'api/addstar', // Replace with the URL to your servlet
            data: formData,
            success: function(response) {
                // Handle success
                alert('Star added successfully!');
                // Optionally, clear the form or update the UI
                $('#add_star_form').trigger('reset');
            },
            error: function(jqXHR, textStatus, errorThrown) {
                // Handle error
                console.error('Error occurred: ' + textStatus, errorThrown);
                alert('Error occurred: ' + textStatus);
            }
        });
    });
});