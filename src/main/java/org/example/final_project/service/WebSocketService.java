package org.example.final_project.service;

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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
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

    private final Lock lock = new ReentrantLock();

    // Store file content in memory (could be in a database or in-memory cache)
    private ConcurrentHashMap<String, String> fileContents = new ConcurrentHashMap<>();

    // Store active WebSocket connections per fileId
    private ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketSession>> fileSessions = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, String> fileLocks = new ConcurrentHashMap<>();

    // Acquire lock for a file
    public boolean acquireLock(String fileId) {
        System.out.println("acquireLock");
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            System.out.println("fileLocks" + fileLocks);
            if (fileLocks.containsKey(fileId) && !fileLocks.get(fileId).equals(username)) {
                System.out.println("222222222222222222");
                return false; // The file is already locked
            }
            System.out.println("username" + username);
            fileLocks.put(fileId, username);
            broadcastLockMessage(fileId, username);
            return true; // Lock acquired
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    // Release lock for a file
    public void releaseLock(String fileId) {
        System.out.println("releaseLock");
        try{
            String username = fileLocks.remove(fileId);
            broadcastUnlockMessage(fileId, username);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void broadcastLockMessage(String fileId, String username) throws IOException {
    System.out.println("broadcastLockMessage");
        TextMessage lockMessage = new TextMessage("{\"type\":\"LOCK\", \"user\":\"" + username + "\"}");
        System.out.println("fileSessions" + fileSessions);
        for (WebSocketSession session : fileSessions.getOrDefault(fileId, new CopyOnWriteArrayList<>())) {
            if (session.isOpen()) {
                session.sendMessage(lockMessage);
            }
        }
    }

    private void broadcastUnlockMessage(String fileId, String username) throws IOException {
        System.out.println("broadcastUnlockMessage");
        TextMessage unlockMessage = new TextMessage("{\"type\":\"UNLOCK\"}");
        for (WebSocketSession session : fileSessions.getOrDefault(fileId, new CopyOnWriteArrayList<>())) {
            if (session.isOpen()) {
                session.sendMessage(unlockMessage);
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("afterConnectionEstablished");
        String fileId = extractFileId(session); // Extract the file ID from the URL
        fileSessions.computeIfAbsent(fileId, k -> new CopyOnWriteArrayList<>()).add(session);
        System.out.println("fileSessions" + fileSessions);
        // Send the current file content to the newly connected session
        if (fileContents.containsKey(fileId)) {
            session.sendMessage(new TextMessage(fileContents.get(fileId)));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        lock.lock();
        System.out.println("handleTextMessage");
        String fileId = extractFileId(session); // Extract the file ID from the URL
        String payload = message.getPayload();  // The content of the file (from client)

        System.out.println("fileSessions" + fileSessions);

        // Store the latest file content
        fileContents.put(fileId, payload);

        // Broadcast the changes to all other clients connected to the same file
        for (WebSocketSession s : fileSessions.getOrDefault(fileId, new CopyOnWriteArrayList<>())) {
            if (s.isOpen()) {
                s.sendMessage(message);  // Send updated content to other clients
            }
        }
        lock.unlock();
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
