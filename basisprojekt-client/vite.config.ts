import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

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
  plugins: [react()],
});
