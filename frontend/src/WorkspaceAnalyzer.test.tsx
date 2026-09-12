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
    expect(screen.getAllByText(/^Repository \d$/)).toHaveLength(2)
    expect(screen.getAllByText('Repository URL')).toHaveLength(2)
    expect(screen.getByLabelText('System anchor')).not.toHaveValue('')
    expect(screen.queryByText('Primary change target')).not.toBeInTheDocument()
    expect(screen.queryByText(/Declared purpose/)).not.toBeInTheDocument()
  })

  it('requires a complete ref pair before requesting change analysis', () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch')
    render(<WorkspaceAnalyzer />)
    fireEvent.change(screen.getByLabelText('Workspace name'), { target: { value: 'EKC system' } })
    fireEvent.change(screen.getByText('Repository URL').querySelector('input')!, { target: { value: 'https://github.com/acme/app.git' } })
    fireEvent.change(screen.getByLabelText('Compare from (base ref)'), { target: { value: 'main' } })

    fireEvent.click(screen.getByRole('button', { name: 'Build system overview' }))

    expect(screen.getByRole('alert')).toHaveTextContent('Provide both the base and head ref')
    expect(fetchSpy).not.toHaveBeenCalled()
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
          status: 'ANALYZED', purpose: { value: 'EKC turns repository structure into engineering knowledge.', classification: 'INFERRED', confidence: 'HIGH', citations: [{ sourceType: 'README', source: 'README.md', description: 'Extracted from README.' }] },
          analysis: { repositoryName: 'app', defaultBranch: 'main', sourceFiles: 3, parsedFiles: 3, extractedFiles: 3, packages: 1, imports: 2, types: 2, fields: 1, methods: 4, annotations: 0, diagnostics: [] },
          changeAnalysis: {
            status: 'ANALYZED', baseRef: 'main', headRef: 'feature/change', baseCommit: 'a', headCommit: 'b',
            commitCount: 1, commitsTruncated: false, changedFileCount: 2, additions: 12, deletions: 3,
            commits: [{ id: 'b', shortId: 'b1234567', message: 'Change dependencies', author: 'Engineer', authoredAt: '2026-09-11T00:00:00Z' }],
            changedFiles: [{ path: 'pom.xml', changeType: 'MODIFY', additions: 3, deletions: 1, diffUrl: 'https://github.com/acme/app/compare/a...b#diff-test' }],
            riskSignals: [{ category: 'DEPENDENCY_CHANGE', severity: 'HIGH', title: 'Dependency definition changed', description: 'Review dependencies.', evidence: ['pom.xml'] }],
            message: 'Compared',
          },
          knowledgeGraph: {
            totalTypeCount: 3, truncated: false,
            nodes: [
              { id: 'type:app.Application', label: 'Application', kind: 'APPLICATION', qualifiedName: 'app.Application', packageName: 'app', sourcePath: 'Application.java', annotations: ['SpringBootApplication'], imports: [], fields: [], methods: [] },
              { id: 'type:app.OrderController', label: 'OrderController', kind: 'CONTROLLER', qualifiedName: 'app.OrderController', packageName: 'app', sourcePath: 'OrderController.java', annotations: ['RestController'], imports: ['app.OrderService'], fields: [{ name: 'service', type: 'OrderService' }], methods: [{ name: 'getOrder', returnType: 'OrderDto', annotations: ['GetMapping'], parameters: [{ name: 'id', type: 'Long' }], localVariables: [{ name: 'result', type: 'OrderDto' }] }] },
              { id: 'type:app.OrderService', label: 'OrderService', kind: 'SERVICE', qualifiedName: 'app.OrderService', packageName: 'app', sourcePath: 'OrderService.java', annotations: ['Service'], imports: [], fields: [], methods: [] },
            ],
            edges: [
              { source: 'type:app.Application', target: 'type:app.OrderController', kind: 'DISCOVERS', label: 'framework discovery', confidence: 'MEDIUM', evidence: 'Spring component scanning' },
              { source: 'type:app.OrderController', target: 'type:app.OrderService', kind: 'DEPENDS_ON', label: 'field service', confidence: 'HIGH', evidence: 'Field type: OrderService' },
            ],
          },
          message: 'Analyzed',
        }],
        systemOverview: { repositories: 1, analyzedRepositories: 1, failedRepositories: 0, suggestedRelationships: [] },
      },
    ]
    vi.spyOn(globalThis, 'fetch').mockImplementation(async () => new Response(JSON.stringify(responses.shift()), { status: 200 }))
    render(<WorkspaceAnalyzer />)
    fireEvent.change(screen.getByLabelText('Workspace name'), { target: { value: 'EKC system' } })
    fireEvent.change(screen.getByText('Repository URL').querySelector('input')!, { target: { value: 'https://github.com/acme/app.git' } })
    fireEvent.click(screen.getByRole('button', { name: 'Build system overview' }))
    await waitFor(() => expect(screen.getByLabelText('Workspace system overview')).toHaveTextContent('EKC system'))
    expect(screen.getByText('EKC turns repository structure into engineering knowledge.')).toBeInTheDocument()
    expect(screen.getByText('README · HIGH')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Open GitHub diff for pom.xml' })).toHaveAttribute('href', 'https://github.com/acme/app/compare/a...b#diff-test')
    const commitTime = document.querySelector('time')
    expect(commitTime).toHaveAttribute('datetime', '2026-09-11T00:00:00Z')
    expect(commitTime?.textContent).toMatch(/2026/)
    expect(screen.getByText('Change intelligence')).toBeInTheDocument()
    expect(screen.getByText('Dependency definition changed')).toBeInTheDocument()
    expect(screen.getByLabelText('app knowledge graph')).toHaveTextContent('Repository knowledge graph')
    fireEvent.click(screen.getByLabelText('Select controller OrderController'))
    expect(screen.getByLabelText('Selected graph node details')).toHaveTextContent('OrderController')
    fireEvent.click(screen.getByRole('button', { name: 'Explore class internals →' }))
    expect(screen.getByLabelText('OrderController class internals graph')).toBeInTheDocument()
    expect(screen.getByLabelText('Method details')).toHaveValue('0')
    expect(screen.getByText('OrderDto', { selector: '.method-signature strong' })).toBeInTheDocument()
    expect(screen.getByLabelText('OrderController class internals graph')).toHaveTextContent('RETURN')
    expect(globalThis.fetch).toHaveBeenCalledTimes(3)
  })
})
