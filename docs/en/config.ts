import type { DefaultTheme, LocaleSpecificConfig } from 'vitepress'
import { version } from '../.vitepress/version'

export default {
  themeConfig: {
    nav: [
      {
        text: 'Getting Started',
        link: '/getting-started/installation',
      },
      {
        text: 'Guide',
        link: '/guide/extensions',
      },
      {
        text: 'Why',
        link: '/why',
      },
      {
        text: 'API Reference',
        link: '/api/',
        target: '_self',
      },
      {
        text: `v${version}`,
        items: [
          { text: 'Changelog', link: 'https://github.com/HarryJhin/querydsl-ktx/releases' },
          { text: 'Contributing', link: 'https://github.com/HarryJhin/querydsl-ktx/blob/main/CONTRIBUTING.md' },
        ],
      },
    ],
    sidebar: {
      '/getting-started/': [
        {
          text: 'Getting Started',
          items: [
            { text: 'Installation', link: '/getting-started/installation' },
            { text: 'Quick Start', link: '/getting-started/quick-start' },
          ],
        },
      ],
      '/guide/': [
        {
          text: 'Core',
          items: [
            { text: 'Extensions', link: '/guide/extensions' },
            { text: 'Dynamic Queries', link: '/guide/dynamic-queries' },
            { text: 'Expressions', link: '/guide/expressions' },
            { text: 'Case DSL', link: '/guide/case-dsl' },
          ],
        },
        {
          text: 'Advanced',
          items: [
            { text: 'Pagination', link: '/guide/pagination' },
            { text: 'Bulk DML', link: '/guide/bulk-dml' },
            { text: 'Best Practices', link: '/guide/best-practices' },
          ],
        },
      ],
    },
    editLink: {
      pattern: 'https://github.com/HarryJhin/querydsl-ktx/edit/main/docs/en/:path',
      text: 'Edit this page on GitHub',
    },
  },
} satisfies LocaleSpecificConfig<DefaultTheme.Config>
