@param Boolean captchaEnabled
@param String siteKey
@param Integer maxFiles
@param Integer maxSize

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

    <form action="/merge" id="mergeForm" method="post" enctype="multipart/form-data">
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
        <button type="submit" id="submitButton" class="use">Merge</button>
        <div id="errorMessage" class="error-message"></div>
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

    <script>const maxFiles = ${maxFiles}; const maxSize = ${maxSize};</script>
    <script src="index.js"></script>
`)
