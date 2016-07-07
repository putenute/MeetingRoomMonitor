/**
 * Created by rattenfaenger on 06/07/16.
 */

$('document').ready(function() {
    fetchDataFromServer();
    window.setInterval(function() {
        fetchDataFromServer();
    }, 30000);
});

function fetchDataFromServer() {
    $.get("/events", null, function(data, textStatus, jqXHR) {
        setRoomName(data.roomName);
        if (data.currentEventName == "JETZT KEIN TERMIN") {
            setRoomFree();
        } else {
            setRoomOccupied(data.currentEventName, data.currentEventOrganizer, data.currentEventEndTime);

            setNextFreeRoom(data.nextFreeRoomName);
            setFreeUntil(data.nextFreeRoomFreeUntil);
        }

        if (data.nextEventName == "KEIN WEITERER TERMIN") {
            noFurtherEvents();
        } else {
            setNextEvent(data.nextEventName, data.nextEventOrganizer, data.nextEventStartTime);
        }
    }, "json");
}