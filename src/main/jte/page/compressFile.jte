@param Boolean captchaEnabled
@param String siteKey

@template.layout.main(content = @`
    <h1>Compress your PDFs Securely and Free of Charge</h1>
    <p>If you have been searching the web for a tool where you can compress a PDF file
        without having to be worried about the privacy of your documents, you have come to the right place.
    <p><b>THIS WEBSITE DOES NOT STORE ANY OF THE FILES THAT YOU UPLOAD!</b> It compresses them on the fly and returns
        the compressed file to you. More information in <a href="/about">About</a></p>

    @if(captchaEnabled)
        <script src="https://challenges.cloudflare.com/turnstile/v0/api.js" async defer></script>
    @endif

    <form action="/compress_pdf" id="compressForm" method="post" enctype="multipart/form-data">
        <div id="upload-sections">
            <div class="upload-section">
                <label for="file">Upload PDF:</label>
                <input type="file" name="file" id="file" accept="application/pdf" required>
            </div>
            <div class="upload-section">
                <label for="compressionLevel">Compression level:</label>
                <select name="compressionLevel" id="compressionLevel">
                    <option value="LIGHT">Light — best quality</option>
                    <option value="BALANCED" selected>Balanced — recommended</option>
                    <option value="STRONG">Strong — smallest file</option>
                </select>
            </div>
        </div>
        <br><br>
        @if(captchaEnabled)
            <div class="cf-turnstile"
                 data-sitekey="${siteKey}">
            </div>
        @endif
        <br>
        <button type="submit" id="submitButton" class="vince-event-name=Compress">Compress</button>
        <button type="button" id="recompressButton" hidden>Compress again (may reduce quality)</button>
        <div id="errorMessage" class="error-message"></div>
        <div id="compressionMessage" class="compression-message" role="status" aria-live="polite"></div>
        <a id="downloadLink" hidden>Download optimized PDF</a>
    </form>

    <style>
        .upload-section { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
        .compression-message { color: green; margin-top: 10px; }
        #downloadLink { display: inline-block; margin-top: 10px; }
        #downloadLink[hidden] { display: none; }
    </style>
    <script src="compress.js"></script>
`)
