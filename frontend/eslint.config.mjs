import { defineConfig, globalIgnores } from "eslint/config";
import js from "@eslint/js";
import globals from "globals";
import react from "eslint-plugin-react";
import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";

export default defineConfig([
  globalIgnores(["dist/", "build/", "coverage/", "node_modules/"]),

  js.configs.recommended,

  {
    files: ["**/*.{js,jsx,mjs,cjs}"],

    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "module",
      parserOptions: {
        ecmaFeatures: {
          jsx: true,
        },
      },
      globals: {
        ...globals.browser,
        ...globals.node,
      },
    },

    plugins: {
      react,
      "react-hooks": reactHooks,
      "react-refresh": reactRefresh,
    },

    settings: {
      react: {
        version: "19.2",
      },
    },

    rules: {
      ...react.configs.recommended.rules,
      ...react.configs["jsx-runtime"].rules,
      ...reactHooks.configs.recommended.rules,

      "react-refresh/only-export-components": [
        "warn",
        { allowConstantExport: true },
      ],

      "react/prop-types": "off",
      "react-hooks/set-state-in-effect": "off",

      "no-unused-vars": [
        "warn",
        {
          argsIgnorePattern: "^_",
          varsIgnorePattern: "^_",
        },
      ],
    },
  },

  {
    files: ["**/*.{test,spec}.{js,jsx}", "**/__tests__/**/*.{js,jsx}"],
    languageOptions: {
      globals: {
        ...globals.jest,
      },
    },
  },
]);
