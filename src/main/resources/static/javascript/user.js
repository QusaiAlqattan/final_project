document.addEventListener("DOMContentLoaded", function () {
    const createFolderBtn = document.getElementById("createFolderBtn");
    const createFileBtn = document.getElementById("createFileBtn");
    const modal = document.getElementById("modal");
    const closeModal = document.getElementById("closeModal");
    const itemName = document.getElementById("itemName");
    const insideFolderCheckbox = document.getElementById("insideFolder");
    const folderSelection = document.getElementById("folderSelection");
    const folderSelect = document.getElementById("folderSelect");
    const submitBtn = document.getElementById("submitBtn");
    let currentAction = "";

    createFolderBtn.addEventListener("click", function () {
        currentAction = "folder";
        openModal("Create Folder");
    });

    createFileBtn.addEventListener("click", function () {
        currentAction = "file";
        openModal("Create File");
    });

    closeModal.addEventListener("click", function () {
        closeModalFunction();
    });

    insideFolderCheckbox.addEventListener("change", function () {
        folderSelection.style.display = insideFolderCheckbox.checked ? "block" : "none";
        if (insideFolderCheckbox.checked) {
            fetchFolders();
        }
    });

    submitBtn.addEventListener("click", function () {
        submitItem();
    });

    function openModal(title) {
        document.getElementById("modalTitle").innerText = title;
        modal.style.display = "flex";
        itemName.value = ""; // Reset input
        insideFolderCheckbox.checked = false;
        folderSelection.style.display = "none";
        fetchFolders(); // Load folders when opening the modal
    }

    function closeModalFunction() {
        modal.style.display = "none";
    }

    async function fetchFolders() {
        try {
            const response = await fetch('/api/folders'); // Adjust the API endpoint accordingly
            const folders = await response.json();
            populateFolderSelect(folders);
        } catch (error) {
            console.error('Error fetching folders:', error);
        }
    }

    function populateFolderSelect(folders) {
        folderSelect.innerHTML = "";
        folders.forEach(folder => {
            const option = document.createElement("option");
            option.value = folder.uniqueId; // Assuming uniqueId is the identifier
            option.textContent = folder.name;
            folderSelect.appendChild(option);
        });
    }

    async function submitItem() {
        const name = itemName.value;
        const folderId = insideFolderCheckbox.checked ? folderSelect.value : null;

        const data = {
            name: name,
            insideFolderId: folderId // Use this key to indicate if it's inside a folder
        };

        try {
            const response = await fetch(`/api/${currentAction}s`, { // Adjust the API endpoint
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data),
            });
            if (response.ok) {
                alert(`${currentAction.charAt(0).toUpperCase() + currentAction.slice(1)} created successfully!`);
                closeModalFunction(); // Close modal after submission
            } else {
                alert('Error creating the item.');
            }
        } catch (error) {
            console.error('Error submitting the item:', error);
        }
    }
});
