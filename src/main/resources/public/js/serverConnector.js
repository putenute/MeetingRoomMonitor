/**
 * Created by rattenfaenger on 06/07/16.
 */

var roomId;

$('document').ready(function() {
    roomId = getParameterByName("roomId");
    fetchDataFromServer();

    setTime(createCurrentTimeString());

    btnCleanTrue.on('click', function() { giveCleanFeedback(true); });
    btnCleanFalse.on('click', function() { giveCleanFeedback(false); });

    btnTerminateEvent.on('click', function() { terminateCurrentMeeting(); });

    window.setInterval(function() {
        fetchDataFromServer();
        setTime(createCurrentTimeString());
    }, 30000);
});

function fetchDataFromServer() {
    $.get("/events/" + roomId, null, function(data, textStatus, jqXHR) {
        setRoomName(data.roomName);
        if (data.currentEventName == "JETZT KEIN TERMIN") {
            setRoomFree();
        } else {
            setRoomOccupied(data.currentEventName, data.currentEventOrganizer, data.currentEventEndTime);
            setNextFreeRoom(data.nextFreeRoomName, data.nextFreeRoomFreeUntil);
        }

        if (data.nextEventName == "KEIN WEITERER TERMIN") {
            noFurtherEvents();
        } else {
            setNextEvent(data.nextEventName, data.nextEventOrganizer, data.nextEventStartTime);
        }

        loader.hide();
    }, "json");
}

function getParameterByName(name, url) {
    if (!url) url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function giveCleanFeedback(fb) {
    if (fb)
        $.get("/vote/" + roomId + "/clean", null, null, "json");
    else
        $.get("/vote/" + roomId + "/dirty", null, null, "json");
    alert('feedback sent');
}

function terminateCurrentMeeting() {
    $.get("/events/" + roomId + "/endevent", null, function(data, textStatus, jqXHR) {

    }, "json");
    setRoomFree();
}

function createCurrentTimeString() {
    var curTime = new Date(Date.now());
    var hrs = curTime.getHours();
    var mins = curTime.getMinutes();
    if (mins < 10)
        mins = '0' + mins;
    return hrs + ':' + mins;
}