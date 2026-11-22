import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const proxy = { '/api/seguimiento': 'http://localhost:8081' };

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy
  },
  preview: { proxy }
});
