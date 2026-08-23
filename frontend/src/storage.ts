import type { AnalysisRun } from './types'

const KEY = 'ekc.analysis-history.v1'
const LEGACY_KEY = 'ekc.compile-history.v1'
const LIMIT = 20

export const loadRuns = (): AnalysisRun[] => {
  try {
    const stored = window.localStorage.getItem(KEY) ?? window.localStorage.getItem(LEGACY_KEY) ?? '[]'
    const value = JSON.parse(stored)
    if (!Array.isArray(value)) return []
    return value.slice(0, LIMIT).map((run) => ({
      ...run,
      status: run.status === 'ACCEPTED' ? 'ANALYZED' : run.status,
    })) as AnalysisRun[]
  } catch {
    return []
  }
}

export const saveRuns = (runs: AnalysisRun[]) => {
  window.localStorage.setItem(KEY, JSON.stringify(runs.slice(0, LIMIT)))
}

export const clearRuns = () => {
  window.localStorage.removeItem(KEY)
  window.localStorage.removeItem(LEGACY_KEY)
}
