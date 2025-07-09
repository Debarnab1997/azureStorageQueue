package com.queue.api;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.SendMessageResult;
import com.queue.service.MessageSchedulerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class MessageApi {

    private final QueueClient client;
    private final MessageSchedulerService schedulerService;

    @GetMapping ("/sendMessage/{message}")
    public String sendMessage(
            @PathVariable ("message")
            String message) {
        if (message != null && !message.trim().isEmpty()) {
            try {
                SendMessageResult sendMessageResult = client.sendMessage(message);
                log.info("Manual Message Sent with Id: {}", sendMessageResult.getMessageId());
                return message + " sent to the queue successfully";
            } catch (Exception e) {
                log.error("Error sending manual message: {}", e.getMessage());
                return "Error sending message: " + e.getMessage();
            }
        }
        return "message is empty or null pls send valid message";
    }

    @PostMapping("/startSchedulingWithParam")
    public String startSchedulingWithParam(
            @RequestBody String message,
            @RequestParam("intervalMs") long intervalMs) {
        schedulerService.enableScheduling(message, intervalMs);
        return "Message scheduling started with interval: " + intervalMs + "ms";
    }

    @PostMapping("/startSchedulingWithCounter")
    public String startSchedulingWithCounter(
            @RequestBody String message,
            @RequestParam("counter") int counter) {
        schedulerService.enableSchedulingWithCounter(message, counter);
        return "Message scheduling started for count: " + counter;
    }

    @GetMapping ("/stopScheduling")
    public String stopScheduling() {
        schedulerService.disableScheduling();
        return "Message scheduling stopped";
    }

    @PostMapping ("/updateInterval/{intervalMs}")
    public String updateInterval(
            @PathVariable ("intervalMs")
            long intervalMs) {
        schedulerService.updateInterval(intervalMs);
        return "Scheduling interval updated to: " + intervalMs + "ms";
    }

    @GetMapping ("/schedulingStatus")
    public String getSchedulingStatus() {
        return String.format("Scheduling enabled: %s, Message: '%s', Interval: %dms",
                schedulerService.isSchedulingEnabled(), schedulerService.getCurrentMessage(),
                schedulerService.getCurrentInterval());
    }

    // DTO for request body
    public static class SchedulingRequest {
        private String message;
        private Long intervalMs;

        // Getters and setters
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Long getIntervalMs() {
            return intervalMs;
        }

        public void setIntervalMs(Long intervalMs) {
            this.intervalMs = intervalMs;
        }
    }
}