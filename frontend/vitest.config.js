import { defineConfig, mergeConfig } from 'vitest/config'
import viteConfig from './vite.config.js'

// Reusa os plugins/aliases do vite.config.js do app (fonte única de verdade).
// Único override: ambiente de teste (jsdom) e globs de teste.
export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      environment: 'jsdom',
      include: ['src/**/*.{test,spec}.{js,ts}'],
      coverage: {
        provider: 'v8',
        reporter: ['text', 'html'],
        include: ['src/services/**', 'src/stores/**'],
        thresholds: {
          lines: 60,
        },
      },
    },
  })
)
