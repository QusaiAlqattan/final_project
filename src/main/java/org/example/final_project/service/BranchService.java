package org.example.final_project.service;

import org.example.final_project.dto.BranchDTO;
import org.example.final_project.model.Branch;
import org.example.final_project.model.File;
import org.example.final_project.model.Folder;
import org.example.final_project.repository.BranchRepository;
import org.example.final_project.repository.FileRepository;
import org.example.final_project.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BranchService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final BranchRepository branchRepository;

    @Autowired
    public BranchService(FileRepository fileRepository, FolderRepository folderRepository, BranchRepository branchRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.branchRepository = branchRepository;
    }

    // Retrieve all filtered Files
    public List<BranchDTO> getAllBranches() {
        List<Branch> branches = branchRepository.findAll();
        List<BranchDTO> filteredBranchs = new ArrayList<>();
        for (Branch branch : branches) {
            filteredBranchs.add(toDTO(branch));
        }
        return filteredBranchs;
    }

    public void createBranch(BranchDTO branchDTO) {
        Branch branch = new Branch();
        branch.setName(branchDTO.getName());

        // Save the new branch first to generate its ID
        branch = branchRepository.save(branch);  // Save the branch to persist it and get its ID

        // If the branch has a parent branch
        if (branchDTO.getParentBranchId() != null) {
            // Find the parent branch
            Optional<Branch> parentBranchOpt = branchRepository.findById(branchDTO.getParentBranchId());
            if (parentBranchOpt.isPresent()) {
                Branch parentBranch = parentBranchOpt.get();

                // Clone the parent branch's tree structure
                Set<Folder> rootFolders = parentBranch.getFolders()
                        .stream()
                        .filter(folder -> folder.getContainer() == null) // Only include folders without a container
                        .collect(Collectors.toSet());

                // Retrieve only the root-level files (those that do not have a container)
                Set<File> rootFiles = parentBranch.getFiles()
                        .stream()
                        .filter(file -> file.getContainer() == null) // Only include files without a container (i.e., root-level files)
                        .collect(Collectors.toSet());

                Set<Folder> clonedFolders = cloneFolders(rootFolders, branch); // Pass the new branch for proper linkage
                Set<File> clonedFiles = cloneFiles(rootFiles, branch);

                branch.setFolders(new ArrayList<>(clonedFolders));
                branch.setFiles(new ArrayList<>(clonedFiles));
            }
        }

        branch.setTimestamp(LocalDateTime.now());

        // Save the new branch with its own separate tree
        branchRepository.save(branch);
    }

    // Recursively clones folders and subfolders
    private Set<Folder> cloneFolders(Set<Folder> originalFolders, Branch newBranch) {
        Set<Folder> clonedFolders = new HashSet<>();

        for (Folder originalFolder : originalFolders) {
            Folder clonedFolder = new Folder();

            clonedFolder.setName(originalFolder.getName()); // Copy folder name
            clonedFolder.setBranch(newBranch); // Link the folder to the new branch

            clonedFolder.setFiles(new ArrayList<>(cloneFiles(new HashSet<>(originalFolder.getFiles()), newBranch))); // Clone files inside this folder

            folderRepository.save(clonedFolder); // save so i can reference in the sub folders and sub files

            for (File file : clonedFolder.getFiles()) {
                file.setContainer(clonedFolder);
                fileRepository.save(file);
            }

            clonedFolder.setSubFolders(new ArrayList<>(cloneFolders(new HashSet<>(originalFolder.getSubFolders()), newBranch))); // Recursively clone subfolders
            for (Folder folder : clonedFolder.getSubFolders()) {
                folder.setContainer(clonedFolder);
                folderRepository.save(folder);
            }

            clonedFolders.add(clonedFolder);

            // create folder in database
            folderRepository.save(clonedFolder);

       }


        return clonedFolders;
    }

    // Clones files within a folder or branch
    private Set<File> cloneFiles(Set<File> originalFiles, Branch newBranch) {
        Set<File> clonedFiles = new HashSet<>();

        for (File originalFile : originalFiles) {
            File clonedFile = new File();

            clonedFile.setName(originalFile.getName()); // Copy file name
            clonedFile.setContent(originalFile.getContent()); // Copy file content
            clonedFile.setBranch(newBranch); // Link the file to the new branch
            clonedFile.setVersion(originalFile.getVersion());
            clonedFile.setTimestamp(LocalDateTime.now());

            clonedFiles.add(clonedFile);

            // create file in database
            fileRepository.save(clonedFile);
        }

        return clonedFiles;
    }

    private BranchDTO toDTO(Branch branch) {
        BranchDTO branchDTO = new BranchDTO();
        branchDTO.setUniqueId(branch.getUniqueId());
        branchDTO.setName(branch.getName());
        branchDTO.setTimestamp(branch.getTimestamp());
        return branchDTO;
    }
}