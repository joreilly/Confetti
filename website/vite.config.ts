import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Everything in public/ is copied verbatim to dist/. Pages that need bundling are
// listed as inputs below and land at the same relative path in dist/.
export default defineConfig({
  plugins: [react()],
  optimizeDeps: {
    // Its `?worker` imports only resolve through Vite's own pipeline, not the dev pre-bundler.
    exclude: ['graphiql/setup-workers/vite'],
  },
  build: {
    // Keep hashed bundles away from the hand-written public/assets/ directory.
    assetsDir: '_bundle',
    // GraphiQL pulls in Monaco, which is big no matter what.
    chunkSizeWarningLimit: 5000,
    rolldownOptions: {
      input: {
        graphiql: 'graphiql/index.html',
      },
      onLog(level, log, handler) {
        // graphiql ships React Compiler "use no memo" directives that are irrelevant once bundled.
        if (log.code === 'MODULE_LEVEL_DIRECTIVE') return;
        handler(level, log);
      },
    },
  },
});
