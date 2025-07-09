package com.queue.service;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.SendMessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
public class MessageSchedulerService {

    private final QueueClient client;
    private final TaskScheduler taskScheduler;
    private boolean isSchedulingEnabled = false;
    private String message;
    private long intervalMs = 300; // Default 300ms
    private ScheduledFuture<?> scheduledTask;

    public MessageSchedulerService(QueueClient client) {
        this.client = client;
        this.taskScheduler = createTaskScheduler();
    }

    private TaskScheduler createTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("message-scheduler-");
        scheduler.initialize();
        return scheduler;
    }

    private void sendScheduledMessage() {
        if (!isSchedulingEnabled) {
            return;
        }

        if (this.message == null || this.message.trim().isEmpty()) {
            this.isSchedulingEnabled = false;
            return;
        }

        try {
            SendMessageResult sendMessageResult = client.sendMessage(this.message);
            log.info("Scheduled Message Sent with Id: {} (interval: {}ms)", sendMessageResult.getMessageId(),
                    intervalMs);
        } catch (Exception e) {
            log.error("Error sending scheduled message: {}", e.getMessage());
        }
    }

    public void enableScheduling(String message, long intervalMs) {
        this.message = message;
        this.intervalMs = intervalMs;

        // Stop existing task if running
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
        }

        // Start new task with new interval
        this.isSchedulingEnabled = true;
        this.scheduledTask =
                taskScheduler.scheduleAtFixedRate(this::sendScheduledMessage, Duration.ofMillis(intervalMs));

        log.info("Message scheduling enabled with message: '{}' and interval: {}ms", message, intervalMs);
    }

    public void enableSchedulingWithCounter(String message, int count) {
        for (int i = 0; i < count; i++) {
            try {
                SendMessageResult sendMessageResult = client.sendMessage(message);
                log.info("Message Sent with Id: [{}] Queue : [{}] Total Message Count : [{}]",
                        sendMessageResult.getMessageId(), client.getQueueName(),
                        client.getProperties().getApproximateMessagesCount());
            } catch (Exception e) {
                log.error("Error sending scheduled message: {}", e.getMessage());
            }
        }
    }

    public void enableScheduling(String message) {
        enableScheduling(message, this.intervalMs); // Use current interval
    }

    public void disableScheduling() {
        this.isSchedulingEnabled = false;
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
        }
        log.info("Message scheduling disabled");
    }

    public void updateInterval(long intervalMs) {
        this.intervalMs = intervalMs;
        if (isSchedulingEnabled) {
            // Restart scheduling with new interval
            String currentMessage = this.message;
            disableScheduling();
            enableScheduling(currentMessage, intervalMs);
        }
        log.info("Scheduling interval updated to: {}ms", intervalMs);
    }

    public boolean isSchedulingEnabled() {
        return this.isSchedulingEnabled;
    }

    public String getCurrentMessage() {
        return this.message;
    }

    public long getCurrentInterval() {
        return this.intervalMs;
    }
}