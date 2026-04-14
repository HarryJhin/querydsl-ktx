import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

export const version = readFileSync(resolve(__dirname, '../../gradle.properties'), 'utf-8')
  .match(/version=(.*)/)?.[1]?.trim() ?? '0.0.0'
