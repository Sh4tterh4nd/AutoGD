package io.kellermann.services;

import io.kellermann.model.gd.Message;
import io.kellermann.model.gd.Status;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class StatusService {
    private final SimpMessagingTemplate simpMessagingTemplate;


    public StatusService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }


    public void sendLogUpdate(String message) {
        simpMessagingTemplate.convertAndSend("/topic/logs",
                new Message(new SimpleDateFormat("HH:mm").format(new Date()), message));
    }

    public void sendDetailStatus(String message, int progress) {
        simpMessagingTemplate.convertAndSend("/topic/detailstatus",
                new Status(message, progress));
    }

    public void sendVideoStatus() {
        simpMessagingTemplate.convertAndSend("/topic/video",
                new Status("Test", 1));
    }

    public void sendMainStatus() {
        simpMessagingTemplate.convertAndSend("/topic/mainstatus",
                new Status("Test", 1));
    }

}
