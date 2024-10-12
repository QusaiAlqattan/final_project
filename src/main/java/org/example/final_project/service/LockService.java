//package org.example.final_project.service;
//
//import org.springframework.stereotype.Service;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Service
//public class LockService {
//
//    private final ConcurrentHashMap<Long, String> fileLocks = new ConcurrentHashMap<>(); // Map<FileId, UserId>
//
//    // Try to acquire a lock for a file by a user
//    public boolean lockFile(Long fileId, String userId) {
//        System.out.println("111111111111111111111111");
//        return fileLocks.putIfAbsent(fileId, userId) == null;  // If lock is already held, return false
//    }
//
//    // Release the lock if held by the same user
//    public boolean unlockFile(Long fileId, String userId) {
//        System.out.println("2222222222222222222222222");
//        String currentHolder = fileLocks.get(fileId);
//        if (currentHolder != null && currentHolder.equals(userId)) {
//            System.out.println("888888888888888888888888");
//            fileLocks.remove(fileId);
//            return true;
//        }
//        return false;
//    }
//
//    // Check if a file is locked
//    public String getLockHolder(Long fileId) {
//        System.out.println("33333333333333333333333333");
//        System.out.println(fileId);
//        System.out.println(fileLocks.get(fileId));
//        return fileLocks.get(fileId);
//    }
//}
