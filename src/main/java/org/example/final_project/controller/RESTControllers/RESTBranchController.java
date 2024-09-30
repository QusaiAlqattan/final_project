package org.example.final_project.controller.RESTControllers;

import org.example.final_project.model.Branch;
import org.example.final_project.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/branches")
public class RESTBranchController {

    @Autowired
    private BranchRepository branchRepository;

    @PostMapping
    public ResponseEntity<?> createBranch(@RequestBody Branch newBranch) {
        Long parentBranchId = newBranch.getUniqueId(); // Assuming the ID represents the parent branch

        // Create a new branch object (or reuse the passed object if it makes sense)
        Branch branchToSave = new Branch();
        branchToSave.setName(newBranch.getName());

        if (parentBranchId != null) {
            // Clone the content from the existing branch (folders and files) into the new branch
            Optional<Branch> parentBranchOpt = branchRepository.findById(parentBranchId);
            if (parentBranchOpt.isPresent()) {
                Branch parentBranch = parentBranchOpt.get();
                branchToSave.setFolders(parentBranch.getFolders());
                branchToSave.setFiles(parentBranch.getFiles());
            } else {
                return ResponseEntity.badRequest().body("Parent branch not found");
            }
        }

        // Save the new branch
        branchRepository.save(branchToSave);
        return ResponseEntity.ok("Branch created successfully");
    }
}
