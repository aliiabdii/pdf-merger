# PDF Merger & Compressor

A Kotlin/Spring web application for merging PDF files and reducing the size of image-heavy PDFs. The live site is available at [pdf-merger.app](https://www.pdf-merger.app).

## Features

- Merge multiple PDFs into one document.
- Compress a single PDF using one of three presets:
  - **Light** — best quality (`0.85` JPEG quality, images capped at 2400 px wide).
  - **Balanced** — recommended default (`0.60` JPEG quality, images capped at 1600 px wide).
  - **Strong** — smallest files (`0.40` JPEG quality, images capped at 1200 px wide).
- Optionally protect file-processing requests with Cloudflare Turnstile.

Compression is most effective for scanned documents and PDFs containing large embedded images. PDFs primarily made of text, vector graphics, or already optimized assets may not become smaller.

## Privacy and file handling

The application does not intentionally save uploaded or generated PDFs to application storage. PDFBox processes the document in memory and the result is returned in the HTTP response.

This is not an absolute infrastructure guarantee: servlet multipart handling may use temporary files, and reverse proxies, operating systems, logs, backups, or crash dumps can have their own retention policies. When Turnstile is enabled, the CAPTCHA token and client IP are sent to Cloudflare for verification; PDF contents are NOT sent to Cloudflare by this application.

## Requirements

- JDK 21
- Node.js 18+ for frontend tests
- Docker and Docker Compose (optional)

The Gradle wrapper is included, so a separate Gradle installation is not required.

## Run locally

```bash
./gradlew bootRun
```

Open <http://localhost:8080>.

The default local configuration disables Turnstile and allows requests up to 20 MB.

## Run with Docker Compose

```bash
export TURNSTILE_SITE_KEY=your-site-key
export TURNSTILE_SECRET_KEY=your-secret-key
export VINCE_ADMIN_NAME=admin
export VINCE_ADMIN_PASSWORD=change-me
export VINCE_DOMAINS=localhost
docker compose up --build
```

The Compose configuration enables the `cloud` Spring profile, which enables Turnstile. The application is exposed on port `8080`; the optional Vince analytics service is exposed on port `8085`.

## Configuration

Spring Boot environment-variable binding can override these settings:

| Property | Environment variable | Default | Purpose |
| --- | --- | --- | --- |
| `service.max-files` | `SERVICE_MAX_FILES` | `10` | Maximum number of PDFs accepted by the merge endpoint. |
| `spring.servlet.multipart.max-file-size` | `SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE` | `20MB` | Maximum size of an individual multipart file. |
| `spring.servlet.multipart.max-request-size` | `SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE` | `20MB` | Maximum total multipart request size. |
| `turnstile.enabled` | `TURNSTILE_ENABLED` | `false` locally, `true` with `cloud` profile | Enables CAPTCHA validation. |
| `turnstile.siteKey` | `TURNSTILE_SITE_KEY` | `dummy-site-key` | Cloudflare Turnstile site key. |
| `turnstile.secretKey` | `TURNSTILE_SECRET_KEY` | `dummy-secret` | Cloudflare Turnstile secret key. |

## Test

Run backend and integration tests:

```bash
./gradlew test
```

Run frontend DOM tests:

```bash
npm ci
npm run test:js
```

The frontend tests use Vitest and JSDOM; no browser runner is required.

## Endpoints

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/` | Merge page. |
| `POST` | `/merge` | Merges multipart `files` into a PDF download. |
| `GET` | `/compress` | Compression page. |
| `POST` | `/compress_pdf` | Compresses multipart `file` using `LIGHT`, `BALANCED`, or `STRONG` `compressionLevel`. |
| `GET` | `/actuator/health` | Returns `OK` for container health checks. |
