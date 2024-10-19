// Handle form submission
document.getElementById('registrationForm').addEventListener('submit', async function(event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    const userData = {
        username: username,
        password: password,
    };

    // Send POST request to create a new user
    const response = await fetch('/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData),
    });

    if (response.ok) {
        alert('User registered successfully!');
    } else {
        alert('Failed to register user. Please try again.');
    }
});