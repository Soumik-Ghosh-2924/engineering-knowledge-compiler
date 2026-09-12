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

export type RepositoryRole = 'APPLICATION' | 'DEPLOYMENT' | 'SERVICE' | 'SHARED_LIBRARY'
export type WorkspaceAnalysisStatus = 'QUEUED' | 'RUNNING' | 'COMPLETED' | 'PARTIALLY_COMPLETED' | 'FAILED'

export interface WorkspaceRepositoryInput {
  repositoryUrl: string
  role: RepositoryRole
  primary: boolean
  baseRef?: string
  headRef?: string
  declaredPurpose?: string
}

export interface WorkspaceResponse {
  id: string
  name: string
  repositories: Array<WorkspaceRepositoryInput & { id: string }>
  createdAt: string
}

export interface WorkspaceAnalysisResponse {
  id: string
  workspaceId: string
  status: WorkspaceAnalysisStatus
  createdAt: string
  completedAt: string | null
  repositories: Array<{ repositoryId: string; repositoryUrl: string; status: string; message: string }>
}

export interface PurposeStatement {
  value: string | null
  classification: 'DECLARED' | 'INFERRED' | 'USER_CONFIRMED' | 'UNKNOWN'
  confidence: string
  citations: Array<{ sourceType: string; source: string; description: string }>
}

export interface RepositoryChangeAnalysis {
  status: 'NOT_REQUESTED' | 'ANALYZED' | 'FAILED'
  baseRef: string | null
  headRef: string | null
  baseCommit: string | null
  headCommit: string | null
  commitCount: number
  commitsTruncated: boolean
  changedFileCount: number
  additions: number
  deletions: number
  commits: Array<{
    id: string
    shortId: string
    message: string
    author: string
    authoredAt: string
  }>
  changedFiles: Array<{
    path: string
    changeType: string
    additions: number
    deletions: number
  }>
  riskSignals: Array<{
    category: string
    severity: 'HIGH' | 'MEDIUM' | 'LOW'
    title: string
    description: string
    evidence: string[]
  }>
  message: string
}

export interface WorkspaceOverviewResponse {
  analysisId: string
  workspaceId: string
  workspaceName: string
  status: WorkspaceAnalysisStatus
  repositoryBriefs: Array<{
    repositoryId: string
    repositoryUrl: string
    role: RepositoryRole
    primary: boolean
    status: string
    purpose: PurposeStatement
    analysis: AnalysisSummary | null
    changeAnalysis?: RepositoryChangeAnalysis | null
    message: string
  }>
  systemOverview: {
    repositories: number
    analyzedRepositories: number
    failedRepositories: number
    suggestedRelationships: Array<{
      sourceRepositoryId: string
      targetRepositoryId: string
      relationship: string
      confidence: string
      confirmed: boolean
      citations: Array<{ sourceType: string; source: string; description: string }>
    }>
  }
}
