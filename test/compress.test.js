// @vitest-environment jsdom
import { afterAll, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';

const pdfResponse = (size, compressionApplied = 'true') => ({
  ok: true,
  headers: new Headers({
    'content-type': 'application/pdf',
    'content-disposition': 'attachment; filename="optimized.pdf"',
    'X-Compression-Applied': compressionApplied
  }),
  blob: async () => new Blob([new Uint8Array(size)], { type: 'application/pdf' })
});

describe('compression flow', () => {
  let fileInput;
  let form;
  let recompressButton;
  let downloadLink;
  let compressionMessage;

  beforeAll(async () => {
    document.body.innerHTML = `
      <form action="/compress_pdf" id="compressForm">
        <input type="file" name="file" id="file">
        <button type="submit" id="submitButton">Compress</button>
        <button type="button" id="recompressButton" hidden>Compress again</button>
        <div id="errorMessage"></div>
        <div id="compressionMessage"></div>
        <a id="downloadLink" hidden>Download optimized PDF</a>
      </form>
    `;

    fileInput = document.getElementById('file');
    form = document.getElementById('compressForm');
    recompressButton = document.getElementById('recompressButton');
    downloadLink = document.getElementById('downloadLink');
    compressionMessage = document.getElementById('compressionMessage');

    Object.defineProperty(fileInput, 'files', {
      configurable: true,
      value: [new File([new Uint8Array(10240)], 'original.pdf', { type: 'application/pdf' })]
    });
    URL.createObjectURL = vi.fn(() => 'blob:optimized');
    URL.revokeObjectURL = vi.fn();
    vi.stubGlobal('fetch', vi.fn());

    await import('../src/main/resources/static/compress.js');
    document.dispatchEvent(new Event('DOMContentLoaded'));
  });

  beforeEach(() => {
    fetch.mockReset();
    URL.createObjectURL.mockClear();
    URL.revokeObjectURL.mockClear();
    delete window.turnstile;
    fileInput.dispatchEvent(new Event('change'));
  });

  afterAll(() => {
    vi.unstubAllGlobals();
  });

  it('keeps the download link hidden before compression', () => {
    expect(downloadLink.hidden).toBe(true);
  });

  it('shows a size result and explicit download link without downloading automatically', async () => {
    fetch.mockResolvedValueOnce(pdfResponse(5120));

    form.requestSubmit();

    await vi.waitFor(() => expect(downloadLink.hidden).toBe(false));
    expect(downloadLink.href).toBe('blob:optimized');
    expect(downloadLink.download).toBe('optimized.pdf');
    expect(compressionMessage.textContent).toBe('10.0 KB → 5.0 KB (50.0% smaller).');
  });

  it('recompresses the prior result once and then hides the recompression button', async () => {
    fetch
      .mockResolvedValueOnce(pdfResponse(5120))
      .mockResolvedValueOnce(pdfResponse(4096));

    form.requestSubmit();
    await vi.waitFor(() => expect(recompressButton.hidden).toBe(false));

    recompressButton.click();
    await vi.waitFor(() => expect(fetch).toHaveBeenCalledTimes(2));

    const secondRequest = fetch.mock.calls[1][1].body;
    expect(secondRequest.get('file').size).toBe(5120);
    await vi.waitFor(() => expect(recompressButton.hidden).toBe(true));
  });

  it('requires a new CAPTCHA token after a compression request', async () => {
    const captcha = document.createElement('input');
    captcha.name = 'cf-turnstile-response';
    captcha.value = 'initial-token';
    form.appendChild(captcha);
    window.turnstile = { reset: vi.fn(() => { captcha.value = ''; }) };
    fetch.mockResolvedValueOnce(pdfResponse(5120));

    form.requestSubmit();
    await vi.waitFor(() => expect(window.turnstile.reset).toHaveBeenCalledOnce());

    recompressButton.click();

    expect(fetch).toHaveBeenCalledOnce();
    expect(document.getElementById('errorMessage').textContent)
      .toBe('Please complete the CAPTCHA before compressing your PDF.');
  });
});
