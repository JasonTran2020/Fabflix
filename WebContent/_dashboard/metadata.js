$(document).ready(function() {
    // Perform the AJAX request
    $.ajax({
        url: 'api/metadata',
        type: 'GET',
        dataType: 'json',
        success: function(data) {
            data.forEach(function(table) {

                var tableHtml = '<h2>' + table.tableName + '</h2>';
                tableHtml += '<table class="metadata-table">';
                tableHtml += '<thead><tr><th>Attribute</th><th>Type</th></tr></thead>';
                tableHtml += '<tbody>';


                var attributes = table.attributes;
                for (var attributeName in attributes) {
                    if (attributes.hasOwnProperty(attributeName)) {

                        tableHtml += '<tr>';
                        tableHtml += '<td>' + attributeName + '</td>';
                        tableHtml += '<td>' + attributes[attributeName] + '</td>';
                        tableHtml += '</tr>';
                    }
                }

                tableHtml += '</tbody>';
                tableHtml += '</table>';


                $('#metadata-container').append(tableHtml);
            });
        },
        error: function(jqXHR, textStatus, errorThrown) {

            console.error("Error occurred: " + textStatus, errorThrown);
        }
    });
});