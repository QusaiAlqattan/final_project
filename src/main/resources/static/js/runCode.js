document.addEventListener("DOMContentLoaded", function () {
    const editor = document.getElementById("editor");
    const runButton = document.getElementById("run-btn");
    const outputElement = document.getElementById("output");
    const languageSelect = document.getElementById("language");
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    const fileId = window.location.pathname.split("/").pop();

    runButton.addEventListener('click', function() {
        const code = editor.value;
        const language = languageSelect.value;

        fetch('/run-code', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ code, language, fileId })
        })
            .then(response => response.json())
            .then(data => {
                outputElement.textContent = data.output;
            })
            .catch(error => {
                outputElement.textContent = `Error: ${error.message}`;
            });
    });
});