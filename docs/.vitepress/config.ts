import { defineConfig } from 'vitepress'
import { groupIconVitePlugin } from 'vitepress-plugin-group-icons'
import enConfig from '../en/config'
import koConfig from '../ko/config'

export default defineConfig({
  title: 'querydsl-ktx',
  description: 'Null-safe infix Kotlin extensions for QueryDSL dynamic queries',
  base: '/querydsl-ktx/',

  head: [
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:title', content: 'querydsl-ktx' }],
    ['meta', { property: 'og:description', content: 'Null-safe infix Kotlin extensions for QueryDSL dynamic queries' }],
    ['meta', { property: 'og:url', content: 'https://harryjhin.github.io/querydsl-ktx/' }],
    ['meta', { name: 'twitter:card', content: 'summary' }],
    ['link', { rel: 'canonical', href: 'https://harryjhin.github.io/querydsl-ktx/' }],
    ['script', { type: 'application/ld+json' }, JSON.stringify({
      '@context': 'https://schema.org',
      '@type': 'SoftwareSourceCode',
      name: 'querydsl-ktx',
      description: 'Null-safe infix Kotlin extensions for QueryDSL dynamic queries',
      url: 'https://github.com/HarryJhin/querydsl-ktx',
      codeRepository: 'https://github.com/HarryJhin/querydsl-ktx',
      programmingLanguage: 'Kotlin',
      runtimePlatform: 'JVM',
      license: 'https://opensource.org/licenses/MIT',
    })],
  ],

  sitemap: {
    hostname: 'https://harryjhin.github.io/querydsl-ktx/',
  },

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
