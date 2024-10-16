package org.example.final_project.service;

import org.example.final_project.dto.FolderDTO;
import org.example.final_project.model.Branch;
import org.example.final_project.model.Folder;
import org.example.final_project.repository.BranchRepository;
import org.example.final_project.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final BranchRepository branchRepository;

    @Autowired
    public FolderService(FolderRepository folderRepository, BranchRepository branchRepository) {
        this.folderRepository = folderRepository;
        this.branchRepository = branchRepository;
    }

    //  !   ///////////////////////////////////////////////////////////////
    //  !   fetch folders
    //  !   ///////////////////////////////////////////////////////////////
    // Retrieve all branch Folders
    public List<FolderDTO> getFolders(Long branchId) {
        List<Folder> folders = folderRepository.findByBranch_UniqueId(branchId);
        List<FolderDTO> filteredFolders = new ArrayList<>();
        for (Folder folder : folders) {
            filteredFolders.add(toDTO(folder));
        }
        return filteredFolders;
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
    //  !   ///////////////////////////////////////////////////////////////



    //  !   ///////////////////////////////////////////////////////////////
    //  !   create folders
    //  !   ///////////////////////////////////////////////////////////////
    @Transactional
    public void saveFolder(FolderDTO folderDTO, Long branchId) {
        Folder folder = new Folder();
        folder.setName(folderDTO.getName());

        // Set branch if provided
        Branch branch = branchRepository.findById(branchId).get();
        folder.setBranch(branch);

        // update branch
        List<Folder> branchFolders = branch.getFolders();
        branchFolders.add(folder);
        branch.setFolders(branchFolders);
        branchRepository.save(branch);

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

        try{
            folderRepository.save(folder);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("A folder with this name already exists in the branch.");
        }

    }
    //  !   ///////////////////////////////////////////////////////////////



    //  !   ///////////////////////////////////////////////////////////////
    //  !   delete folder
    //  !   ///////////////////////////////////////////////////////////////
    public void deleteFolderById(Long folderId) {
        Optional<Folder> folder = folderRepository.findById(folderId);
        if (folder.isPresent()) {
            folderRepository.deleteById(folderId);
        } else {
            throw new RuntimeException("Folder not found with id: " + folderId);
        }
    }
    //  !   ///////////////////////////////////////////////////////////////
}
