/**
 * Created by rattenfaenger on 06/07/16.
 */

var roomBox,            // container for room details
    roomName,           // displayed room name
    roomState,          // displayed room state (frei/belegt)
    currentTitle,
    currentOwner,
    currentStart,
    currentEnd,
    nextTitle,
    nextOwner,
    nextStart,
    nextEnd,
    nextFreeRoom,
    freeUntil,
    btnCleanTrue,
    btnCleanFalse,
    btnFeedbackPos,
    btnFeedbackNeutral,
    btnFeedbackNeg,
    btnTerminateCurrent;

// get all relevant gui elements and set click listeners
$('document').ready(function () {
    roomBox = $('#roomBox');
    roomName = $('#roomName');
    roomState = $('#roomState');
    currentTitle = $('#currentTitle');
    currentOwner = $('#currentowner');
    currentStart = $('#currentStart');
    currentEnd = $('#currentEnd');
    nextTitle = $('#nextTitle');
    nextOwner = $('#nextOwner');
    nextStart = $('#nextstart');
    nextEnd = $('#nextEnd');
    nextFreeRoom = $('#nextFreeRoom');
    freeUntil = $('#freeUntil');
    btnCleanTrue = $('#btnCleanTrue');
    btnCleanFalse = $('#btnCleanFalse');
    btnFeedbackPos = $('#btnFeedbackPos');
    btnFeedbackNeutral = $('#btnFeedbackNeutral');
    btnFeedbackNeg = $('#btnFeedbackNeg');
    btnTerminateCurrent = $('#btnTerminateCurrent');
});

function setRoomFree() {
    if (roomBox.hasClass("alert-danger")) {
        roomBox.removeClass("alert-danger");
        roomBox.addClass("alert-success");
    }

    $('#nextFreeRoomBox').hide();
    btnTerminateCurrent.addClass("disabled");

    roomState.html("frei");
}

function setRoomOccupied() {
    if (roomBox.hasClass("alert-success")) {
        roomBox.removeClass("alert-success");
        roomBox.addClass("alert-danger");
    }

    $('#nextFreeRoomBox').show();
    btnTerminateCurrent.removeClass("disabled");

    roomState.html("besetzt");
}

function setRoomName(name) {
    roomName.html(name);
}

function setCurrentTitle(title) {
    currentTitle.html(title);
}

function setCurrentOwner(owner) {
    currentOwner.html(owner);
}

function setCurrentStart(start) {
    currentStart.html(start);
}

function setCurrentEnd(end) {
    currentEnd.html(end);
}

function setNextTitle(title) {
    nextTitle.html(title);
}

function setNextOwner(owner) {
    nextOwner.html(owner);
}

function setNextStart(start) {
    nextStart.html(start);
}

function setNextEnd(end) {
    nextEnd.html(end);
}

function setNextFreeRoom(room) {
    nextFreeRoom.html(room);
}

function setFreeUntil(time) {
    freeUntil.html('frei bis: ' + time);
}