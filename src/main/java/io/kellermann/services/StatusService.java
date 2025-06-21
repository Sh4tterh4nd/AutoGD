package io.kellermann.services;

import io.kellermann.model.gd.Status;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StatusService {
    Map<Integer, Status> statusList = new HashMap<>();

    public void submitStatus(Integer serviceID, Status status) {
        statusList.put(serviceID, status);
    }

    public Status getStatus(Integer serviceID) {
        return statusList.get(serviceID);
    }
}
