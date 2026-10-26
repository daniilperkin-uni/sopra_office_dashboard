import globals from "globals";
import pluginJs from "@eslint/js";
import tseslint from "typescript-eslint";
import pluginVue from "eslint-plugin-vue";
// Formatierung uebernimmt Prettier; kollidierende Stilregeln abschalten
import eslintConfigPrettier from "eslint-config-prettier/flat";

export default [
  {
    ignores: ["dist/**", "node_modules/**", "coverage/**", "public/**"]
  },
  {
    files: ["**/*.{js,mjs,cjs,ts,vue}"]
  },
  {
    languageOptions: { 
        globals: globals.browser 
    }
  },
  {
    // Build- und Tooling-Konfiguration laeuft in Node (process, __dirname, ...).
    files: ["**/*.config.{js,mjs,cjs,ts}", "eslint.config.js"],
    languageOptions: {
        globals: globals.node
    }
  },
  {
    // Tests laufen mit Vitest (globals: true in vitest.config.js) und brauchen
    // daher describe/it/expect/vi als globale Bezeichner. Die Browser-Globals
    // aus dem Block oben bleiben gemerged erhalten (jsdom-Umgebung).
    files: ["tests/**/*.{js,ts}", "src/**/*.test.{js,ts}"],
    languageOptions: {
        globals: globals.vitest
    }
  },
  pluginJs.configs.recommended,
  ...tseslint.configs.recommended,
  ...pluginVue.configs["flat/recommended"],
  {
    files: ["**/*.vue"],
    languageOptions: {
        parserOptions: {
            parser: tseslint.parser
        }
    }
  },
  eslintConfigPrettier
];
