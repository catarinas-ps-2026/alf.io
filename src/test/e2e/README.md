# Alf.io Playwright E2E Tests

This directory contains the Playwright End-to-End (E2E) tests for the `alf.io` project.

## Directory Structure
* `tests/`: Contains test suites (e.g., `smoke.spec.ts`).
* `fixtures/`: Playwright fixtures that automate the lifecycle of testing resources.
* `helpers/`: Utility files (e.g., `api-helper.ts` for programmatically creating/deleting test events).

---

## Prerequisites (NixOS)
This project is configured with a Nix flake. The `flake.nix` automatically sets:
* `PLAYWRIGHT_BROWSERS_PATH`: points to Nixpkgs' patched browser binaries.
* `PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS=true`: skips validation of host packages that aren't globally installed in NixOS.

To ensure compatibility, the `@playwright/test` version in `package.json` is locked to match the Nixpkgs `playwright-driver` version (currently `1.59.1`).

---

## How to Install and Run

### 1. Install Dependencies
Ensure you are inside the `src/test/e2e` directory and run:
```bash
pnpm install
```
*(No need to run `pnpm exec playwright install` on NixOS, as the patched browsers are already provided by the Nix environment).*

### 2. Configure Environment Variables
To run tests that interact with the Admin API (like the dynamic event creation test), set the following environment variables:
```bash
export E2E_SERVER_APIKEY="your-admin-api-key"
export PLAYWRIGHT_BASE_URL="http://localhost:8080" # defaults to http://localhost:8080
```

### 3. Run Tests
Start your local `alf.io` instance, then run:
```bash
# Run all tests headlessly (Chromium & Firefox)
pnpm test

# Run tests in headed mode
pnpm run test:headed

# Run tests in Playwright's interactive UI mode
pnpm run test:ui

# Run tests in debug mode
pnpm run test:debug
```
