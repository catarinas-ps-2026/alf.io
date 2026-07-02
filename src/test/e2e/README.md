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
* `PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS=true`: skips validation of host packages that are not globally installed in NixOS.

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
To run tests, set the following environment variables:

```bash
# Required for event creation tests
export E2E_SERVER_APIKEY="your-system-api-key"

# Required for authentication tests (all have defaults when running in dev mode)
export E2E_ADMIN_USERNAME="admin"        # default: admin
export E2E_ADMIN_PASSWORD="abcd"         # default: abcd (dev mode only)
export E2E_OWNER_USERNAME="owner-e2e"    # default: owner-e2e
export E2E_OWNER_PASSWORD="abcd"         # default: abcd
export E2E_SUPERVISOR_USERNAME="supervisor-e2e"  # default: supervisor-e2e
export E2E_SUPERVISOR_PASSWORD="abcd"    # default: abcd

# Optional
export PLAYWRIGHT_BASE_URL="http://localhost:8080"  # default: http://localhost:8080
```

**Note:** When running with the `dev` Spring profile (the default for local development), the admin password is always `"abcd"` regardless of what was previously set. The owner and supervisor users are created automatically by the test fixtures and cleaned up after each test run.

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
