document.addEventListener("DOMContentLoaded", function () {
    const editor = document.getElementById("editor");
    const runButton = document.getElementById("run-btn");
    const outputElement = document.getElementById("output");
    const languageSelect = document.getElementById("language");

    runButton.addEventListener('click', function() {
        const code = editor.value;
        const language = languageSelect.value;

        fetch('/run-code', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ code, language })
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