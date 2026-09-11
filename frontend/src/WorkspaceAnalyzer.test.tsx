import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import WorkspaceAnalyzer from './WorkspaceAnalyzer'

describe('Phase 2 workspace analyzer', () => {
  afterEach(() => {
    cleanup()
    vi.restoreAllMocks()
  })

  it('adds repositories without using a delimited URL field', () => {
    render(<WorkspaceAnalyzer />)
    fireEvent.click(screen.getByRole('button', { name: '+ Add repository' }))
    expect(screen.getAllByText(/Repository \d/)).toHaveLength(2)
    expect(screen.getAllByText('Repository URL')).toHaveLength(2)
  })

  it('creates, analyzes, and displays a workspace overview', async () => {
    const responses = [
      {
        id: 'workspace-1', name: 'EKC system', createdAt: '2026-09-11T00:00:00Z',
        repositories: [{ id: 'repository-1', repositoryUrl: 'https://github.com/acme/app.git', role: 'APPLICATION', primary: true }],
      },
      { id: 'analysis-1', workspaceId: 'workspace-1', status: 'COMPLETED', createdAt: '2026-09-11T00:00:00Z', completedAt: '2026-09-11T00:00:01Z', repositories: [] },
      {
        analysisId: 'analysis-1', workspaceId: 'workspace-1', workspaceName: 'EKC system', status: 'COMPLETED',
        repositoryBriefs: [{
          repositoryId: 'repository-1', repositoryUrl: 'https://github.com/acme/app.git', role: 'APPLICATION', primary: true,
          status: 'ANALYZED', purpose: { value: 'Analyzes code', classification: 'DECLARED', confidence: 'HIGH', citations: [] },
          analysis: { repositoryName: 'app', defaultBranch: 'main', sourceFiles: 3, parsedFiles: 3, extractedFiles: 3, packages: 1, imports: 2, types: 2, fields: 1, methods: 4, annotations: 0, diagnostics: [] },
          message: 'Analyzed',
        }],
        systemOverview: { repositories: 1, analyzedRepositories: 1, failedRepositories: 0, suggestedRelationships: [] },
      },
    ]
    vi.spyOn(globalThis, 'fetch').mockImplementation(async () => new Response(JSON.stringify(responses.shift()), { status: 200 }))
    render(<WorkspaceAnalyzer />)
    fireEvent.change(screen.getByLabelText('Workspace name'), { target: { value: 'EKC system' } })
    fireEvent.change(screen.getByText('Repository URL').querySelector('input')!, { target: { value: 'https://github.com/acme/app.git' } })
    fireEvent.change(screen.getByText(/Declared purpose/).querySelector('textarea')!, { target: { value: 'Analyzes code' } })
    fireEvent.click(screen.getByRole('button', { name: 'Build system overview' }))
    await waitFor(() => expect(screen.getByLabelText('Workspace system overview')).toHaveTextContent('EKC system'))
    expect(screen.getAllByText('Analyzes code')).toHaveLength(2)
    expect(globalThis.fetch).toHaveBeenCalledTimes(3)
  })
})
