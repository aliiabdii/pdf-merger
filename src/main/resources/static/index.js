// Add and remove upload file buttons
document.addEventListener('DOMContentLoaded', () => {
  const uploadSections = document.getElementById('upload-sections');
  const addMoreButton = document.getElementById('add-more');
  let fileCount = uploadSections.querySelectorAll('.upload-section').length;

  function createSection(idx) {
    const newSection = document.createElement('div');
    newSection.classList.add('upload-section');
    newSection.innerHTML =
      '<label for="file' + idx + '">Upload PDF ' + idx + ':</label>' +
      '<input type="file" name="files" id="file' + idx + '" accept="application/pdf" required>';
    return newSection;
  }

  function addRemoveButton(section){
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'remove-section';
    btn.setAttribute('aria-label','Remove file');
    btn.textContent = '×';
    section.appendChild(btn);
  }

  function addSection() {
    if (fileCount < maxFiles) {
      fileCount++;
      const section = createSection(fileCount);
      addRemoveButton(section);
      uploadSections.appendChild(section);
    }
  }

  // Ensure at least two sections on load
  while (fileCount < 2) {
    addSection();
  }

  // Add remove button to any pre-existing sections (if any)
  uploadSections.querySelectorAll('.upload-section').forEach((sec) => {
    if (!sec.querySelector('.remove-section')) addRemoveButton(sec);
  });

  addMoreButton.addEventListener('click', () => {
    if (fileCount < maxFiles) {
      addSection();
    } else {
      alert('You can upload up to ' + maxFiles + ' files only.');
    }
  });

  // Event delegation for remove buttons
  uploadSections.addEventListener('click', (e) => {
    if (!e.target.classList.contains('remove-section')) return;
    const section = e.target.closest('.upload-section');
    if (!section) return;
    if (uploadSections.children.length <= 2) {
      alert('At least 2 files are required.');
      return;
    }
    section.remove();
    renumberSections();
  });

  function renumberSections(){
    const sections = uploadSections.querySelectorAll('.upload-section');
    sections.forEach((sec, i) => {
      const idx = i + 1;
      const label = sec.querySelector('label');
      const input = sec.querySelector('input[type="file"]');
      if (label) {
        label.setAttribute('for', 'file' + idx);
        label.textContent = 'Upload PDF ' + idx + ':';
      }
      if (input) input.id = 'file' + idx;
    });
    fileCount = sections.length;
  }
});


const form = document.getElementById('mergeForm');
const submitButton = document.getElementById('submitButton');
const errorMessageDiv = document.getElementById('errorMessage');

form.addEventListener('submit', function (event) {
    event.preventDefault();

    if (!checkSize()) {
        errorMessageDiv.textContent = "Total size of the files should be maximum " + maxSize + "MB!";
        return;
    }

    errorMessageDiv.textContent = '';

    submitButton.disabled = true;
    submitButton.classList.add('loading');
    submitButton.textContent = 'Submitting...';

    const formData = new FormData(form);

    fetch('/merge', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (response.ok) {
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/pdf')) {
                return response.blob();
            } else {
                throw new Error('PDF file was generated but could not be fetched. Try again!');
            }
        } else {
            return response.text().then(rawError => {
                try {
                    throw JSON.parse(text);
                } catch (e) {
                    throw new Error(rawError);
                }
            });
        }
    })
    .then(blob => {
        // If we get a blob, create a temporary link to download it
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        // TODO: get this from Content-Disposition header if available
        a.download = 'merged.pdf';
        document.body.appendChild(a);
        a.click(); // Simulate a click to start the download
        window.URL.revokeObjectURL(url); // Clean up the temporary URL

        // Re-enable the button after successful download initiation
        trackAndReset('');
    })
    .catch(error => {
        trackAndReset(error.message);
    });

    function trackAndReset(error) {
        let e = {
            props: {
                with_error: !!error
            },
            callback: function() {
                resetSubmitButton(error);
            }
        }
        plausible('Merge', e);
    }

    function resetSubmitButton(error) {
        errorMessageDiv.textContent = error
        submitButton.disabled = false;
        submitButton.classList.remove('loading');
        submitButton.textContent = 'Submit';
    }

    function checkSize() {
        const MAX_SIZE_BYTES = maxSize * 1024 * 1024;
        const fileInputs = document.querySelectorAll("input[name='files']");

        let totalSize = 0;

        fileInputs.forEach(input => {
            if (input.files) {
                for (let i = 0; i < input.files.length; i++) {
                    totalSize += input.files[i].size;
                }
            }
        });

        return totalSize <= MAX_SIZE_BYTES;
    }
});