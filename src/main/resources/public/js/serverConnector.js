/**
 * Created by rattenfaenger on 06/07/16.
 */

$('document').ready(function() {
    window.setInterval(function() {
        fetchDataFromServer();
    }, 30000);
});

function fetchDataFromServer() {
    $.get("localhost:8080/events/sample", null, function(data, textStatus, jqXHR) {
        console.log('success');
        console.log(data);
    }, "json");
}