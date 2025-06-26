package io.kellermann.services;

import io.kellermann.model.gd.FullStatus;
import io.kellermann.model.gd.Message;
import io.kellermann.model.gd.StatusKeys;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class StatusService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private boolean finished = true;


    public StatusService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }


    public void sendLogUpdate(String message) {
        simpMessagingTemplate.convertAndSend("/topic/logs",
                new Message(new SimpleDateFormat("HH:mm").format(new Date()), message));
    }

    public void sendFullDetail(StatusKeys statusKey, Double progress, String message) {
        double stepProgress = (((statusKey.maxProgress - statusKey.minProgress) * progress) + statusKey.minProgress);
        double fullProgress = (((statusKey.gdTarget.maxProgress - statusKey.gdTarget.minProgress) * stepProgress) + statusKey.gdTarget.minProgress);
        FullStatus fullStatus = new FullStatus(statusKey.title, message, statusKey.gdTarget.name(),
                (int) (progress * 100),
                (int) (stepProgress * 100),
                (int) (fullProgress * 100));

        simpMessagingTemplate.convertAndSend("/topic/progressDetails",
                fullStatus);
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
