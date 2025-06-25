package io.kellermann.services;

import io.kellermann.model.gd.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class StatusService {
    private final SimpMessagingTemplate simpMessagingTemplate;


    public StatusService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Scheduled(fixedRate = 2000)
    public void sendMessage() {
        final String time = new SimpleDateFormat("HH:mm").format(new Date());
        simpMessagingTemplate.convertAndSend("/topic/message",
                new Message(time, "Test Test"));
    }

    public void sendStatusUpdate(String message) {
        simpMessagingTemplate.convertAndSend("/topic/message",
                new Message(message, new SimpleDateFormat("HH:mm").format(new Date())));
    }

    public void sendDetailStatus(String message) {
        simpMessagingTemplate.convertAndSend("/topic/detailstatus",
                new Message(message, new SimpleDateFormat("HH:mm").format(new Date())));
    }

    public void sendMainStatus(String message) {
        simpMessagingTemplate.convertAndSend("/topic/mainstatus",
                new Message(message, new SimpleDateFormat("HH:mm").format(new Date())));
    }

}
