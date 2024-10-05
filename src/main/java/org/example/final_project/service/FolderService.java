package org.example.final_project.service;

import org.example.final_project.dto.FolderDTO;
import org.example.final_project.model.Branch;
import org.example.final_project.model.Folder;
import org.example.final_project.repository.BranchRepository;
import org.example.final_project.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final BranchRepository branchRepository;

    @Autowired
    public FolderService(FolderRepository folderRepository, BranchRepository branchRepository) {
        this.folderRepository = folderRepository;
        this.branchRepository = branchRepository;
    }

    // Retrieve all filtered Folders
    public List<FolderDTO> getAllFolders() {
        List<Folder> folders = folderRepository.findAll();
        List<FolderDTO> filteredFolders = new ArrayList<>();
        for (Folder folder : folders) {
            filteredFolders.add(toDTO(folder));
        }
        return filteredFolders;
    }

    // Save a new folder
    public void saveFolder(FolderDTO folderDTO) {
        Folder folder = new Folder();
        folder.setName(folderDTO.getName());

        // Set branch if provided
        if (folderDTO.getBranchId() != null && branchRepository.findById(folderDTO.getBranchId()).isPresent()) {
            Branch branch = branchRepository.findById(folderDTO.getBranchId()).get();
            folder.setBranch(branch);

            // update branch
            List<Folder> branchFolders = branch.getFolders();
            branchFolders.add(folder);
            branch.setFolders(branchFolders);
            branchRepository.save(branch);
        }

        // Set parent folder if provided (for nested folders)
        if (folderDTO.getContainerId() != null && folderRepository.findById(folderDTO.getContainerId()).isPresent()) {
            Folder container = folderRepository.findById(folderDTO.getContainerId()).get();
            folder.setContainer(container);

            // add to the sub folders in the container
            List<Folder> subFolders = container.getSubFolders();
            subFolders.add(folder);

            container.setSubFolders(subFolders);
            folderRepository.save(container);
        }

        folderRepository.save(folder);

    }

    private FolderDTO toDTO(Folder folder) {
        FolderDTO folderDTO = new FolderDTO();
        folderDTO.setUniqueId(folder.getUniqueId());
        folderDTO.setName(folder.getName());
        if (folder.getBranch() != null) {
            folderDTO.setBranchName(folder.getBranch().getName());
        }
        if (folder.getContainer() != null) {
            folderDTO.setContainerName(folder.getContainer().getName());
        }
        return folderDTO;
    }
}
