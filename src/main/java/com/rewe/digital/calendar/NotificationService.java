package com.rewe.digital.calendar;

import org.springframework.stereotype.Component;

@Component
public class NotificationService {


    public void notifyOrganizerClean(final Meeting lastEventInRoom) {
        System.out.println("Hier würde jetzt eine saubere Mail geschickt werden!");
    }

    public void notifyOrganizerDirty(final Meeting lastEventInRoom) {
        System.out.println("Hier würde jetzt eine dreckige Mail geschickt werden!");
    }
}
