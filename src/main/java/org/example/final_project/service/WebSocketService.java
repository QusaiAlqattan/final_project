package org.example.final_project.service;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebSocketService extends TextWebSocketHandler {

    // Store file content in memory (could be in a database or in-memory cache)
    private ConcurrentHashMap<String, String> fileContents = new ConcurrentHashMap<>();

    // Store active WebSocket connections per fileId
    private ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketSession>> fileSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String fileId = extractFileId(session); // Extract the file ID from the URL
        fileSessions.computeIfAbsent(fileId, k -> new CopyOnWriteArrayList<>()).add(session);

        // Send the current file content to the newly connected session
        if (fileContents.containsKey(fileId)) {
            session.sendMessage(new TextMessage(fileContents.get(fileId)));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String fileId = extractFileId(session); // Extract the file ID from the URL
        String payload = message.getPayload();  // The content of the file (from client)

        // Store the latest file content
        fileContents.put(fileId, payload);

        // Broadcast the changes to all other clients connected to the same file
        for (WebSocketSession s : fileSessions.getOrDefault(fileId, new CopyOnWriteArrayList<>())) {
            if (s.isOpen() && !s.getId().equals(session.getId())) {
                s.sendMessage(message);  // Send updated content to other clients
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String fileId = extractFileId(session); // Extract the file ID from the URL
        fileSessions.getOrDefault(fileId, new CopyOnWriteArrayList<>()).remove(session);
    }

    private String extractFileId(WebSocketSession session) {
        return session.getUri().getPath().split("/")[3];  // Extract fileId from the WebSocket URL
    }
}
