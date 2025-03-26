import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  preview: {
    port: 3000,
  },

  server: {
    port: 3000, // default 5173
  },
  build: {
    outDir: "build",
  },
  plugins: [react()],
  esbuild: {},
  base: "./",
});
