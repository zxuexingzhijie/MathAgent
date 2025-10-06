package com.mathmodel.service;

import com.mathmodel.schema.response.SystemMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket Service for sending messages to clients
 */
@Slf4j
@Service
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Send message to specific task
     */
    public void sendMessage(String taskId, SystemMessage message) {
        try {
            String destination = "/topic/task/" + taskId;
            messagingTemplate.convertAndSend(destination, message);
            log.debug("Sent message to {}: {}", destination, message.getContent());
        } catch (Exception e) {
            log.error("Failed to send WebSocket message", e);
        }
    }

    /**
     * Send message to specific user
     */
    public void sendToUser(String userId, String destination, Object payload) {
        try {
            messagingTemplate.convertAndSendToUser(userId, destination, payload);
            log.debug("Sent message to user {}: {}", userId, destination);
        } catch (Exception e) {
            log.error("Failed to send WebSocket message to user", e);
        }
    }

    /**
     * Broadcast message to all users
     */
    public void broadcast(String destination, Object payload) {
        try {
            messagingTemplate.convertAndSend(destination, payload);
            log.debug("Broadcasted message to {}", destination);
        } catch (Exception e) {
            log.error("Failed to broadcast WebSocket message", e);
        }
    }
}
