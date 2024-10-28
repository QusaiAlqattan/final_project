// Handle form submission
document.getElementById('registrationForm').addEventListener('submit', async function(event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    // Get the CSRF token and header name from meta tags
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    const userData = {
        username: username,
        password: password,
    };

    // Send POST request to create a new user
    const response = await fetch('/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken, // Add CSRF token to the headers
        },
        body: JSON.stringify(userData),
    });

    if (response.ok) {
        alert('User registered successfully!');
    } else {
        alert('Failed to register user. Please try again.');
    }
});