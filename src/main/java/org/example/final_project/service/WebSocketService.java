package org.example.final_project.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.final_project.dto.FileDTO;
import org.example.final_project.model.File;
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

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private SystemUserRepository systemUserRepository;

    @Autowired
    private FileService fileService;

    private final ReentrantLock  lock = new ReentrantLock();

    private int offset = 0;

    private int prevPosition = 0;

    // Store file content in memory (could be in a database or in-memory cache)
    private ConcurrentHashMap<String, String> fileContents = new ConcurrentHashMap<>();

    // Store active WebSocket connections per fileId
    private ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketSession>> fileSessions = new ConcurrentHashMap<>();

    //  !   ///////////////////////////////////////////////////////////////
    //  !   built in methods
    //  !   ///////////////////////////////////////////////////////////////
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
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

        String fileId = extractFileId(session);
        fileSessions.computeIfAbsent(fileId, k -> new CopyOnWriteArrayList<>()).add(session);
        String content = fileService.getFileContent(Long.parseLong(fileId));

        // Send the current file content to the newly connected session
        if (!fileContents.containsKey(fileId)) {
            if (content != null && content.length() > 0) {
                fileContents.put(fileId, content);
                session.sendMessage(new TextMessage(fileContents.get(fileId)));
            }
        }else {
            session.sendMessage(new TextMessage(fileContents.get(fileId)));
        }

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

            TextMessage newMessage = new TextMessage(fileContents.get(fileId));

            // Broadcast the changes to all other clients connected to the same file
            for (WebSocketSession s : fileSessions.getOrDefault(fileId, new CopyOnWriteArrayList<>())) {
                if (s.isOpen()) {
                    s.sendMessage(newMessage);
                }
            }

            // Check if there are any waiting threads
            if (!lock.hasQueuedThreads()) {
                offset = 0;
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
    private String updateContent(TextMessage message, String originalContent){
        int position = 0;
        int actualPosition = 0;
        String value = "default";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Parse the JSON string into a JsonNode
            HashMap<String, Object> map = objectMapper.readValue(message.getPayload(), new TypeReference<HashMap<String, Object>>() {});
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
