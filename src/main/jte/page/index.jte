@param Boolean captchaEnabled
@param String siteKey

@template.layout.main(content = @`
    <h1>Merge your PDFs Securely and Free of Charge</h1>
    <p>If you have been searching the web for a tool where you can just merge a bunch of PDF files into one
        without having to be worried about the privacy of your documents, you have come to the right place.
    <p>Here you can merge up to 5 PDFs, free of charge!</p>
    <p><b>THIS WEBSITE DOES NOT STORE ANY OF THE FILES THAT YOU UPLOAD!</b> It just merges them on fly and returns
        the single merged file back to you.</p>

    @if(captchaEnabled)
        <script src="https://challenges.cloudflare.com/turnstile/v0/api.js" async defer></script>
    @endif

    <form action="/merge" method="post" enctype="multipart/form-data">
        <div id="upload-sections">

        </div>
        <button type="button" id="add-more">Add More Files</button>
        <br><br>
        @if(captchaEnabled)
            <div class="cf-turnstile"
                 data-sitekey="${siteKey}">
            </div>
        @endif
        <br>
        <button type="submit">Merge</button>
    </form>

    <style>
        .remove-section{
            background: transparent;
            color: #e74c3c;
            border: none;
            border-radius: 0;
            padding: 0 6px;
            font-size: 26px;
            font-weight: bold;
            line-height: 1;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            justify-content: center;
        }
        .upload-section { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
    </style>

    <script>
        document.addEventListener('DOMContentLoaded', () => {
          const uploadSections = document.getElementById('upload-sections');
          const addMoreButton = document.getElementById('add-more');
          const maxFiles = 5;
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
    </script>
`)
