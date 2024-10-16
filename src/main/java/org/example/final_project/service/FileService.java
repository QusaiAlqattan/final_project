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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    //  !   ///////////////////////////////////////////////////////////////
    //  !   fetch files
    //  !   ///////////////////////////////////////////////////////////////
    // Retrieve all branch Files
    public List<FileDTO> getFiles(Long branchId) {
        List<File> files = fileRepository.findByBranch_UniqueId(branchId);
        List<FileDTO> filteredFiles = new ArrayList<>();
        for (File file : files) {
            filteredFiles.add(toDTO(file));
        }
        return filteredFiles;
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

    public String getFileContent(Long fileId) {
        File file = fileRepository.findById(fileId).get();
        return file.getContent();
    }
    //  !   ///////////////////////////////////////////////////////////////



    //  !   ///////////////////////////////////////////////////////////////
    //  !   create file
    //  !   ///////////////////////////////////////////////////////////////
    @Transactional
    public void createFile(FileDTO fileDto, Long branchId, String content) {
        try {
            File file = new File();
            file.setName(fileDto.getName());
            file.setContent(content);
            String newVersion = lastVersion(fileDto, branchId);
            file.setVersion(newVersion);

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

            try{
                fileRepository.save(file);
            } catch (DataIntegrityViolationException e) {
                throw new IllegalArgumentException("A file with this name and version already exists in the branch.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't create a file.: "+e.getMessage());
        }
    }

    private String lastVersion(FileDTO fileDTO, Long branchId){
        Branch branch = branchRepository.findById(branchId).get();
        List<File> files = fileRepository.findByBranch_UniqueId(branch.getUniqueId());
        Long lastVersion = 0L;
        for (File f : files) {
            if (f.getName().equals(fileDTO.getName())) {
                if (lastVersion < Long.parseLong(f.getVersion())) {
                    lastVersion = Long.parseLong(f.getVersion());
                }
            }
        }

        return String.valueOf(lastVersion + 1);
    }
    //  !   ///////////////////////////////////////////////////////////////



    //  !   ///////////////////////////////////////////////////////////////
    //  !   delete files
    //  !   ///////////////////////////////////////////////////////////////
    public void deleteFileById(Long fileId) {
        Optional<File> file = fileRepository.findById(fileId);
        if (file.isPresent()) {
            fileRepository.deleteById(fileId);
        } else {
            throw new RuntimeException("File not found");
        }
    }
    //  !   ///////////////////////////////////////////////////////////////

}