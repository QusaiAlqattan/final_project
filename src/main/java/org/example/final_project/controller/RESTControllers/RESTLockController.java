package org.example.final_project.controller.RESTControllers;

import org.example.final_project.model.SystemUser;
import org.example.final_project.service.LockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lock")
public class RESTLockController {

    @Autowired
    private LockService lockService;

    @PostMapping("/acquire/{fileId}")
    public ResponseEntity<String> acquireLock(@PathVariable Long fileId, @AuthenticationPrincipal SystemUser user) {
        System.out.println("4444444444444444444444444444");
        boolean success = lockService.lockFile(fileId, user.getUsername());
        if (success) {
            return ResponseEntity.ok("Lock acquired");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("File is locked by another user");
        }
    }

    @PostMapping("/release/{fileId}")
    public ResponseEntity<String> releaseLock(@PathVariable Long fileId, @AuthenticationPrincipal SystemUser user) {
        System.out.println("555555555555555555555555555");
        boolean success = lockService.unlockFile(fileId, user.getUsername());
        if (success) {
            return ResponseEntity.ok("Lock released");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("You do not hold the lock");
        }
    }

    @GetMapping("/status/{fileId}")
    public ResponseEntity<String> getLockStatus(@PathVariable Long fileId) {
        System.out.println("666666666666666666666666666");
        String lockHolder = lockService.getLockHolder(fileId);
        if (lockHolder != null) {
            return ResponseEntity.ok(lockHolder);
        }
        return ResponseEntity.ok("File is not locked");
    }
}
