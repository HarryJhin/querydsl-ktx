import type { DefaultTheme, LocaleSpecificConfig } from 'vitepress'
import { version } from '../.vitepress/version'

export default {
  themeConfig: {
    nav: [
      {
        text: '시작하기',
        link: '/ko/getting-started/installation',
      },
      {
        text: '가이드',
        link: '/ko/guide/extensions',
      },
      {
        text: 'API 레퍼런스',
        link: '/api/',
        target: '_self',
      },
      {
        text: `v${version}`,
        items: [
          { text: '변경 내역', link: 'https://github.com/HarryJhin/querydsl-ktx/releases' },
          { text: '기여 가이드', link: 'https://github.com/HarryJhin/querydsl-ktx/blob/main/CONTRIBUTING.ko.md' },
        ],
      },
    ],
    sidebar: {
      '/ko/getting-started/': [
        {
          text: '시작하기',
          items: [
            { text: '설치', link: '/ko/getting-started/installation' },
            { text: '빠른 시작', link: '/ko/getting-started/quick-start' },
          ],
        },
      ],
      '/ko/guide/': [
        {
          text: '핵심',
          items: [
            { text: '확장 인터페이스', link: '/ko/guide/extensions' },
            { text: '동적 쿼리', link: '/ko/guide/dynamic-queries' },
            { text: 'Expressions', link: '/ko/guide/expressions' },
            { text: 'Case/When DSL', link: '/ko/guide/case-dsl' },
          ],
        },
        {
          text: '고급',
          items: [
            { text: '페이지네이션', link: '/ko/guide/pagination' },
            { text: 'Bulk DML', link: '/ko/guide/bulk-dml' },
            { text: '베스트 프랙티스', link: '/ko/guide/best-practices' },
          ],
        },
      ],
    },
    editLink: {
      pattern: 'https://github.com/HarryJhin/querydsl-ktx/edit/main/docs/ko/:path',
      text: '이 페이지를 GitHub에서 편집',
    },
    outline: {
      label: '이 페이지에서',
    },
    docFooter: {
      prev: '이전',
      next: '다음',
    },
    lastUpdated: {
      text: '마지막 수정일',
    },
    returnToTopLabel: '맨 위로',
    sidebarMenuLabel: '메뉴',
    darkModeSwitchLabel: '테마',
  },
} satisfies LocaleSpecificConfig<DefaultTheme.Config>
