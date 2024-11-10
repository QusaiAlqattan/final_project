package org.example.final_project.service;

import org.example.final_project.dto.BranchDTO;
import org.example.final_project.model.Branch;
import org.example.final_project.model.File;
import org.example.final_project.model.Folder;
import org.example.final_project.repository.BranchRepository;
import org.example.final_project.repository.FileRepository;
import org.example.final_project.repository.FolderRepository;
import org.example.final_project.repository.SystemUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BranchService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final BranchRepository branchRepository;
    private final SystemUserRepository systemUserRepository;

    @Autowired
    public BranchService(FileRepository fileRepository, FolderRepository folderRepository, BranchRepository branchRepository, SystemUserRepository systemUserRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.branchRepository = branchRepository;
        this.systemUserRepository = systemUserRepository;
    }

    //  !   ///////////////////////////////////////////////////////////////
    //  !   fetch branch
    //  !   ///////////////////////////////////////////////////////////////
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
    @Transactional
    public void createBranch(BranchDTO branchDTO) {

        Branch branch = new Branch();
        branch.setName(branchDTO.getName());

        try {
            // Save the new branch first to generate its ID, so i can use it later
            branch = branchRepository.save(branch);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("A branch with this name already exists.");
        }

        // If the branch has a parent branch
        if (branchDTO.getParentBranchId() != null) {
            Optional<Branch> parentBranchOpt = branchRepository.findById(branchDTO.getParentBranchId());
            if (parentBranchOpt.isPresent()) {
                Branch parentBranch = parentBranchOpt.get();

                // Clone the parent branch's tree structure
                // Retrieve only the root-level files/folders (those that do not have a container)
                Set<Folder> rootFolders = parentBranch.getFolders()
                        .stream()
                        .filter(folder -> folder.getContainer() == null)
                        .collect(Collectors.toSet());

                Set<File> rootFiles = parentBranch.getFiles()
                        .stream()
                        .filter(file -> file.getContainer() == null)
                        .collect(Collectors.toSet());

                Set<Folder> clonedFolders = cloneFolders(rootFolders, branch);
                Set<File> clonedFiles = cloneFiles(rootFiles, branch);

                branch.setFolders(new ArrayList<>(clonedFolders));
                branch.setFiles(new ArrayList<>(clonedFiles));
            }
        }

        branch.setTimestamp(LocalDateTime.now());

        try {
            branchRepository.save(branch);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("A branch with this name already exists.");
        }
    }

    // Recursively clones folders and subfolders
    private Set<Folder> cloneFolders(Set<Folder> originalFolders, Branch newBranch) {
        Set<Folder> clonedFolders = new HashSet<>();

        for (Folder originalFolder : originalFolders) {

            Folder clonedFolder = new Folder();

            clonedFolder.setName(originalFolder.getName());
            clonedFolder.setBranch(newBranch);

            // Clone files inside this folder
            clonedFolder.setFiles(
                    new ArrayList<>(
                            cloneFiles(
                                    new HashSet<>(originalFolder.getFiles()), newBranch)));

            // save so i can reference it in the sub folders and sub files
            folderRepository.save(clonedFolder);

            for (File file : clonedFolder.getFiles()) {
                file.setContainer(clonedFolder);
                fileRepository.save(file);
            }

            clonedFolder.setSubFolders(new ArrayList<>(cloneFolders(new HashSet<>(originalFolder.getSubFolders()), newBranch))); // Recursively clone subfolders
            for (Folder folder : clonedFolder.getSubFolders()) {
                folder.setContainer(clonedFolder);
                folderRepository.save(folder);
            }

            // update branch folders
            if (newBranch.getFolders() != null){
                List<Folder> branchFolders = newBranch.getFolders();
                branchFolders.add(clonedFolder);
                newBranch.setFolders(branchFolders);
                branchRepository.save(newBranch);
            }else{
                List<Folder> clonedFolderList = new ArrayList<>();
                clonedFolderList.add(clonedFolder);
                newBranch.setFolders(clonedFolderList);
                branchRepository.save(newBranch);
            }

            clonedFolders.add(clonedFolder);
            folderRepository.save(clonedFolder);
       }
        return clonedFolders;
    }

    // Clones files within a folder or branch
    private Set<File> cloneFiles(Set<File> originalFiles, Branch newBranch) {
        Set<File> clonedFiles = new HashSet<>();

        for (File originalFile : originalFiles) {
            File clonedFile = new File();

            clonedFile.setName(originalFile.getName());
            clonedFile.setContent(originalFile.getContent());
            clonedFile.setBranch(newBranch);
            clonedFile.setVersion(originalFile.getVersion());
            clonedFile.setTimestamp(originalFile.getTimestamp());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            clonedFile.setCreator(systemUserRepository.findByUsername(auth.getName()));


            clonedFiles.add(clonedFile);

            // create file in database
            fileRepository.save(clonedFile);

            // update branch files
            if (newBranch.getFiles() != null){
                List<File> branchFiles = newBranch.getFiles();
                branchFiles.add(clonedFile);
            }else{
                List<File> clonedFileList = new ArrayList<>();
                clonedFileList.add(clonedFile);
                newBranch.setFiles(clonedFileList);
                branchRepository.save(newBranch);
            }
        }

        return clonedFiles;
    }
    //  !   ///////////////////////////////////////////////////////////////


    //  !   ///////////////////////////////////////////////////////////////
    //  !   merge branches
    //  !   ///////////////////////////////////////////////////////////////
    @Transactional
    public void mergeBranches(Long sourceBranchId, Long destinationBranchId) {
        Optional<Branch> sourceBranchOpt = branchRepository.findById(sourceBranchId);
        Optional<Branch> destinationBranchOpt = branchRepository.findById(destinationBranchId);

        if ((sourceBranchOpt.isPresent() && destinationBranchOpt.isPresent()) && !(sourceBranchOpt.get().getName().equals(destinationBranchOpt.get().getName()))) {
            Branch sourceBranch = sourceBranchOpt.get();
            Branch destinationBranch = destinationBranchOpt.get();

            mergeBranchContent(sourceBranch, destinationBranch);
            branchRepository.save(destinationBranch);
        }
    }

    private void mergeBranchContent(Branch sourceBranch, Branch destinationBranch) {
        mergeFolders(sourceBranch, destinationBranch);

        mergeFiles(sourceBranch, destinationBranch);

        branchRepository.save(destinationBranch);
    }

    private void mergeFiles(Branch sourceBranch, Branch destinationBranch) {
        // Merge the files in root
        List<File> sourceFiles = sourceBranch.getFiles()
                .stream()
                .filter(folder -> folder.getContainer() == null)
                .collect(Collectors.toList());

        List<File> destinationFiles = destinationBranch.getFiles()
                .stream()
                .filter(file -> file.getContainer() == null)
                .collect(Collectors.toList());

        for (File sourceFile : sourceFiles) {
            Optional<File> destFileOpt = destinationFiles.stream()
                    .filter(destFile -> destFile.getName().equals(sourceFile.getName()))
                    .findFirst();

            if (destFileOpt.isPresent()) {
                File destFile = destFileOpt.get();
                File newFile = handleFileConflict(sourceFile, destFile);
                destinationFiles.add(newFile);
            } else {
                File newFile = cloneFileMerge(sourceFile, destinationBranch);
                destinationFiles.add(newFile);
            }
        }
        destinationBranch.setFiles(destinationFiles);
    }

    private File cloneFileMerge(File sourceFile, Branch destinationBranch) {
        File newFile = new File();
        newFile.setName(sourceFile.getName());
        newFile.setContent(sourceFile.getContent());
        newFile.setVersion(sourceFile.getVersion());
        newFile.setTimestamp(sourceFile.getTimestamp());
        newFile.setBranch(destinationBranch);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        newFile.setCreator(systemUserRepository.findByUsername(auth.getName()));

        fileRepository.save(newFile);

        return newFile;
    }

    private File handleFileConflict(File sourceFile, File destinationFile) {
        String newName = sourceFile.getName() + "_conflict";
        File newFile = new File();
        newFile.setName(newName);
        newFile.setContent(sourceFile.getContent());
        newFile.setVersion(sourceFile.getVersion());
        newFile.setTimestamp(sourceFile.getTimestamp());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        newFile.setCreator(systemUserRepository.findByUsername(auth.getName()));

        Branch destinationBranch = destinationFile.getBranch();
        newFile.setBranch(destinationBranch);

        fileRepository.save(newFile);

        return newFile;
    }

    private void mergeFolders(Branch sourceBranch, Branch destinationBranch) {
        // merge folder in the Root
        List<Folder> sourceFolders = sourceBranch.getFolders()
                .stream()
                .filter(folder -> folder.getContainer() == null)
                .collect(Collectors.toList());

        List<Folder> destinationFolders = destinationBranch.getFolders()
                .stream()
                .filter(file -> file.getContainer() == null)
                .collect(Collectors.toList());

        for (Folder sourceFolder : sourceFolders) {
            Optional<Folder> destFolderOpt = destinationFolders.stream()
                    .filter(destFolder -> destFolder.getName().equals(sourceFolder.getName()))
                    .findFirst();

            if (destFolderOpt.isPresent()) {
                Folder destFolder = destFolderOpt.get();
                handleFolderConflict(sourceFolder, destFolder);
            } else {
                Folder newFolder = cloneFolderMerge(sourceFolder, destinationBranch);
                destinationFolders.add(newFolder);
            }
        }
    }

    private Folder cloneFolderMerge(Folder sourceFolder, Branch destinationBranch) {
        Folder clonedFolder = new Folder();

        clonedFolder.setName(sourceFolder.getName());
        clonedFolder.setBranch(destinationBranch);

        clonedFolder.setFiles(new ArrayList<>(cloneFiles(new HashSet<>(sourceFolder.getFiles()), destinationBranch)));

        // save so i can reference in the sub folders and sub files
        folderRepository.save(clonedFolder);

        for (File file : clonedFolder.getFiles()) {
            file.setContainer(clonedFolder);
            fileRepository.save(file);
        }

        // Recursively clone subfolders
        clonedFolder.setSubFolders(new ArrayList<>(cloneFolders(new HashSet<>(sourceFolder.getSubFolders()), destinationBranch)));
        for (Folder folder : clonedFolder.getSubFolders()) {
            folder.setContainer(clonedFolder);
            folderRepository.save(folder);
        }

        folderRepository.save(clonedFolder);
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
                    Folder clonedsourceSubFolder = cloneFolderMerge(sourceSubFolder, destinationFolder.getBranch());
                    clonedsourceSubFolder.setContainer(destinationFolder);
                    folderRepository.save(clonedsourceSubFolder);
                }
            }

        }

        // duplicate folder contains files
        if(!(sourceFolder.getFiles() == null || sourceFolder.getFiles().isEmpty())){
            mergeFiles(sourceFolder, destinationFolder);
        }
    }

    private void mergeFiles(Folder sourceFolder, Folder destinationFolder) {
        // Merge the files in folder
        List<File> sourceFiles = sourceFolder.getFiles();
        List<File> destinationFiles = destinationFolder.getFiles();

        for (File sourceFile : sourceFiles) {
            Optional<File> destFileOpt = destinationFiles.stream()
                    .filter(destFile -> destFile.getName().equals(sourceFile.getName()))
                    .findFirst();

            // if it does, handle conflict
            // in not then clone it
            if (destFileOpt.isPresent()) {
                File destFile = destFileOpt.get();
                File conflictFile = handleFileConflict(sourceFile, destFile);
                conflictFile.setContainer(destinationFolder);
                fileRepository.save(conflictFile);
            } else {
                File clonedSourceFile = cloneFileMerge(sourceFile, destinationFolder.getBranch());
                clonedSourceFile.setContainer(destinationFolder);

                fileRepository.save(clonedSourceFile);
            }
        }
    }
    //  !   ///////////////////////////////////////////////////////////////


    //  !   ///////////////////////////////////////////////////////////////
    //  !   deletes branches
    //  !   ///////////////////////////////////////////////////////////////
    public void deleteBranch(Long branchId) {
        // Check if branch exists, throw exception if not
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        // Delete the branch
        branchRepository.delete(branch);
    }
    //  !   ///////////////////////////////////////////////////////////////

}