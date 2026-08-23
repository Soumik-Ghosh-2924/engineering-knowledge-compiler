import type { CompileRun } from './types'

const KEY = 'ekc.compile-history.v1'
const LIMIT = 20

export const loadRuns = (): CompileRun[] => {
  try {
    const value = JSON.parse(window.localStorage.getItem(KEY) ?? '[]')
    return Array.isArray(value) ? value.slice(0, LIMIT) : []
  } catch {
    return []
  }
}

export const saveRuns = (runs: CompileRun[]) => {
  window.localStorage.setItem(KEY, JSON.stringify(runs.slice(0, LIMIT)))
}

export const clearRuns = () => window.localStorage.removeItem(KEY)
