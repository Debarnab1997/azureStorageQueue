package com.queue.batch;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.QueueMessageItem;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class QueueStorageMessageReceiver {
    private final QueueClient client;

//    @Scheduled(fixedRate = 100)
//    public void receiveMessage(){
//        QueueMessageItem queueMessageItem = client.receiveMessage();
//        if( null != queueMessageItem){
//            log.info("Received message is :{}",queueMessageItem.getBody().toString());
//            client.deleteMessage(queueMessageItem.getMessageId(),queueMessageItem.getPopReceipt());
//        }
//
//    }
}
