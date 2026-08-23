export type ServiceState = 'checking' | 'online' | 'offline'
export type RunStatus = 'ACCEPTED' | 'FAILED'

export interface CompileResponse {
  status: RunStatus | string
  message: string
}

export interface HealthResponse {
  status: string
}

export interface CompileRun {
  id: string
  repositoryUrl: string
  repositoryName: string
  status: RunStatus
  message: string
  createdAt: string
  durationMs: number
}
