package org.example.final_project.service;

import org.example.final_project.dto.FileDTO;
import org.example.final_project.dto.FolderDTO;
import org.example.final_project.model.Branch;
import org.example.final_project.model.File;
import org.example.final_project.model.Folder;
import org.example.final_project.repository.BranchRepository;
import org.example.final_project.repository.FileRepository;
import org.example.final_project.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final BranchRepository branchRepository;

    @Autowired
    public FileService(FileRepository fileRepository, FolderRepository folderRepository, BranchRepository branchRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.branchRepository = branchRepository;
    }

    // Retrieve all filtered Files
    public List<FileDTO> getAllFiles() {
        List<File> files = fileRepository.findAll();
        List<FileDTO> filteredFiles = new ArrayList<>();
        for (File file : files) {
            filteredFiles.add(toDTO(file));
        }
        return filteredFiles;
    }

    public void createFile(FileDTO fileDto) {
        File file = new File();
        file.setName(fileDto.getName());

        file.setContent(fileDto.getContent());

        file.setVersion("1"); // Default version

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
        if (fileDto.getBranchId() != null && branchRepository.findById(fileDto.getBranchId()).isPresent()) {
            Branch branch = branchRepository.findById(fileDto.getBranchId()).get();
            file.setBranch(branch);

            // update branch
            List<File> branchFiles = branch.getFiles();
            branchFiles.add(file);
            branch.setFiles(branchFiles);
            branchRepository.save(branch);
        }

        //TODO: set creator, after managing sessions & oauth

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