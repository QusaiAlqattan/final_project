// Fetch users and populate the table
async function fetchUsers() {
    const response = await fetch('/admin/api/users'); // Adjust this API endpoint as needed
    const users = await response.json();
    const userTableBody = document.getElementById('userTableBody');
    userTableBody.innerHTML = ''; // Clear existing rows

    users.forEach(user => {
        const row = document.createElement('tr');
        row.innerHTML = `
                    <td>${user.username}</td>
                    <td>
                        <select class="form-control" id="roleSelect-${user.uniqueId}">
                            <!-- Populate role options dynamically -->
                        </select>
                    </td>
                    <td>
                        <button class="btn btn-primary" onclick="saveRole(${user.uniqueId})">Save</button>
                        <button class="btn btn-danger" onclick="confirmDelete(${user.uniqueId})">Delete</button>
                    </td>
                `;
        userTableBody.appendChild(row);
        populateRoles(row.querySelector(`#roleSelect-${user.uniqueId}`), user.roleId); // Pass user role ID
    });
}

async function populateRoles(selectElement, currentRoleId) {
    const response = await fetch('/admin/api/roles'); // API to get all roles
    const roles = await response.json();

    roles.forEach(role => {
        const option = document.createElement('option');
        option.value = role.uniqueId; // Role ID
        option.textContent = role.name; // Role Name

        // Set the current role as selected
        if (role.uniqueId === currentRoleId) {
            option.selected = true; // Select the user's current role
        }
        selectElement.appendChild(option);
    });
}

async function saveRole(userId) {
    const selectElement = document.getElementById(`roleSelect-${userId}`);
    const newRoleId = selectElement.value;

    const response = await fetch(`/admin/api/users/${userId}/role`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ roleId: newRoleId }), // Send role ID
    });

    if (response.ok) {
        alert('Role updated successfully!');
        fetchUsers(); // Refresh the user list
    } else {
        alert('Failed to update role.');
    }
}

async function confirmDelete(userId) {
    if (confirm('Are you sure you want to delete this user?')) {
        const response = await fetch(`/admin/api/users/${userId}`, {
            method: 'DELETE',
        });

        if (response.ok) {
            alert('User deleted successfully!');
            fetchUsers(); // Refresh the user list
        } else {
            alert('Failed to delete user.');
        }
    }
}

// Fetch users when the page loads
window.onload = fetchUsers;