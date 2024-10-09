package org.example.final_project.service;

import org.example.final_project.model.Branch;
import org.example.final_project.model.File;
import org.example.final_project.model.Folder;
import org.example.final_project.repository.BranchRepository;
import org.example.final_project.repository.FileRepository;
import org.example.final_project.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class WebSocketService extends TextWebSocketHandler {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private BranchRepository branchRepository;

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

            //TODO add creator

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
