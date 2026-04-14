import { defineConfig } from 'vitepress'
import { groupIconVitePlugin } from 'vitepress-plugin-group-icons'
import enConfig from '../en/config'
import koConfig from '../ko/config'

export default defineConfig({
  title: 'querydsl-ktx',
  description: 'Null-safe infix Kotlin extensions for QueryDSL dynamic queries',
  base: '/querydsl-ktx/',

  srcExclude: [
    'superpowers/**',
  ],

  rewrites: {
    'en/:rest*': ':rest*',
  },

  locales: {
    root: {
      label: 'English',
      lang: 'en-US',
      ...enConfig,
    },
    ko: {
      label: '한국어',
      lang: 'ko-KR',
      ...koConfig,
    },
  },

  themeConfig: {
    search: {
      provider: 'local',
    },
    socialLinks: [
      { icon: 'github', link: 'https://github.com/HarryJhin/querydsl-ktx' },
    ],
  },

  markdown: {
    theme: {
      light: 'github-light',
      dark: 'github-dark',
    },
    lineNumbers: true,
  },

  vite: {
    plugins: [
      groupIconVitePlugin(),
    ],
  },
})
