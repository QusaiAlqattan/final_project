package org.example.final_project.service;

import org.example.final_project.dto.FileDTO;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final BranchRepository branchRepository;
    private final SystemUserRepository systemUserRepository;

    @Autowired
    public FileService(FileRepository fileRepository, FolderRepository folderRepository, BranchRepository branchRepository, SystemUserRepository systemUserRepository) {

        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.branchRepository = branchRepository;
        this.systemUserRepository = systemUserRepository;
    }

    // Retrieve all filtered Files
    public List<FileDTO> getFiles(Long branchId) {
        List<File> files = fileRepository.findByBranch_UniqueId(branchId);
        List<FileDTO> filteredFiles = new ArrayList<>();
        for (File file : files) {
            filteredFiles.add(toDTO(file));
        }
        return filteredFiles;
    }

    public void createFile(FileDTO fileDto, Long branchId) {
        File file = new File();
        file.setName(fileDto.getName());

        file.setContent(fileDto.getContent());

        List<File> branchFiles = fileRepository.findByBranch_UniqueId(branchId);
        Boolean duplicate = false;
        for (File branchFile : branchFiles) {
            if (branchFile.getName().equals(fileDto.getName())) {
                duplicate = true;
                file.setVersion(String.valueOf(Long.parseLong(branchFile.getVersion()) + 1));
            }
        }
        if (!duplicate) {
            file.setVersion("1"); // Default version
        }

        file.setTimestamp(LocalDateTime.now());

        // Set folder if provided
        if (fileDto.getContainerId() != null && folderRepository.findById(fileDto.getContainerId()).isPresent()) {
            Folder container = folderRepository.findById(fileDto.getContainerId()).get();
            file.setContainer(container);

            // add to the sub folders in the container
            List<File> subFiles = container.getFiles();
            subFiles.add(file);
            container.setFiles(subFiles);
            folderRepository.save(container);
        }

        // Set branch if provided
        Branch branch = branchRepository.findById(branchId).get();
        file.setBranch(branch);

        // update branch
        List<File> oldBranchFiles = branch.getFiles();
        oldBranchFiles.add(file);
        branch.setFiles(oldBranchFiles);
        branchRepository.save(branch);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SystemUser user = systemUserRepository.findByUsername(auth.getName());
        file.setCreator(user);

        fileRepository.save(file);

    }

//    public File getFileById(Long id) {
//        return fileRepository.findById(id).orElse(null);
//    }
//
//    public File updateFile(Long id, FileDTO fileDto) {
//        File file = getFileById(id);
//        if (file != null) {
//            file.setName(fileDto.getName());
//            file.setContents(fileDto.getContents());
//            // Update version and timestamp or other fields as necessary
//            file.setVersion("2"); // Increment version, logic can be improved based on your requirements
//            file.setTimestamp(LocalDateTime.now());
//            // Optionally update relationships if needed
//            return fileRepository.save(file);
//        }
//        return null; // or throw an exception
//    }
//
//    public void deleteFile(Long id) {
//        fileRepository.deleteById(id);
//    }
    public String getFileContent(Long fileId) {
        File file = fileRepository.findById(fileId).get();
        return file.getContent();
    }

    private FileDTO toDTO(File file) {
        FileDTO fileDTO = new FileDTO();
        fileDTO.setUniqueId(file.getUniqueId());
        fileDTO.setName(file.getName());
        fileDTO.setTimestamp(file.getTimestamp());
        fileDTO.setVersion(file.getVersion());
        fileDTO.setContent(file.getContent());

        if (file.getBranch() != null) {
            fileDTO.setBranchName(file.getBranch().getName());
        }
        if (file.getContainer() != null) {
            fileDTO.setContainerName(file.getContainer().getName());
        }
        return fileDTO;
    }
}