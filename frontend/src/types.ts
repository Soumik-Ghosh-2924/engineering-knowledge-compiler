export type ServiceState = 'checking' | 'online' | 'offline'
export type RunStatus = 'ANALYZED' | 'FAILED'

export interface AnalysisDiagnostic {
  stage: string
  severity: 'WARNING' | 'ERROR'
  sourcePath: string | null
  message: string
}

export interface AnalysisSummary {
  repositoryName: string
  defaultBranch: string
  sourceFiles: number
  parsedFiles: number
  extractedFiles: number
  packages: number
  imports: number
  types: number
  fields: number
  methods: number
  annotations: number
  diagnostics: AnalysisDiagnostic[]
}

export interface AnalysisResponse {
  status: RunStatus | string
  message: string
  durationMs: number | null
  analysis: AnalysisSummary | null
}

export interface HealthResponse {
  status: string
}

export interface AnalysisRun {
  id: string
  repositoryUrl: string
  repositoryName: string
  status: RunStatus
  message: string
  createdAt: string
  durationMs: number
  analysis?: AnalysisSummary | null
}
