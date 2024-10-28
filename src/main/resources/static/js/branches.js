const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

document.addEventListener("DOMContentLoaded", function () {
    // fetch and populate branches tables and dropdowns
    fetchBranches();

    document.getElementById("openModalButton").addEventListener("click", openCreateBranchModal);
    document.getElementById("closeCreateBranchModal").addEventListener("click", closeCreateBranchModal);

    document.getElementById("openMergeModalButton").addEventListener("click", openMergeBranchModal);
    document.getElementById("closeMergeBranchModal").addEventListener("click", closeMergeBranchModal);

    document.getElementById("createBranchButton").addEventListener("click", createBranch);
    document.getElementById("mergeBranchButton").addEventListener("click", mergeBranches);
});

async function fetchBranches() {
    try {
        const response = await fetch('/api/branches');
        const branches = await response.json();
        populateBranchesTable(branches);
        populateBranchDropdowns(branches);
    } catch (error) {
        console.error('Error fetching branches:', error);
    }
}

async function createBranch() {
    const branchName = document.getElementById("branchName").value;
    const parentBranchId = document.getElementById("parentBranch").value;

    if (!branchName) {
        alert('Branch name is required.');
        return;
    }

    const response = await fetch('/api/branches');
    const branches = await response.json();

    const branchExists = branches.some(branch => branch.name === branchName);
    if (branchExists) {
        alert('Branch name already exists');
        return;
    }

    try {
        const response = await fetch('/api/branches', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken, // Add CSRF token to headers
            },
            body: JSON.stringify({
                name: branchName,
                parentBranchId: parentBranchId,
            }),
        });

        if (response.ok) {
            alert('Branch created successfully!');
            fetchBranches();
            closeCreateBranchModal();
        } else {
            alert('Failed to create branch.');
        }
    } catch (error) {
        console.error('Error creating branch:', error);
    }
}

async function mergeBranches() {
    const sourceBranchId = document.getElementById("sourceBranch").value;
    const targetBranchId = document.getElementById("targetBranch").value;

    if (!sourceBranchId || !targetBranchId) {
        alert('Both source and target branches are required.');
        return;
    }

    if (sourceBranchId === targetBranchId) {
        alert('Source and target branches must be different.');
        return;
    }

    try {
        const response = await fetch('/api/branches/merge', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken, // Add CSRF token to headers
            },
            body: JSON.stringify({
                sourceBranchId: sourceBranchId,
                destinationBranchId: targetBranchId,
            }),
        });

        if (response.ok) {
            alert('Branch merged successfully!');
            fetchBranches();
            closeMergeBranchModal();
        } else {
            alert('Failed to merge branches.');
        }
    } catch (error) {
        console.error('Error merging branches:', error);
    }
}

function populateBranchesTable(branches) {
    const branchesTableBody = document.getElementById("branchesTableBody");
    branchesTableBody.innerHTML = "";

    branches.forEach(branch => {
        const row = document.createElement("tr");

        const nameCell = document.createElement("td");
        const branchLink = document.createElement("a");
        branchLink.href = `branch-detail/${branch.uniqueId}`;
        branchLink.textContent = branch.name;
        nameCell.appendChild(branchLink);

        const timestampCell = document.createElement("td");
        timestampCell.textContent = branch.timestamp;

        const actionsCell = document.createElement("td");
        const deleteButton = document.createElement("button");
        deleteButton.textContent = "Delete";
        deleteButton.classList.add('delete-btn');
        deleteButton.addEventListener("click", () => deleteBranch(branch.uniqueId));
        actionsCell.appendChild(deleteButton);

        row.appendChild(nameCell);
        row.appendChild(timestampCell);
        row.appendChild(actionsCell);

        branchesTableBody.appendChild(row);
    });
}

function populateBranchDropdowns(branches) {
    const sourceBranchSelect = document.getElementById("sourceBranch");
    const targetBranchSelect = document.getElementById("targetBranch");
    const parentBranchSelect = document.getElementById("parentBranch");

    parentBranchSelect.innerText = "";
    sourceBranchSelect.innerHTML = "";
    targetBranchSelect.innerHTML = "";

    // default option for parent branch
    let option = document.createElement("option");
    option.value = "";
    option.textContent = "None";
    parentBranchSelect.appendChild(option);

    branches.forEach(branch => {
        const sourceOption = document.createElement("option");
        sourceOption.value = branch.uniqueId;
        sourceOption.textContent = branch.name;
        sourceBranchSelect.appendChild(sourceOption);

        const targetOption = document.createElement("option");
        targetOption.value = branch.uniqueId;
        targetOption.textContent = branch.name;
        targetBranchSelect.appendChild(targetOption);

        const parentOption = document.createElement("option");
        parentOption.value = branch.uniqueId;
        parentOption.textContent = branch.name;
        parentBranchSelect.appendChild(parentOption);
    });
}

async function deleteBranch(branchId) {
    const confirmDelete = confirm("Are you sure you want to delete this branch?");

    if (!confirmDelete) {
        return;
    }

    try {
        const response = await fetch(`/api/branches/${branchId}`, {
            method: 'DELETE',
            headers: {
                [csrfHeader]: csrfToken, // Add CSRF token to headers
            },
        });

        if (response.ok) {
            alert('Branch deleted successfully!');
            fetchBranches();
        } else {
            alert('Failed to delete branch.');
        }
    } catch (error) {
        console.error('Error deleting branch:', error);
    }
}

function openCreateBranchModal() {
    document.getElementById("createBranchModal").style.display = "block";
}

function closeCreateBranchModal() {
    document.getElementById("createBranchModal").style.display = "none";
    document.getElementById("branchName").value = ""; // Clear input
    document.getElementById("parentBranch").value = ""; // Reset dropdown
}

function openMergeBranchModal() {
    document.getElementById("mergeBranchModal").style.display = "block";
}

function closeMergeBranchModal() {
    document.getElementById("mergeBranchModal").style.display = "none";
    document.getElementById("sourceBranch").value = "";
    document.getElementById("targetBranch").value = "";
}

// Close the modals if the user clicks outside of them
window.onclick = function(event) {
    const createBranchModal = document.getElementById("createBranchModal");
    const mergeBranchModal = document.getElementById("mergeBranchModal");
    if (event.target == createBranchModal) {
        closeCreateBranchModal();
    } else if (event.target == mergeBranchModal) {
        closeMergeBranchModal();
    }
}