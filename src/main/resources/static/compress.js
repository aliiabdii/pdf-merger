document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('compressForm');
  const fileInput = document.getElementById('file');
  const submitButton = document.getElementById('submitButton');
  const recompressButton = document.getElementById('recompressButton');
  const errorMessage = document.getElementById('errorMessage');
  const compressionMessage = document.getElementById('compressionMessage');
  const downloadLink = document.getElementById('downloadLink');
  let sourceBlob;
  let sourceFilename;
  let compressionPasses = 0;
  let downloadUrl;

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    if (compressionPasses === 0) submitCompression();
  });

  recompressButton.addEventListener('click', () => {
    submitCompression(sourceBlob, sourceFilename);
  });

  fileInput.addEventListener('change', resetResults);

  async function submitCompression(blob, filename) {
    if (!hasFreshCaptchaToken()) {
      errorMessage.textContent = 'Please complete the CAPTCHA before compressing your PDF.';
      return;
    }

    errorMessage.textContent = '';
    compressionMessage.textContent = '';
    setSubmitting(true);
    let requestSent = false;

    try {
      const formData = new FormData(form);
      if (blob) formData.set('file', blob, filename);

      requestSent = true;
      const response = await fetch(form.action, {
        method: 'POST',
        body: formData
      });

      if (!response.ok) {
        throw new Error(await getErrorMessage(response));
      }

      if (!response.headers.get('content-type')?.includes('application/pdf')) {
        throw new Error('The optimized PDF could not be downloaded. Please try again.');
      }

      const compressionApplied = response.headers.get('X-Compression-Applied');
      const resultBlob = await response.blob();
      const originalSize = blob ? blob.size : fileInput.files[0].size;
      sourceBlob = resultBlob;
      sourceFilename = getDownloadName(response);
      compressionPasses++;
      showResult(originalSize, resultBlob.size, compressionApplied);

      if (typeof plausible === 'function') {
        plausible('Compress', {
          props: { compression_applied: compressionApplied === 'true' }
        });
      }
    } catch (error) {
      errorMessage.textContent = error.message;
    } finally {
      if (requestSent) resetTurnstile();
      setSubmitting(false);
    }
  }

  async function getErrorMessage(response) {
    const rawError = await response.text();

    try {
      return JSON.parse(rawError).message || rawError;
    } catch {
      return rawError || 'The PDF could not be compressed. Please try again.';
    }
  }

  function hasFreshCaptchaToken() {
    const captchaInput = form.querySelector('[name="cf-turnstile-response"]');
    return !captchaInput || captchaInput.value;
  }

  function getDownloadName(response) {
    const contentDisposition = response.headers.get('content-disposition');
    const filename = contentDisposition?.match(/filename="?([^";]+)"?/i)?.[1];
    return filename || 'optimized.pdf';
  }

  function showResult(originalSize, optimizedSize, compressionApplied) {
    if (downloadUrl) URL.revokeObjectURL(downloadUrl);
    downloadUrl = URL.createObjectURL(sourceBlob);
    downloadLink.href = downloadUrl;
    downloadLink.download = sourceFilename;
    downloadLink.hidden = false;
    compressionMessage.textContent = getCompressionMessage(
      originalSize,
      optimizedSize,
      compressionApplied
    );

    submitButton.hidden = true;
    recompressButton.hidden = compressionPasses >= 2;
  }

  function getCompressionMessage(originalSize, optimizedSize, compressionApplied) {
    if (compressionApplied === 'true') {
      const reduction = ((1 - optimizedSize / originalSize) * 100).toFixed(1);
      return `${formatSize(originalSize)} → ${formatSize(optimizedSize)} (${reduction}% smaller).`;
    }

    if (compressionApplied === 'false') {
      return `No smaller version was produced; ${formatSize(optimizedSize)} was retained unchanged.`;
    }

    return `${formatSize(originalSize)} → ${formatSize(optimizedSize)}.`;
  }

  function formatSize(bytes) {
    return `${(bytes / 1024).toFixed(1)} KB`;
  }

  function setSubmitting(isSubmitting) {
    submitButton.disabled = isSubmitting;
    recompressButton.disabled = isSubmitting;
    submitButton.classList.toggle('loading', isSubmitting);
    recompressButton.classList.toggle('loading', isSubmitting);
    submitButton.textContent = isSubmitting ? 'Compressing...' : 'Compress';
    recompressButton.textContent = isSubmitting
      ? 'Compressing...'
      : 'Compress again (may reduce quality)';
  }

  function resetTurnstile() {
    if (window.turnstile) window.turnstile.reset();
  }

  function resetResults() {
    sourceBlob = undefined;
    sourceFilename = undefined;
    compressionPasses = 0;
    compressionMessage.textContent = '';
    errorMessage.textContent = '';
    recompressButton.hidden = true;
    submitButton.hidden = false;
    downloadLink.hidden = true;
    downloadLink.removeAttribute('href');
    downloadLink.removeAttribute('download');
    if (downloadUrl) URL.revokeObjectURL(downloadUrl);
    downloadUrl = undefined;
  }
});
