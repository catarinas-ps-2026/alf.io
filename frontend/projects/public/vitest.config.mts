/// <reference types="vitest" />
import { defineConfig } from "vitest/config";
import angular from "@analogjs/vite-plugin-angular";

export default defineConfig(({ mode }) => ({
  plugins: [angular()],
  test: {
    globals: true,
    setupFiles: ["src/test-setup.ts"],
    environment: "jsdom",
    environmentOptions: {
      jsdom: {
        url: "http://localhost",
      },
    },
    include: ["src/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}"],
    coverage: {
      provider: "v8",
      reporter: ["text", "json", "html"],
      exclude: [
        "src/app/challenge/**",
        "src/app/shared/ticket.service.ts",
        "src/app/shared/i18n.service.ts",
        "src/app/shared/analytics.service.ts",
        "src/app/shared/validation-helper.ts",
        "src/app/shared/event.service.ts",
        "src/app/payment/stripe-payment-proxy/**",
      ],
    },
  },
  resolve: {
    tsconfigPaths: true,
  },
  define: {
    "import.meta.vitest": mode !== "production",
  },
}));
