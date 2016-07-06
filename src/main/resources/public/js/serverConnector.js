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
    $.get("/events/sample", null, function(data, textStatus, jqXHR) {
        setRoomName(data.roomName);
        if (data.currentEventName == "JETZT KEIN TERMIN") {
            setRoomFree();
            $('#nextFreeRoomBox').hide();
        } else {
            setRoomOccupied();
            setCurrentTitle(data.currentEventName);
            setCurrentOwner(data.currentEventOrganizer);
            setCurrentStart(data.currentEventStartTime);
            setCurrentEnd(data.currentEventEndTime);
            $('#nextFreeRoomBox').show();
        }

        if (data.nextEventName == "KEIN WEITERER TERMIN") {
            $('#nextMeeting').hide();
            $('#noMoreEvents').show();
        } else {
            $('#nextMeeting').show();
            $('#noMoreEvents').hide();

            setNextTitle(data.nextEventName);
            setNextOwner(data.nextEventOrganizer);
            setNextStart(data.nextEventStartTime);
            setNextEnd(data.nextEventEndTime);
        }
        
        setNextFreeRoom(data.nextFreeRoomName);
        setFreeUntil(data.freeRoomFreeUntil);
    }, "json");
}