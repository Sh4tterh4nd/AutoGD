package io.kellermann.model.gd;

import java.time.LocalTime;

public record GdJob(Integer serviceId, LocalTime startTime, LocalTime endTime) {
}
