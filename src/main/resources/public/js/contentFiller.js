/**
 * Created by rattenfaenger on 06/07/16.
 */

var roomBox,            // container for room details
    roomName,           // displayed room name
    roomState,          // displayed room state (frei/belegt)
    time,
    
    currentTitle,
    currentEventBox,
    currentOrganizator,
    currentStartTime,
    currentEndTime,
    
    nextEventBox,
    nextTitle,
    nextOrganizator,
    nextStartTime,
    nextEndTime,
    
    nextFreeRoomBox,
    nextFreeRoom,
    freeUntil,
    
    noMoreEventsBox,
    
    btnCleanTrue,
    btnCleanFalse,
    btnFeedbackPos,
    btnFeedbackNeutral,
    btnFeedbackNeg,
    btnTerminateEvent;

// get all relevant gui elements and set click listeners
$('document').ready(function () {
    roomBox = $('#roomBox');
    roomName = $('#roomName');
    roomState = $('#roomState');
    time = $('#time');
    
    currentEventBox = $('#currentEventBox');
    currentTitle = $('#currentTitle');
    currentOrganizator = $('#currentOrganizator');
    currentStartTime = $('#currentStartTime');
    currentEndTime = $('#currentEndTime');
    
    nextEventBox = $('#nextEventBox');
    nextTitle = $('#nextTitle');
    nextOrganizator = $('#nextOrganizator');
    nextStartTime = $('#nextStartTime');
    nextEndTime = $('#nextEndTime');
    
    nextFreeRoomBox = $('#nextFreeRoomBox');
    nextFreeRoom = $('#nextFreeRoom');
    freeUntil = $('#freeUntil');
    
    noMoreEventsBox = $('#noMoreEventsBox');
    
    btnCleanTrue = $('#btnCleanTrue');
    btnCleanFalse = $('#btnCleanFalse');
    //btnFeedbackPos = $('#btnFeedbackPos');
    //btnFeedbackNeutral = $('#btnFeedbackNeutral');
    //btnFeedbackNeg = $('#btnFeedbackNeg');
    btnTerminateEvent = $('#btnTerminateEvent');
});

function setRoomFree() {
    if (roomBox.hasClass("alert-danger")) {
        roomBox.removeClass("alert-danger");
        roomBox.addClass("alert-success");
    }

    currentEventBox.hide();
    nextFreeRoomBox.hide();
    btnTerminateEvent.addClass("disabled");

    roomState.html("frei");
}

function setRoomOccupied(title, owner, end) {
    if (roomBox.hasClass("alert-success")) {
        roomBox.removeClass("alert-success");
        roomBox.addClass("alert-danger");
    }

    currentEventBox.show();
    nextFreeRoomBox.show();
    btnTerminateEvent.removeClass("disabled");

    setCurrentTitle(title);
    setCurrentOwner(owner);
    setCurrentEnd(end);
    roomState.html("besetzt");
}

function noFurtherEvents() {
    nextEventBox.hide();
    noMoreEventsBox.show();
}

function setNextEvent(title, owner, start) {
    nextEventBox.show();
    noMoreEventsBox.hide();

    setNextTitle(title);
    setNextOwner(owner);
    setNextStart(start);
}

function setRoomName(name) {
    roomName.html(name);
}

function setCurrentTitle(title) {
    currentTitle.html(title);
}

function setCurrentOwner(owner) {
    currentOrganizator.html(owner);
}

function setCurrentStart(start) {
    currentStartTime.html(start);
}

function setCurrentEnd(end) {
    currentEndTime.html(end);
}

function setNextTitle(title) {
    nextTitle.html(title);
}

function setNextOwner(owner) {
    nextOrganizator.html(owner);
}

function setNextStart(start) {
    nextStartTime.html(start);
}

function setNextEnd(end) {
    nextEndTime.html(end);
}

function setNextFreeRoom(room) {
    nextFreeRoom.html(room);
}

function setFreeUntil(time) {
    freeUntil.html('frei bis: ' + time);
}