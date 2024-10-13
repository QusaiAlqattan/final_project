package org.example.final_project.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.final_project.model.Branch;
import org.example.final_project.model.File;
import org.example.final_project.model.Folder;
import org.example.final_project.model.SystemUser;
import org.example.final_project.repository.BranchRepository;
import org.example.final_project.repository.FileRepository;
import org.example.final_project.repository.FolderRepository;
import org.example.final_project.repository.SystemUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class WebSocketService extends TextWebSocketHandler {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private SystemUserRepository systemUserRepository;

    private final ReentrantLock  lock = new ReentrantLock();

    private int offset = 0;

    private int prevPosition = 0;

    // Store file content in memory (could be in a database or in-memory cache)
    private ConcurrentHashMap<String, String> fileContents = new ConcurrentHashMap<>();

    // Store active WebSocket connections per fileId
    private ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketSession>> fileSessions = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, String> fileLocks = new ConcurrentHashMap<>();

    // Acquire lock for a file
//    public boolean acquireLock(String fileId) {
//        System.out.println("acquireLock");
//        try{
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            String username = auth.getName();
//            System.out.println("fileLocks" + fileLocks);
//            if (fileLocks.containsKey(fileId) && !fileLocks.get(fileId).equals(username)) {
//                System.out.println("222222222222222222");
//                return false; // The file is already locked
//            }
//            System.out.println("username" + username);
//            fileLocks.put(fileId, username);
//            broadcastLockMessage(fileId, username);
//            return true; // Lock acquired
//        }catch (Exception e){
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    // Release lock for a file
//    public void releaseLock(String fileId) {
//        System.out.println("releaseLock");
//        try{
//            String username = fileLocks.remove(fileId);
//            broadcastUnlockMessage(fileId, username);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }

//    private void broadcastLockMessage(String fileId, String username) throws IOException {
//    System.out.println("broadcastLockMessage");
//        TextMessage lockMessage = new TextMessage("{\"type\":\"LOCK\", \"user\":\"" + username + "\"}");
//        System.out.println("fileSessions" + fileSessions);
//        for (WebSocketSession session : fileSessions.getOrDefault(fileId, new CopyOnWriteArrayList<>())) {
//            if (session.isOpen()) {
//                session.sendMessage(lockMessage);
//            }
//        }
//    }
//
//    private void broadcastUnlockMessage(String fileId, String username) throws IOException {
//        System.out.println("broadcastUnlockMessage");
//        TextMessage unlockMessage = new TextMessage("{\"type\":\"UNLOCK\"}");
//        for (WebSocketSession session : fileSessions.getOrDefault(fileId, new CopyOnWriteArrayList<>())) {
//            if (session.isOpen()) {
//                session.sendMessage(unlockMessage);
//            }
//        }
//    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("afterConnectionEstablished");
        String fileId = extractFileId(session); // Extract the file ID from the URL
        fileSessions.computeIfAbsent(fileId, k -> new CopyOnWriteArrayList<>()).add(session);
        System.out.println("fileSessions" + fileSessions);
        // Send the current file content to the newly connected session
        if (fileContents.containsKey(fileId)) {
            session.sendMessage(new TextMessage("{\"content\":\""+ fileContents.get(fileId) +"\"}"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        lock.lock();
        try {
            System.out.println("handleTextMessage");

            String fileId = extractFileId(session); // Extract the file ID from the URL

            // Store the latest file content
            if (fileContents.containsKey(fileId)) {
                fileContents.put(fileId, updateContent(message, fileContents.get(fileId)));
            } else {
                fileContents.put(fileId, updateContent(message, ""));
            }

            TextMessage newMessage = new TextMessage("{\"content\":\"" + fileContents.get(fileId) + "\"}");

            // Broadcast the changes to all other clients connected to the same file
            for (WebSocketSession s : fileSessions.getOrDefault(fileId, new CopyOnWriteArrayList<>())) {
                if (s.isOpen()) {
                    s.sendMessage(newMessage);  // Send updated content to other clients
                }
            }

            // Check if there are any waiting threads
            System.out.println("before: " + fileContents.get(fileId));
            if (!lock.hasQueuedThreads()) {
                System.out.println("lock has queued");
                System.out.println("offset: " + offset);
                offset = 0;
            }
            System.out.println("after: " + fileContents.get(fileId));
        } finally {
            lock.unlock();  // Always unlock, even if an exception occurs
        }
    }

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
                        String output = removeSubstring(originalContent, actualPosition, tempOffset);
                        offset -= tempOffset;
                        prevPosition = actualPosition;
                        return output;
                    } else {
                        // dont care about the offset
                        String output = removeSubstring(originalContent, position, tempOffset);
                        offset -= tempOffset;
                        prevPosition = position;
                        return output;
                    }
                }else {
                    // dont care about the offset
                    String output = removeSubstring(originalContent, position, tempOffset);
                    offset -= tempOffset;
                    prevPosition = position;
                    return output;
                }
            }

        } catch (Exception e) {
            System.out.println("offset: " + offset);
            System.out.println("actual position: " + actualPosition);
            System.out.println("content: " + originalContent);
            System.out.println("position: " + position);
            System.out.println("value: " + value);
            e.printStackTrace();
        }
        return "oops!!!";
    }

    private String removeSubstring(String original, int startIndex, int length) {
        // Check for valid index and length
        if (startIndex < 0) {
            startIndex -= offset;
        }

        // Concatenate the part before and after the substring to be removed
        return original.substring(0, startIndex) + original.substring(startIndex + length);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("afterConnectionClosed");
        String fileId = extractFileId(session); // Extract the file ID from the URL
        CopyOnWriteArrayList<WebSocketSession> sessions = fileSessions.get(fileId);

        if (sessions != null) {
            sessions.remove(session);

            // If no more sessions are left, clear unsaved changes
            if (sessions.isEmpty()) {
                System.out.println("remove");
                fileSessions.remove(fileId); // Optionally remove the entry for the fileId
                fileContents.remove(fileId);  // Remove unsaved changes for the file
            }
        }
    }

    public String createNewFile(String fileId, String content) {
        try {
            File file = fileRepository.getById(Long.parseLong(fileId));
            String oldVersion = file.getVersion();
            String newVersion = String.valueOf(Long.parseLong(oldVersion) + 1);

            File newFile = new File();
            newFile.setVersion(newVersion);
            newFile.setContent(content);
            newFile.setBranch(file.getBranch());
            newFile.setTimestamp(LocalDateTime.now());
            newFile.setName(file.getName());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            SystemUser user = systemUserRepository.findByUsername(auth.getName());
            newFile.setCreator(user);

            fileRepository.save(newFile);

            if (file.getContent() != null) {
                Folder container = file.getContainer();
                newFile.setContainer(file.getContainer());

                // add to the sub folders in the container
                List<File> subFiles = container.getFiles();
                subFiles.add(file);
                container.setFiles(subFiles);
                folderRepository.save(container);
            }

            // update branch
            Branch branch = file.getBranch();
            List<File> oldBranchFiles = branch.getFiles();
            oldBranchFiles.add(file);
            branch.setFiles(oldBranchFiles);
            branchRepository.save(branch);

            return "File saved successfully!";
        } catch (Exception e) {
            return "Error saving file";
        }
    }

    private String extractFileId(WebSocketSession session) {
        return session.getUri().getPath().split("/")[3];  // Extract fileId from the WebSocket URL
    }
}
