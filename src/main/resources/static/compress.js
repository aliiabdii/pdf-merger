document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('compressForm');
  const submitButton = document.getElementById('submitButton');
  const errorMessage = document.getElementById('errorMessage');
  const compressionMessage = document.getElementById('compressionMessage');

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    errorMessage.textContent = '';
    compressionMessage.textContent = '';
    submitButton.disabled = true;
    submitButton.classList.add('loading');
    submitButton.textContent = 'Compressing...';

    try {
      const response = await fetch(form.action, {
        method: 'POST',
        body: new FormData(form)
      });

      if (!response.ok) {
        throw new Error(await getErrorMessage(response));
      }

      if (!response.headers.get('content-type')?.includes('application/pdf')) {
        throw new Error('The optimized PDF could not be downloaded. Please try again.');
      }

      const compressionApplied = response.headers.get('X-Compression-Applied');
      const blob = await response.blob();
      downloadPdf(blob, getDownloadName(response));
      compressionMessage.textContent = getCompressionMessage(compressionApplied);

      if (typeof plausible === 'function') {
        plausible('Compress', {
          props: { compression_applied: compressionApplied === 'true' }
        });
      }
    } catch (error) {
      errorMessage.textContent = error.message;
    } finally {
      submitButton.disabled = false;
      submitButton.classList.remove('loading');
      submitButton.textContent = 'Compress';
    }
  });

  async function getErrorMessage(response) {
    const rawError = await response.text();

    try {
      return JSON.parse(rawError).message || rawError;
    } catch {
      return rawError || 'The PDF could not be compressed. Please try again.';
    }
  }

  function getDownloadName(response) {
    const contentDisposition = response.headers.get('content-disposition');
    const filename = contentDisposition?.match(/filename="?([^";]+)"?/i)?.[1];
    return filename || 'optimized.pdf';
  }

  function downloadPdf(blob, filename) {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
  }

  function getCompressionMessage(compressionApplied) {
    if (compressionApplied === 'true') {
      return 'Your PDF was compressed and downloaded.';
    }

    if (compressionApplied === 'false') {
      return 'The selected level did not meaningfully reduce this PDF\'s size, so the original was downloaded unchanged. Please try a different compression level.';
    }

    return 'Your PDF was downloaded.';
  }
});
