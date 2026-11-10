import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vite.dev/config/
export default defineConfig({
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
  plugins: [react()],
  build: {
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
});
