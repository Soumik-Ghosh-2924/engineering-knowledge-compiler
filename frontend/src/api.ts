import type { AnalysisResponse, HealthResponse } from './types'

const parseResponse = async <T>(response: Response): Promise<T> => {
  const payload = await response.json().catch(() => null) as T | null
  if (!response.ok) {
    const message = payload && typeof payload === 'object' && 'message' in payload
      ? String(payload.message)
      : `Request failed with status ${response.status}`
    throw new Error(message)
  }
  if (!payload) throw new Error('The server returned an empty response.')
  return payload
}

export const getHealth = async (signal?: AbortSignal): Promise<HealthResponse> => {
  const response = await fetch('/health', { signal, headers: { Accept: 'application/json' } })
  return parseResponse<HealthResponse>(response)
}

export const analyzeRepository = async (repositoryUrl: string): Promise<AnalysisResponse> => {
  const response = await fetch('/api/v1/compiler/analyze', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify({ repositoryUrl }),
  })
  return parseResponse<AnalysisResponse>(response)
}
