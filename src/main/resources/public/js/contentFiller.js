/**
 * Created by rattenfaenger on 06/07/16.
 */

var roomBox,            // container for room details
    roomName,           // displayed room name
    roomState,          // displayed room state (frei/belegt)
    time,
    
    currentTitle,
    currentEventBox,
    currentOrgaLabel,
    currentOrganizator,
    currentStartTime,
    currentEndTime,
    
    nextEventBox,
    nextTitle,
    nextOrgaLabel,
    nextOrganizator,
    nextStartTime,
    nextEndTime,
    
    nextFreeRoomBox,
    nextFreeRoom,
    freeUntil,
    
    noMoreEventsBox,

    noFreeRoomsBox,
    
    btnCleanTrue,
    btnCleanFalse,
    btnFeedbackPos,
    btnFeedbackNeutral,
    btnFeedbackNeg,
    btnTerminateEvent,
    btnTerminateModal,

    loader;

// get all relevant gui elements and set click listeners
$('document').ready(function () {
    roomBox = $('#roomBox');
    roomName = $('#roomName');
    roomState = $('#roomState');
    time = $('#time');
    
    currentEventBox = $('#currentEventBox');
    currentTitle = $('#currentTitle');
    currentOrgaLabel = $('#currentOrgaLabel')
    currentOrganizator = $('#currentOrganizator');
    currentStartTime = $('#currentStartTime');
    currentEndTime = $('#currentEndTime');
    
    nextEventBox = $('#nextEventBox');
    nextTitle = $('#nextTitle');
    nextOrgaLabel = $('#nextOrgaLabel')
    nextOrganizator = $('#nextOrganizator');
    nextStartTime = $('#nextStartTime');
    nextEndTime = $('#nextEndTime');
    
    nextFreeRoomBox = $('#nextFreeRoomBox');
    nextFreeRoom = $('#nextFreeRoom');
    freeUntil = $('#freeUntil');
    
    noMoreEventsBox = $('#noMoreEventsBox');

    noFreeRoomsBox = $('#noFreeRoomsBox');
    noFreeRoomsBox.hide();
    
    btnCleanTrue = $('#btnCleanTrue');
    btnCleanFalse = $('#btnCleanFalse');
    //btnFeedbackPos = $('#btnFeedbackPos');
    //btnFeedbackNeutral = $('#btnFeedbackNeutral');
    //btnFeedbackNeg = $('#btnFeedbackNeg');
    btnTerminateEvent = $('#btnTerminateEvent');
    btnTerminateModal = $('#btnterminatemodal');

    btnTerminateModal.on('click', function() {
        $('#modal-container-750565').modal('show');
    })

    loader = $('#loader');
    loader.height($(window).height());
    $('#spinner').css("margin-top", (($(window).height() / 2) - 50) + "px");
});

function setRoomFree() {
    if (roomBox.hasClass("alert-danger")) {
        roomBox.removeClass("alert-danger");
        roomBox.addClass("alert-success");
    }

    currentEventBox.hide();
    nextFreeRoomBox.hide();
    btnTerminateEvent.addClass("disabled");
    btnTerminateModal.addClass("disabled");

    roomState.html("frei");
}

function setRoomOccupied(title, owner, start, end) {
    if (roomBox.hasClass("alert-success")) {
        roomBox.removeClass("alert-success");
        roomBox.addClass("alert-danger");
    }

    currentEventBox.show();
    nextFreeRoomBox.show();
    btnTerminateEvent.removeClass("disabled");
    btnTerminateModal.removeClass("disabled");

    setCurrentTitle(title);
    setCurrentOwner(owner);
    setCurrentEnd('von: ' + start + ' bis ' + end);
    roomState.html("besetzt");
}

function noFurtherEvents() {
    nextEventBox.hide();
    noMoreEventsBox.show();
}

function setNextEvent(title, owner, start, end) {
    nextEventBox.show();
    noMoreEventsBox.hide();

    setNextTitle(title);
    setNextOwner(owner);
    setNextStart('von: ' + start + ' bis ' + end);
}

function setRoomName(name) {
    roomName.html(name);
}

function setCurrentTitle(title) {
    currentTitle.html(title);
}

function setCurrentOwner(owner) {
    if (owner == "") {
        currentOrgaLabel.html("");
        currentOrganizator.html("");
    } else {
        currentOrgaLabel.html("Organisator");
        currentOrganizator.html(owner);
    }
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
    if (owner == "") {
        nextOrgaLabel.html("");
        nextOrganizator.html("");
    } else {
        nextOrgaLabel.html("Organisator");
        nextOrganizator.html(owner);
    }
}

function setNextStart(start) {
    nextStartTime.html(start);
}

function setNextEnd(end) {
    nextEndTime.html(end);
}

function setNextFreeRoom(room, time) {
    if (room == "") {
        nextFreeRoomBox.hide();
        noFreeRoomsBox.show();
    } else {
        nextFreeRoom.html(room);
        setFreeUntil(time);

        nextFreeRoomBox.show();
        noFreeRoomsBox.hide();
    }
}

function setFreeUntil(time) {
    freeUntil.html(time);
}

function setTime(timeString) {
    time.html(timeString);
}