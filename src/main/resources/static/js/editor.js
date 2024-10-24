document.addEventListener("DOMContentLoaded", function () {
    const fileId = window.location.pathname.split("/").pop(); // Extract fileId from URL
    const socket = new WebSocket(`ws://localhost:8080/ws/file/${fileId}`);
    const editor = document.getElementById("editor");
    let oldContent = '';

    // Fetch and display notes for the file
    function loadNotes() {
        fetch(`/api/notes/${fileId}`)
            .then(response => response.json())
            .then(notes => {
                const notesSection = document.getElementById("notesSection");
                notesSection.innerHTML = '';

                notes.forEach(note => {
                    const noteElement = document.createElement("div");
                    noteElement.classList.add("note");
                    noteElement.innerHTML = `
                                <div class="note-header">Line: ${note.rowNumber} | Written by: ${note.writerName}</div>
                                <div class="note-content">${note.content}</div>
                            `;
                    notesSection.appendChild(noteElement);
                });
            })
            .catch(error => {
                console.error('Error fetching notes:', error);
            });
    }

    loadNotes();

    function updateText(value, length, position, type){
        let currentValue = editor.value;
        if (position < 0 || length < 0) {
            throw new Error("Invalid start position or length");
        }

        if (type === 'Insert'){
            let before = currentValue.slice(0, position);
            let after = currentValue.slice(position);

            editor.value = before + value + after;
        }else{
            // if (value.length === 0){
            //     editor.value = '';
            // }else{
                // Calculate the end position of the substring to be removed
                let endPosition = position + length;

                // Slice before and after the substring and concatenate the result
                const before = currentValue.slice(0, position);
                const after = currentValue.slice(endPosition);

                editor.value = before + after;
            // }
        }
        oldContent = editor.value;
    }

    socket.onmessage = function (event) {
        try {
            let data = JSON.parse(event.data);
            console.log(data)
            if (data.type.length >= 0) {
                if (data.fullContent){
                    editor.value = data.value;
                    oldContent = data.value;
                }else{
                    updateText(data.value, data.length, data.position, data.type);
                }
            } else {
                console.error('Content not found in received data');
            }
        } catch (error) {
            console.error('Error parsing message:', error);
        }
    };

    // Send editor changes to the server in real-time
    document.getElementById("editor").addEventListener("input", function () {
        let startPosition =  editor.selectionStart;
        let newData = editor.value;
        let type = 'Delete';
        let value = findExtraSubstring(newData, oldContent);
        let offset = value.length;

        if (newData.length > oldContent.length){
            type = 'Insert';
            startPosition = startPosition - offset;
        }

        console.log('newData: ', newData);
        console.log('oldContent: ', oldContent)

        console.log('position: ', startPosition)
        console.log('value: ', value)
        console.log('type: ', type)
        console.log('offset: ', offset)

        socket.send(JSON.stringify({
            content: value,
            type: type,
            offset: offset,
            position: startPosition
        }));

        oldContent = editor.value;
    });

    function findExtraSubstring(str1, str2) {
        let longer = str1.length > str2.length ? str1 : str2;
        let shorter = str1.length <= str2.length ? str1 : str2;
        let delta = longer.length - shorter.length;

        let diffSubstring = "";
        let differenceFound = false;

        for (let i = 0; i < longer.length; i++) {
            if (longer[i] !== shorter[i]) {
                diffSubstring = longer.substring(i, i + delta);
                differenceFound = true;
                break;
            }
        }

        if (!differenceFound && longer.length > shorter.length) {
            // If no difference was found in the characters, it means the extra part is at the end
            diffSubstring = longer.substring(shorter.length, shorter.length + delta);
        }
        return diffSubstring
    }

    document.getElementById("saveButton").addEventListener("click", function () {
        const content = document.getElementById("editor").value;

        fetch(`/api/save/${fileId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain'
            },
            body: content
        })
            .then(response => {
                if (response.ok) {
                    return response.text();
                } else {
                    throw new Error('Error saving file');
                }
            })
            .then(data => {
                alert(data);
            })
            .catch(error => {
                console.error('Error saving file:', error);
                alert('Error saving file');
            });
    });

    // Modal logic for adding notes
    const modal = document.getElementById("noteModal");
    const addNoteButton = document.getElementById("addNoteButton");
    const closeModal = document.getElementsByClassName("close")[0];
    const submitNoteButton = document.getElementById("submitNoteButton");

    addNoteButton.onclick = function() {
        modal.style.display = "block";
    };

    closeModal.onclick = function() {
        modal.style.display = "none";
    };

    window.onclick = function(event) {
        if (event.target === modal) {
            modal.style.display = "none";
        }
    };

    submitNoteButton.onclick = function() {
        const rowNumber = document.getElementById("rowNumber").value;
        const noteContent = document.getElementById("noteContent").value;

        if (rowNumber && noteContent) {
            const noteData = {
                rowNumber: rowNumber,
                content: noteContent
            };

            fetch(`/api/note/add/${fileId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(noteData)
            })
                .then(response => {
                    if (response.ok) {
                        alert('Note added successfully!');
                        modal.style.display = "none";
                        loadNotes();
                    } else {
                        throw new Error('Error adding note');
                    }
                })
                .catch(error => {
                    console.error('Error adding note:', error);
                    alert('Error adding note');
                });
        } else {
            alert('Please fill out all fields');
        }
    };
});