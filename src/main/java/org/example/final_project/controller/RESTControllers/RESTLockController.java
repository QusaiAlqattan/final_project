//package org.example.final_project.controller.RESTControllers;
//
//import org.example.final_project.model.SystemUser;
//import org.example.final_project.repository.SystemUserRepository;
//import org.example.final_project.service.LockService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/lock")
//public class RESTLockController {
//
//    @Autowired
//    private LockService lockService;
//    @Autowired
//    private SystemUserRepository systemUserRepository;
//
//    @PostMapping("/acquire/{fileId}")
//    public ResponseEntity<String> acquireLock(@PathVariable Long fileId) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String username = auth.getName();
//        System.out.println("4444444444444444444444444444");
//        System.out.println(username);
//        boolean success = lockService.lockFile(fileId, username);
//        if (success) {
//            return ResponseEntity.ok("Lock acquired");
//        } else {
//            return ResponseEntity.status(HttpStatus.CONFLICT).body("File is locked by another user");
//        }
//    }
//
//    @PostMapping("/release/{fileId}")
//    public ResponseEntity<String> releaseLock(@PathVariable Long fileId) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String username = auth.getName();
//        System.out.println(username);
//        System.out.println("555555555555555555555555555");
//        boolean success = lockService.unlockFile(fileId, username);
//        if (success) {
//            return ResponseEntity.ok("Lock released");
//        } else {
//            return ResponseEntity.status(HttpStatus.CONFLICT).body("You do not hold the lock");
//        }
//    }
//
//    @GetMapping("/status/{fileId}")
//    public ResponseEntity<String> getLockStatus(@PathVariable Long fileId) {
//        System.out.println("666666666666666666666666666");
//        String lockHolder = lockService.getLockHolder(fileId);
//        System.out.println("LockHolder: " + lockHolder);
//        if (lockHolder != null) {
//            return ResponseEntity.ok(lockHolder);
//        }
//        return ResponseEntity.ok("File is not locked");
//    }
//}
