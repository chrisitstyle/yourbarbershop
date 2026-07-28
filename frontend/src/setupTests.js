/**
 * This file is automatically executed by Vitest before running any tests.
 * * It imports custom DOM matchers from '@testing-library/jest-dom',
 * extending the standard expect() assertions. This allows us to use
 * convenient, user-centric DOM assertions like .toBeInTheDocument(),
 * .toHaveTextContent(), or .toBeDisabled() globally, without needing
 * to import them manually in every single test file.
 */
import "@testing-library/jest-dom";
