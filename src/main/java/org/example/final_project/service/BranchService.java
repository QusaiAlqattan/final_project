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

    //  !   ///////////////////////////////////////////////////////////////
    //  !   fetch branch
    //  !   ///////////////////////////////////////////////////////////////

    // Retrieve all filtered Files
    public List<BranchDTO> getAllBranches() {
        List<Branch> branches = branchRepository.findAll();
        List<BranchDTO> filteredBranchs = new ArrayList<>();
        for (Branch branch : branches) {
            filteredBranchs.add(toDTO(branch));
        }
        return filteredBranchs;
    }

    // convert for model to DTO
    private BranchDTO toDTO(Branch branch) {
        BranchDTO branchDTO = new BranchDTO();
        branchDTO.setUniqueId(branch.getUniqueId());
        branchDTO.setName(branch.getName());
        branchDTO.setTimestamp(branch.getTimestamp());
        return branchDTO;
    }

    //  !   ///////////////////////////////////////////////////////////////



    //  !   ///////////////////////////////////////////////////////////////
    //  !   create branch
    //  !   ///////////////////////////////////////////////////////////////

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

//            Folder clonedFolder = cloneFolderMerge(originalFolder, newBranch);
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
//            clonedFiles.add(cloneFileMerge(originalFile, newBranch));

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

    //  !   ///////////////////////////////////////////////////////////////



    //  !   ///////////////////////////////////////////////////////////////
    //  !   merge branches
    //  !   ///////////////////////////////////////////////////////////////
    public void mergeBranches(Long sourceBranchId, Long destinationBranchId) throws Exception {
        Optional<Branch> sourceBranchOpt = branchRepository.findById(sourceBranchId);
        Optional<Branch> destinationBranchOpt = branchRepository.findById(destinationBranchId);

        if (sourceBranchOpt.isPresent() && destinationBranchOpt.isPresent()) {
            Branch sourceBranch = sourceBranchOpt.get();
            Branch destinationBranch = destinationBranchOpt.get();

            mergeBranchContent(sourceBranch, destinationBranch);
            branchRepository.save(destinationBranch);
        } else {
            throw new Exception("Source or Destination branch not found.");
        }
    }

    private void mergeBranchContent(Branch sourceBranch, Branch destinationBranch) {
        // Merge folders first to ensure hierarchy
        mergeFolders(sourceBranch, destinationBranch);
        // Then merge files
        mergeFiles(sourceBranch, destinationBranch);
    }

    private void mergeFiles(Branch sourceBranch, Branch destinationBranch) {
//        List<File> sourceFiles = sourceBranch.getFiles();
//        List<File> destinationFiles = destinationBranch.getFiles();

        // Clone the parent branch's tree structure
        List<File> sourceFiles = sourceBranch.getFiles()
                .stream()
                .filter(folder -> folder.getContainer() == null) // Only include folders without a container
                .collect(Collectors.toList());

        // Retrieve only the root-level files (those that do not have a container)
        List<File> destinationFiles = destinationBranch.getFiles()
                .stream()
                .filter(file -> file.getContainer() == null) // Only include files without a container (i.e., root-level files)
                .collect(Collectors.toList());

        for (File sourceFile : sourceFiles) {
            Optional<File> destFileOpt = destinationFiles.stream()
                    .filter(destFile -> destFile.getName().equals(sourceFile.getName()))
                    .findFirst();

            if (destFileOpt.isPresent()) {
                File destFile = destFileOpt.get();
                handleFileConflict(sourceFile, destFile);
            } else {
                // Clone the file to the destination
                File newFile = cloneFileMerge(sourceFile, destinationBranch);
                destinationFiles.add(newFile); // Add to destination
            }
        }
    }

    private File cloneFileMerge(File sourceFile, Branch destinationBranch) {
        File newFile = new File();
        newFile.setName(sourceFile.getName());
        newFile.setContent(sourceFile.getContent());
        newFile.setVersion(sourceFile.getVersion());
        newFile.setTimestamp(sourceFile.getTimestamp());
        newFile.setBranch(destinationBranch); // Link to the destination branch
        fileRepository.save(newFile);

//        List<File> destinationBranchFiles = destinationBranch.getFiles();
//        destinationBranchFiles.add(newFile);
//        destinationBranch.setFiles(destinationBranchFiles);
//
//        branchRepository.save(destinationBranch);

        return newFile;
    }

    private void handleFileConflict(File sourceFile, File destinationFile) {
        // Conflict resolution strategy
        // Example: Keep both versions with a version suffix
        String newName = sourceFile.getName() + "_conflict_" + LocalDateTime.now();
        File newFile = new File();
        newFile.setName(newName);
        newFile.setContent(sourceFile.getContent());
        newFile.setVersion("conflict");
        newFile.setBranch(destinationFile.getBranch());

        // You can add any additional metadata or settings here
        destinationFile.getBranch().getFiles().add(newFile);
    }

    private void mergeFolders(Branch sourceBranch, Branch destinationBranch) {
//        List<Folder> sourceFolders = sourceBranch.getFolders();
//        List<Folder> destinationFolders = destinationBranch.getFolders();

        // Clone the parent branch's tree structure
        List<Folder> sourceFolders = sourceBranch.getFolders()
                .stream()
                .filter(folder -> folder.getContainer() == null) // Only include folders without a container
                .collect(Collectors.toList());

        // Retrieve only the root-level files (those that do not have a container)
        List<Folder> destinationFolders = destinationBranch.getFolders()
                .stream()
                .filter(file -> file.getContainer() == null) // Only include files without a container (i.e., root-level files)
                .collect(Collectors.toList());

        for (Folder sourceFolder : sourceFolders) {
            Optional<Folder> destFolderOpt = destinationFolders.stream()
                    .filter(destFolder -> destFolder.getName().equals(sourceFolder.getName()))
                    .findFirst();

            if (destFolderOpt.isPresent()) {
                Folder destFolder = destFolderOpt.get();
                handleFolderConflict(sourceFolder, destFolder);
            } else {
                // Clone the folder to the destination
                Folder newFolder = cloneFolderMerge(sourceFolder, destinationBranch);
                destinationFolders.add(newFolder); // Add to destination
            }
        }
    }

    private Folder cloneFolderMerge(Folder sourceFolder, Branch destinationBranch) {
        Folder clonedFolder = new Folder();

        clonedFolder.setName(sourceFolder.getName()); // Copy folder name
        clonedFolder.setBranch(destinationBranch); // Link the folder to the new branch

        clonedFolder.setFiles(new ArrayList<>(cloneFiles(new HashSet<>(sourceFolder.getFiles()), destinationBranch))); // Clone files inside this folder

        folderRepository.save(clonedFolder); // save so i can reference in the sub folders and sub files

        for (File file : clonedFolder.getFiles()) {
            file.setContainer(clonedFolder);
            fileRepository.save(file);
        }

        clonedFolder.setSubFolders(new ArrayList<>(cloneFolders(new HashSet<>(sourceFolder.getSubFolders()), destinationBranch))); // Recursively clone subfolders
        for (Folder folder : clonedFolder.getSubFolders()) {
            folder.setContainer(clonedFolder);
            folderRepository.save(folder);
        }

        // create folder in database
        folderRepository.save(clonedFolder);

//        List<Folder> destinationBranchFolders = destinationBranch.getFolders();
//        destinationBranchFolders.add(clonedFolder);
//        destinationBranch.setFolders(destinationBranchFolders);
//
//        branchRepository.save(destinationBranch);

        // create an empty clone
//        Folder newFolder = new Folder();
//        newFolder.setName(sourceFolder.getName());
//        newFolder.setBranch(destinationBranch); // Link to the destination branch
//        folderRepository.save(newFolder);
//
//        List<Folder> clonedFolders = new ArrayList<>();
//        for (Folder originalSubFolder : sourceFolder.getSubFolders()) {
//            Folder clonedFolder = cloneFolderMerge(originalSubFolder, destinationBranch);
//            clonedFolders.add(clonedFolder);
//        }
//
//        newFolder.setSubFolders(new ArrayList<>(clonedFolders));
//
//        for (Folder folder : newFolder.getSubFolders()) {
//            folder.setContainer(newFolder);
//            folderRepository.save(folder);
//        }
//
//        folderRepository.save(newFolder);
        return clonedFolder;
    }

    private void handleFolderConflict(Folder sourceFolder, Folder destinationFolder) {
        List<Folder> sourceSubFolders = sourceFolder.getSubFolders();
        List<Folder> destinationSubFolders = destinationFolder.getSubFolders();

        if(!(sourceSubFolders == null || sourceSubFolders.isEmpty())){
            for (Folder sourceSubFolder : sourceSubFolders) {
                // check if sourceSubFolder in destinationFolder
                Optional<Folder> destFolderOpt = destinationSubFolders.stream()
                        .filter(destFolder -> destFolder.getName().equals(sourceSubFolder.getName()))
                        .findFirst();

                // if it is then handleFolderConflict
                // if not then clone
                if (destFolderOpt.isPresent()) {
                    Folder destFolder = destFolderOpt.get();
                    handleFolderConflict(sourceSubFolder, destFolder);
                } else {
                    // Clone the folder to the destination
                    cloneFolderMerge(sourceSubFolder, destinationFolder.getBranch());
                }
            }

        }
        // duplicate folder only contains files
        if(!(sourceFolder.getFiles() == null || sourceFolder.getFiles().isEmpty())){
            // Example strategy: Merge contents, resolve conflicts for files
            mergeFiles(sourceFolder, destinationFolder);
        }
    }

    private void mergeFiles(Folder sourceFolder, Folder destinationFolder) {
        List<File> sourceFiles = sourceFolder.getFiles();
        List<File> destinationFiles = destinationFolder.getFiles();

        for (File sourceFile : sourceFiles) {
            // check if file exists in the destination
            Optional<File> destFileOpt = destinationFiles.stream()
                    .filter(destFile -> destFile.getName().equals(sourceFile.getName()))
                    .findFirst();

            // if it does, handle conflict
            // in not then clone it
            if (destFileOpt.isPresent()) {
                File destFile = destFileOpt.get();
                handleFileConflict(sourceFile, destFile); // Handle file conflict if needed
            } else {
                cloneFileMerge(sourceFile, destinationFolder.getBranch()); // Add to destination
            }
        }
    }

    //  !   ///////////////////////////////////////////////////////////////
}