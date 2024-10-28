document.addEventListener("DOMContentLoaded", function () {
    const createFolderBtn = document.getElementById("createFolderBtn");
    const createFileBtn = document.getElementById("createFileBtn");
    const folderModal = document.getElementById("folderModal");
    const closeFolderModal = document.getElementById("closeFolderModal");
    const submitFolderBtn = document.getElementById("submitFolderBtn");
    const fileModal = document.getElementById("fileModal");
    const closeFileModal = document.getElementById("closeFileModal");
    const submitFileBtn = document.getElementById("submitFileBtn");
    const filesTable = document.getElementById("filesTable").querySelector("tbody");
    const folderNameInput = document.getElementById("folderName");
    const fileNameInput = document.getElementById("fileName");
    const foldersTable = document.getElementById("foldersTable").querySelector("tbody");
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    const currentUrl = window.location.href;
    let branchId = null;
    // Use a regular expression to match the last number
    const match = currentUrl.match(/\/(\d+)$/);

    if (match) {
        branchId = Number(match[1]);
    }

    fetchFiles();
    fetchFolders();

    // Open folder creation modal
    createFolderBtn.addEventListener("click", function () {
        folderModal.style.display = "flex";
        folderNameInput.value = "";
    });

    // Close folder modal
    closeFolderModal.addEventListener("click", function () {
        folderModal.style.display = "none";
    });

    // Open file creation modal
    createFileBtn.addEventListener("click", function () {
        fileModal.style.display = "flex";
        fileNameInput.value = ""; // Clear input
    });

    // Close file modal
    closeFileModal.addEventListener("click", function () {
        fileModal.style.display = "none";
    });

    // Fetch files and folders for the specific branch
    async function fetchFiles() {
        try {
            const response = await fetch(`/api/files/${branchId}`);
            const files = await response.json();
            populateFilesTable(files);
        } catch (error) {
            console.error('Error fetching files and folders:', error);
        }
    }

    // Fetch files and folders for the specific branch
    async function fetchFolders() {
        try {
            const response = await fetch(`/api/folders/${branchId}`);
            const folders = await response.json();
            populateFoldersTable(folders);
        } catch (error) {
            console.error('Error fetching files and folders:', error);
        }
    }

    // Populate the files and folders table
    function populateFilesTable(items) {
        filesTable.innerHTML = "";
        items.forEach(file => {
            const row = document.createElement("tr");

            const nameCell = document.createElement("td");
            const fileLink = document.createElement("a");
            fileLink.href = `/file/${file.uniqueId}`;  // Redirect to the file editor page
            fileLink.textContent = file.name;
            nameCell.appendChild(fileLink);

            const containerCell = document.createElement("td");
            containerCell.textContent = file.containerName ? file.containerName : "Root";

            const versionCell = document.createElement("td");
            versionCell.textContent = file.version ? file.version : "N/A";

            const timestampCell = document.createElement("td");
            timestampCell.textContent = file.timestamp ? file.timestamp : "N/A";

            // Create delete button cell
            const actionCell = document.createElement("td");
            const deleteButton = document.createElement("button");
            deleteButton.textContent = "Delete";
            deleteButton.style.backgroundColor = "red";
            deleteButton.style.color = "white";
            deleteButton.addEventListener("click", () => deleteFile(file.uniqueId));
            actionCell.appendChild(deleteButton);

            row.appendChild(nameCell);
            row.appendChild(containerCell);
            row.appendChild(versionCell);
            row.appendChild(timestampCell);
            row.appendChild(actionCell);

            filesTable.appendChild(row);
        });
    }

    // Function to delete a file
    async function deleteFile(fileId) {
        const confirmed = confirm("Are you sure you want to delete this file?");
        if (!confirmed) return;

        try {
            await fetch(`/api/files/${fileId}`, {
                method: 'DELETE',
                headers: {
                    [csrfHeader]: csrfToken, // Add CSRF token to headers
                },
            });
            fetchFiles();
        } catch (error) {
            console.error('Error deleting file:', error);
        }
    }

    // Populate the files and folders table
    function populateFoldersTable(items) {
        foldersTable.innerHTML = "";
        const folderContainerSelect = document.getElementById("folderContainer");
        const fileContainerSelect = document.getElementById("fileContainer");

        // Clear existing options in the selects
        folderContainerSelect.innerHTML = '<option value="root">Root</option>';
        fileContainerSelect.innerHTML = '<option value="root">Root</option>';

        items.forEach(folder => {
            const row = document.createElement("tr");

            const nameCell = document.createElement("td");
            nameCell.textContent = folder.name;

            const containerCell = document.createElement("td");
            containerCell.textContent = folder.containerName ? folder.containerName : "Root";

            // Create the delete button
            const actionsCell = document.createElement("td");
            const deleteBtn = document.createElement("button");
            deleteBtn.textContent = "Delete";
            deleteBtn.style.backgroundColor = "#e74c3c";
            deleteBtn.style.color = "white";
            deleteBtn.style.border = "none";
            deleteBtn.style.padding = "5px 10px";
            deleteBtn.style.cursor = "pointer";

            // Add event listener for folder deletion
            deleteBtn.addEventListener("click", function () {
                if (confirm(`Are you sure you want to delete the folder "${folder.name}"?`)) {
                    deleteFolderById(folder.uniqueId);
                }
            });

            actionsCell.appendChild(deleteBtn);

            row.appendChild(nameCell);
            row.appendChild(containerCell);
            row.appendChild(actionsCell);

            foldersTable.appendChild(row);

            // Populate the select options
            const option = document.createElement("option");
            option.value = folder.uniqueId; // Assuming folder has a uniqueId property
            option.textContent = folder.name;
            folderContainerSelect.appendChild(option);
            fileContainerSelect.appendChild(option.cloneNode(true)); // Clone the option for file modal
        });
    }

    async function deleteFolderById(folderId) {
        try {
            const response = await fetch(`/api/folders/${folderId}`, {
                method: 'DELETE',
                headers: {
                    [csrfHeader]: csrfToken, // Add CSRF token to headers
                },
            });

            if (response.ok) {
                alert("Folder deleted successfully");
                fetchFolders();
                fetchFiles();
            } else {
                alert("Error deleting folder. Please try again.");
            }
        } catch (error) {
            console.error('Error deleting folder:', error);
        }
    }

    // Submit folder creation
    submitFolderBtn.addEventListener("click", async function () {
        const folderName = folderNameInput.value;
        const response = await fetch(`/api/folders/${branchId}`);
        const folders = await response.json();

        // Check if the folder name already exists
        const folderExists = folders.some(folder => folder.name === folderName);
        if (folderExists) {
            alert('Folder name already exists');
            return; // Stop execution if folder name exists
        }
        let selectedContainer = document.getElementById("folderContainer").value;


        if (selectedContainer === "root"){
            selectedContainer = null;
        }

        if (!folderName) return;

        try {
            await fetch(`/api/folders/${branchId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken, // Add CSRF token to headers
                },
                body: JSON.stringify({ name: folderName, containerId: selectedContainer })
            });

            folderModal.style.display = "none";
            fetchFiles();
            fetchFolders();
        } catch (error) {
            console.error('Error creating folder:', error);
        }
    });

    submitFileBtn.addEventListener("click", async function () {
        const fileName = fileNameInput.value;
        const response = await fetch(`/api/files/${branchId}`);
        const files = await response.json();

        const fileExists = files.some(file => file.name === fileName);
        if (fileExists) {
            alert('File name already exists');
            return;
        }

        let selectedContainer = document.getElementById("fileContainer").value;

        if (selectedContainer === "root"){
            selectedContainer = null;
        }

        if (!fileName) return;

        try {
            await fetch(`/api/files/${branchId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken, // Add CSRF token to headers
                },
                body: JSON.stringify({ name: fileName, containerId: selectedContainer })
            });

            fileModal.style.display = "none";
            fetchFiles();
            fetchFolders();
        } catch (error) {
            console.error('Error creating file:', error);
        }
    });
});