package org.example.final_project.controller.RESTControllers;

import org.example.final_project.dto.BranchDTO;
import org.example.final_project.model.Branch;
import org.example.final_project.repository.BranchRepository;
import org.example.final_project.service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/branches")
public class RESTBranchController {

    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private BranchService branchService;

    @GetMapping
    public ResponseEntity<List<BranchDTO>> getAllBranches() {
        List<BranchDTO> branches = branchService.getAllBranches();
        return ResponseEntity.ok(branches);
    }

    @PostMapping
    public ResponseEntity<BranchDTO> createBranch(@RequestBody BranchDTO branchDTO) {
        System.out.println("1111111111111111111111111111111");
        branchService.createBranch(branchDTO);
        return ResponseEntity.ok(branchDTO);
    }

    @PostMapping("/merge")
    public ResponseEntity<String> mergeBranches(@RequestBody Map<String, String> requestData) {
        Long sourceBranchId = Long.parseLong(requestData.get("sourceBranchId"));
        Long destinationBranchId = Long.parseLong(requestData.get("destinationBranchId"));
        try {
            branchService.mergeBranches(sourceBranchId, destinationBranchId);
            return ResponseEntity.ok("Branches merged successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error merging branches: " + e.getMessage());
        }
    }

}
