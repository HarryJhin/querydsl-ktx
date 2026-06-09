import { defineConfig } from 'vitepress'
import { groupIconVitePlugin } from 'vitepress-plugin-group-icons'
import { version } from './version'
import enConfig from '../en/config'
import koConfig from '../ko/config'

export default defineConfig({
  title: 'querydsl-ktx',
  description: 'Null-safe infix Kotlin extensions for QueryDSL dynamic queries',
  base: '/querydsl-ktx/',

  head: [
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:image', content: 'https://harryjhin.github.io/querydsl-ktx/og-image.png' }],
    ['meta', { property: 'og:image:width', content: '1200' }],
    ['meta', { property: 'og:image:height', content: '630' }],
    ['meta', { name: 'twitter:card', content: 'summary_large_image' }],
    ['meta', { name: 'twitter:image', content: 'https://harryjhin.github.io/querydsl-ktx/og-image.png' }],
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

  // 페이지별 canonical / og 태그. head 배열은 전역 정적이라 모든 페이지가
  // 같은 값을 갖는다. rewrites(en/ -> root)를 반영해 선행 en/ 를 제거한다.
  transformPageData(pageData) {
    const siteBase = 'https://harryjhin.github.io/querydsl-ktx/'
    const route = pageData.relativePath
      .replace(/^en\//, '')
      .replace(/(^|\/)index\.md$/, '$1')
      .replace(/\.md$/, '.html')
    const url = siteBase + route

    const isHome = pageData.frontmatter.layout === 'home'
    const title = isHome ? 'querydsl-ktx' : `${pageData.title} | querydsl-ktx`
    const description = pageData.description
      || pageData.frontmatter.description
      || 'Null-safe infix Kotlin extensions for QueryDSL dynamic queries'

    pageData.frontmatter.head ??= []
    pageData.frontmatter.head.push(
      ['link', { rel: 'canonical', href: url }],
      ['meta', { property: 'og:url', content: url }],
      ['meta', { property: 'og:title', content: title }],
      ['meta', { property: 'og:description', content: description }],
    )
  },

  sitemap: {
    hostname: 'https://harryjhin.github.io/querydsl-ktx/',
  },

  srcExclude: [
    'superpowers/**',
    'CLAUDE.md',
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
    config: (md) => {
      const defaultRender = md.render.bind(md)
      md.render = (src: string, env: any) => {
        return defaultRender(src.replaceAll('{{ version }}', version), env)
      }
    },
  },

  vite: {
    plugins: [
      groupIconVitePlugin(),
    ],
  },
})
