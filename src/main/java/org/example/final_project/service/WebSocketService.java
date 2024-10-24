package org.example.final_project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.final_project.dto.FileDTO;
import org.example.final_project.model.File;
import org.example.final_project.model.Message;
import org.example.final_project.repository.BranchRepository;
import org.example.final_project.repository.FileRepository;
import org.example.final_project.repository.FolderRepository;
import org.example.final_project.repository.SystemUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import static org.example.final_project.service.util.WebSocketServiceUtil.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import static org.example.final_project.service.util.WebSocketServiceUtil.extractFileId;

import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Service
public class WebSocketService extends TextWebSocketHandler implements HandshakeInterceptor {

    private final FileRepository fileRepository;

    private final FileService fileService;

    private final ReentrantLock  lock = new ReentrantLock();

    private int offset = 0;

    private boolean isMultithread = false;

    private int prevPosition = 0;

    // Store file content in memory (could be in a database or in-memory cache)
    private ConcurrentHashMap<String, String> fileContents = new ConcurrentHashMap<>();

    // Store active WebSocket connections per fileId
    private ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketSession>> fileSessions = new ConcurrentHashMap<>();

    private final ObjectMapper OBJECT_MAPPER  = new ObjectMapper();

    @Autowired
    public WebSocketService (FileRepository fileRepository, FileService fileService){
        this.fileRepository = fileRepository;
        this.fileService = fileService;
    }

    //  !   ///////////////////////////////////////////////////////////////
    //  !   built in methods
    //  !   ///////////////////////////////////////////////////////////////
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        attributes.put("auth", authentication);

        return true;
    }


    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        lock.lock();

        // add session
        String fileId = extractFileId(session);
        fileSessions.computeIfAbsent(fileId, k -> new CopyOnWriteArrayList<>()).add(session);

        // Send the current file content to the newly connected session
        sendInitContent(session, fileId);

        lock.unlock();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        lock.lock();
        try {
            String fileId = extractFileId(session); // Extract the file ID from the URL

            // Store the latest file content
            if (fileContents.containsKey(fileId)) {
                fileContents.put(fileId, updateContent(message, fileContents.get(fileId)));
            } else {
                fileContents.put(fileId, updateContent(message, ""));
            }

            // Check if there are any waiting threads
            if (!lock.hasQueuedThreads()) {
                offset = 0;
                broadcast(session, message, fileId);
            }else{
                isMultithread = true;
            }
        } finally {
            lock.unlock();  // Always unlock, even if an exception occurs
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        lock.lock();
        String fileId = extractFileId(session);
        CopyOnWriteArrayList<WebSocketSession> sessions = fileSessions.get(fileId);

        if (sessions != null) {
            sessions.remove(session);

            // If no more sessions are left, clear unsaved changes
            if (sessions.isEmpty()) {
                fileSessions.remove(fileId); // Remove the session for the fileId
                fileContents.remove(fileId);  // Remove unsaved changes for the file
            }
        }
        lock.unlock();
    }
    //  !   ///////////////////////////////////////////////////////////////



    //  !   ///////////////////////////////////////////////////////////////
    //  !   custom methods
    //  !   ///////////////////////////////////////////////////////////////
    private void sendInitContent(WebSocketSession session, String fileId) throws IOException {
        String content = fileContents.get(fileId);

        if (content == null) {
            // get data from DB
            content = fileService.getFileContent(Long.parseLong(fileId));
            if (content == null || content.isEmpty()) {
                // Early return if file is empty
                return;
            }
            fileContents.put(fileId, content);
        }

        Message newMessageObj = new Message(content, "", 0, 0, true);
        String newMessageJson = OBJECT_MAPPER.writeValueAsString(newMessageObj);
        TextMessage newMessage = new TextMessage(newMessageJson);
        session.sendMessage(newMessage);
    }

    private void broadcast(WebSocketSession session, TextMessage message, String fileId) throws IOException {
        if (isMultithread) {
            // broadcast all content
            Message newMessageObj = new Message(fileContents.get(fileId), "", 0, 0, isMultithread);
            String newMessageJson = OBJECT_MAPPER .writeValueAsString(newMessageObj);
            TextMessage newMessage = new TextMessage(newMessageJson);

            for (WebSocketSession s : fileSessions.getOrDefault(fileId, new CopyOnWriteArrayList<>())) {
                if (s.isOpen()) {
                    s.sendMessage(newMessage);
                }
            }
        }else {
            // Broadcast only the changes to all other clients connected to the same file
            // Parse the JSON string into a JsonNode
            HashMap<String, Object> map = OBJECT_MAPPER .readValue(message.getPayload(), new TypeReference<HashMap<String, Object>>() {});
            String value = (String) map.get("content");
            String type = (String) map.get("type");
            int length = (Integer) map.get("offset");

            Message newMessageObj = new Message(value, type, length, prevPosition, isMultithread);
            String newMessageJson = OBJECT_MAPPER .writeValueAsString(newMessageObj);

            for (WebSocketSession s : fileSessions.getOrDefault(fileId, new CopyOnWriteArrayList<>())) {
                if (s.isOpen()) {
                    if ( !s.getId().equals(session.getId())) {
                        TextMessage newMessage = new TextMessage(newMessageJson);
                        s.sendMessage(newMessage);
                    }
                }
            }
        }

        // reset flag value
        isMultithread = false;
    }

    private String updateContent(TextMessage message, String originalContent){
        int position = 0;
        int actualPosition = 0;
        String value = "default";
        try {
            // Parse the JSON string into a JsonNode
            HashMap<String, Object> map = OBJECT_MAPPER .readValue(message.getPayload(), new TypeReference<HashMap<String, Object>>() {});
            int tempOffset = (Integer) map.get("offset");
            value = (String) map.get("content");
            String type = (String) map.get("type");
            position = (Integer) map.get("position");
            actualPosition = position + offset;

            StringBuilder stringBuilder = new StringBuilder(originalContent);

            if (type.equals("Insert")) {
                if (offset != 0){
                    // someone edited the file before me
                    if (actualPosition >= prevPosition){
                        // i do care about the offset
                        stringBuilder.insert(actualPosition, value);
                        prevPosition = actualPosition;
                    }else{
                        // dont care about the offset
                        stringBuilder.insert(position, value);
                        prevPosition = position;
                    }
                }else{
                    // offset is 0 (someone cut & replace same size)
                    stringBuilder.insert(position, value);
                    prevPosition = position;
                }
                offset += tempOffset;
                return stringBuilder.toString();
            }else{
//                if(tempOffset == 0){
//                    // select all + delete
//                    offset = 0;
//                    prevPosition = 0;
//                    return "";
//                }else{
                    if (offset != 0) {
                        if (actualPosition >= prevPosition) {
                            // i do care about the offset
                            String output = removeSubstring(originalContent, actualPosition, tempOffset, offset);
                            offset -= tempOffset;
                            prevPosition = actualPosition;
                            return output;
                        } else {
                            // dont care about the offset
                            String output = removeSubstring(originalContent, position, tempOffset, offset);
                            offset -= tempOffset;
                            prevPosition = position;
                            return output;
                        }
                    }else {
                        // dont care about the offset
                        String output = removeSubstring(originalContent, position, tempOffset, offset);
                        offset -= tempOffset;
                        prevPosition = position;
                        return output;
                    }
                }
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "oops!!!, something when wrong";
    }

    @Transactional
    public String createNewFile(String fileId, String content) {
        try {
            File file = fileRepository.getById(Long.parseLong(fileId));
            FileDTO fileDTO = new FileDTO();
            fileDTO.setName(file.getName());
            fileDTO.setContent(content);
            fileDTO.setCreatorId(file.getCreator().getUniqueId());
            fileDTO.setVersion(file.getVersion());
            fileDTO.setBranchId(file.getBranch().getUniqueId());
            if (file.getContainer() != null){
                fileDTO.setContainerId(file.getContainer().getUniqueId());
            }
            fileDTO.setTimestamp(file.getTimestamp());

            fileService.createFile(fileDTO, file.getBranch().getUniqueId(), content) ;

            return "File saved successfully!";
        } catch (Exception e) {
            return "Error saving file" + e.getMessage();
        }
    }
    //  !   ///////////////////////////////////////////////////////////////
}
