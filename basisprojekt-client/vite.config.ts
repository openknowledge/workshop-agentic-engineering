/// <reference types="vitest/config" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import istanbul from 'vite-plugin-istanbul';

// https://vite.dev/config/
export default defineConfig({
  // tag::proxy-config[]
  server: {
    host: true,
    proxy: {
      '/api': {
        target: process.env.VITE_BACKEND_ENDPOINT || 'http://127.0.0.1:8080',
        changeOrigin: true,
        secure: false,
      },
    },
    cors: {
      origin: ['http://127.0.0.1:8080/*'],
    },
  },
  // end::proxy-config[]
  // tag::istanbul-plugin[]
  plugins: [
    react(),
    ...(process.env.VITE_COVERAGE
      ? [
          istanbul({
            include: 'src/**',
            exclude: ['node_modules', 'tests/', 'src/main.tsx'],
            extension: ['.ts', '.tsx'],
            requireEnv: false,
          }),
        ]
      : []),
  ],
  // end::istanbul-plugin[]
<<<<<<< HEAD
=======
  build: {
    sourcemap: process.env.VITE_COVERAGE ? 'hidden' : false,
    rolldownOptions: {
      output: {
        codeSplitting: {
          groups: [
            { name: 'react', test: /node_modules[\\/](react|react-dom|scheduler)[\\/]/ },
            { name: 'tanstack', test: /node_modules[\\/]@tanstack[\\/]/ },
          ],
        },
      },
    },
  },
>>>>>>> 8740e8a (Initial setup)
  // tag::vitest-config[]
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './tests/setup.ts',
    include: ['tests/**/*.test.{ts,tsx}'],
    css: false,
  },
  // end::vitest-config[]
});
