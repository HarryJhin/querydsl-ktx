import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import './custom.css'
import 'virtual:group-icons.css'

export default {
  extends: DefaultTheme,
} satisfies Theme
