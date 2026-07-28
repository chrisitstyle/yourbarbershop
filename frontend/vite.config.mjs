/// <reference types="vitest" />
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  // preview server configuration
  preview: {
    port: 3000,
  },

  // development server configuration
  server: {
    port: 3000, // override default vite port (5173) to 3000
  },

  build: {
    outDir: "build",
    chunkSizeWarningLimit: 1000, // increase chunk warning limit to 1000 kb to prevent false alarms
    rollupOptions: {
      output: {
        // rolldown / vite 8 requires manualchunks to be a function for splitting vendor chunks
        manualChunks(id) {
          if (id.includes("node_modules")) {
            if (
              id.includes("react-router-dom") ||
              id.includes("react-dom") ||
              id.includes("react")
            ) {
              return "react";
            }
            if (
              id.includes("react-bootstrap") ||
              id.includes("bootstrap") ||
              id.includes("lucide-react")
            ) {
              return "ui";
            }
            if (id.includes("@fortawesome")) {
              return "fontawesome";
            }
            if (id.includes("@supabase")) {
              return "supabase";
            }
          }
        },
      },
    },
  },

  // vitest testing configuration
  test: {
    globals: true, // use describe, it, expect without importing them
    environment: "jsdom", // simulate browser environment
    setupFiles: "./src/setupTests.js", // path to the setup file
    css: true, // enable css parsing for visibility assertions
  },

  plugins: [react()],
  // use relative base path for deployment flexibility
  base: "./",
});
