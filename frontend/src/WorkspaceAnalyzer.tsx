import { FormEvent, useState } from 'react'
import { createWorkspace, getWorkspaceAnalysis, getWorkspaceOverview, startWorkspaceAnalysis } from './api'
import KnowledgeGraph from './KnowledgeGraph'
import type { RepositoryChangeAnalysis, RepositoryRole, WorkspaceOverviewResponse, WorkspaceRepositoryInput } from './types'

type RepositoryDraft = WorkspaceRepositoryInput & { key: string }

const newRepository = (primary = false): RepositoryDraft => ({
  key: crypto.randomUUID(), repositoryUrl: '', role: primary ? 'APPLICATION' : 'SERVICE', primary,
  baseRef: '', headRef: '',
})

const repositoryLabel = (repository: RepositoryDraft, index: number) => {
  const pathName = repository.repositoryUrl.trim().replace(/\/$/, '').split('/').pop()?.replace(/\.git$/, '')
  return `Repository ${index + 1} — ${pathName || repository.role.toLowerCase().replace('_', ' ')}`
}

const terminalStatuses = new Set(['COMPLETED', 'PARTIALLY_COMPLETED', 'FAILED'])
const wait = (milliseconds: number) => new Promise((resolve) => window.setTimeout(resolve, milliseconds))

function ChangeIntelligence({ change }: { change?: RepositoryChangeAnalysis | null }) {
  if (!change || change.status === 'NOT_REQUESTED') {
    return <div className="change-empty"><strong>Change intelligence not requested</strong><span>Add both a base and head ref to compare a branch, release, commit, or pull-request head.</span></div>
  }
  if (change.status === 'FAILED') {
    return <div className="change-empty change-failed"><strong>Change comparison unavailable</strong><span>{change.message}</span></div>
  }
  return <section className="change-intelligence" aria-label="Repository change intelligence">
    <div className="change-heading"><div><span>Change intelligence</span><strong>{change.baseRef} → {change.headRef}</strong></div><span className="change-method">STATIC DIFF · NO CODE EXECUTION</span></div>
    <div className="change-stats">
      <div><span>Commits</span><strong>{change.commitsTruncated ? `${change.commitCount - 1}+` : change.commitCount}</strong></div>
      <div><span>Changed files</span><strong>{change.changedFileCount}</strong></div>
      <div><span>Lines</span><strong><em>+{change.additions}</em> <b>−{change.deletions}</b></strong></div>
      <div><span>Review signals</span><strong>{change.riskSignals.length}</strong></div>
    </div>
    {change.riskSignals.length > 0 ? <div className="risk-signals">{change.riskSignals.map((signal) => <article className={`risk-signal ${signal.severity.toLowerCase()}`} key={signal.category}>
      <div><span>{signal.severity}</span><strong>{signal.title}</strong></div>
      <p>{signal.description}</p>
      <ul>{signal.evidence.map((item) => <li key={item}>{item}</li>)}</ul>
    </article>)}</div> : <div className="no-risk-signals">No dependency, deployment, data, security-sensitive, or broad-change signals were detected by the current rules.</div>}
    <div className="change-evidence">
      <details open><summary>Changed files <span>{change.changedFiles.length}{change.changedFileCount > change.changedFiles.length ? '+' : ''}</span></summary>
        <div className="changed-file-list">{change.changedFiles.map((file) => <div key={`${file.changeType}:${file.path}`}><span className={`change-type ${file.changeType.toLowerCase()}`}>{file.changeType}</span><code>{file.path}</code><span className="line-delta">+{file.additions} −{file.deletions}</span>{file.diffUrl && <a href={file.diffUrl} target="_blank" rel="noreferrer" aria-label={`Open GitHub diff for ${file.path}`}>Open diff ↗</a>}</div>)}</div>
      </details>
      <details><summary>Commits <span>{change.commits.length}{change.commitsTruncated ? '+' : ''}</span></summary>
        <div className="commit-list">{change.commits.map((commit) => <div key={commit.id}><code>{commit.shortId}</code><span><strong>{commit.message}</strong><small>{commit.author} · <time dateTime={commit.authoredAt}>{new Date(commit.authoredAt).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })}</time></small></span></div>)}</div>
      </details>
    </div>
    <p className="change-disclaimer">Review signals identify areas needing attention. They are not vulnerability findings and do not replace dependency or security scanners.</p>
  </section>
}

export default function WorkspaceAnalyzer({ onViewChange }: { onViewChange?: (resultsActive: boolean) => void }) {
  const [name, setName] = useState('')
  const [repositories, setRepositories] = useState<RepositoryDraft[]>([newRepository(true)])
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [overview, setOverview] = useState<WorkspaceOverviewResponse | null>(null)
  const [working, setWorking] = useState(false)
  const [resultsActive, setResultsActive] = useState(false)

  function updateRepository(key: string, change: Partial<RepositoryDraft>) {
    setRepositories((current) => current.map((repository) => repository.key === key ? { ...repository, ...change } : repository))
  }

  function selectPrimary(key: string) {
    setRepositories((current) => current.map((repository) => ({ ...repository, primary: repository.key === key })))
  }

  function removeRepository(key: string) {
    setRepositories((current) => {
      const remaining = current.filter((repository) => repository.key !== key)
      if (remaining.length > 0 && !remaining.some((repository) => repository.primary)) {
        return remaining.map((repository, index) => ({ ...repository, primary: index === 0 }))
      }
      return remaining
    })
  }

  async function submit(event: FormEvent) {
    event.preventDefault()
    setError('')
    setOverview(null)
    if (!name.trim()) return setError('Name this workspace before starting analysis.')
    if (repositories.some((repository) => !repository.repositoryUrl.trim())) return setError('Every repository needs a URL.')
    if (repositories.some((repository) => repository.baseRef?.trim() && repository.headRef?.trim() && repository.baseRef.trim() === repository.headRef.trim())) {
      return setError('The base and head refs must be different.')
    }
    setResultsActive(true)
    onViewChange?.(true)
    setWorking(true)
    try {
      setStatus('Creating workspace')
      const workspace = await createWorkspace(name.trim(), repositories.map((repository) => {
        const baseRef = repository.baseRef?.trim()
        const headRef = repository.headRef?.trim()
        const compareChanges = Boolean(baseRef && headRef)
        return {
          repositoryUrl: repository.repositoryUrl.trim(),
          role: repository.role,
          primary: repository.primary,
          baseRef: compareChanges ? baseRef : undefined,
          headRef: compareChanges ? headRef : undefined,
        }
      }))
      setStatus('Analysis queued')
      let analysis = await startWorkspaceAnalysis(workspace.id)
      for (let attempt = 0; attempt < 120 && !terminalStatuses.has(analysis.status); attempt += 1) {
        await wait(1000)
        analysis = await getWorkspaceAnalysis(analysis.id)
        setStatus(`Analysis ${analysis.status.toLowerCase().replace('_', ' ')}`)
      }
      if (!terminalStatuses.has(analysis.status)) throw new Error('Analysis is still running. Try again shortly.')
      const result = await getWorkspaceOverview(analysis.id)
      setOverview(result)
      setStatus(`Analysis ${result.status.toLowerCase().replace('_', ' ')}`)
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Workspace analysis failed.')
      setStatus('')
    } finally {
      setWorking(false)
    }
  }

  function editWorkspace() {
    setResultsActive(false)
    onViewChange?.(false)
  }

  const overviewContent = overview && <div className="workspace-overview" aria-label="Workspace system overview">
    <div className="overview-summary"><div><span>Workspace</span><strong>{overview.workspaceName}</strong></div><div><span>Analyzed</span><strong>{overview.systemOverview.analyzedRepositories}/{overview.systemOverview.repositories}</strong></div><div><span>Relationships</span><strong>{overview.systemOverview.suggestedRelationships.length}</strong></div></div>
    <div className="repository-briefs">{overview.repositoryBriefs.map((brief) => {
      const repositoryName = brief.analysis?.repositoryName || new URL(brief.repositoryUrl).pathname.split('/').pop()?.replace('.git', '') || 'Repository'
      return <article key={brief.repositoryId} className="repository-brief"><div className="brief-role">{brief.role.replace('_', ' ')}{brief.primary ? ' · SYSTEM ANCHOR' : ''}</div><h3>{repositoryName}</h3><div className="understanding-label">Repository understanding</div><p>{brief.purpose.value || 'Repository understanding is unavailable because analysis did not complete.'}</p><span className={`purpose-class ${brief.purpose.classification.toLowerCase()}`}>{brief.purpose.citations[0]?.sourceType || brief.purpose.classification} · {brief.purpose.confidence}</span>{brief.knowledgeGraph && <KnowledgeGraph graph={brief.knowledgeGraph} repositoryName={repositoryName}/>}<ChangeIntelligence change={brief.changeAnalysis}/>{brief.status === 'FAILED' && <div className="brief-error">{brief.message}</div>}</article>
    })}</div>
  </div>

  if (resultsActive) return <section className="workspace-section workspace-results" id="workspace">
    <div className="results-heading"><div><div className="section-label">System overview</div><h2>{name}</h2><p>Explore architecture and evaluate changes without the setup form competing for attention.</p></div><button type="button" onClick={editWorkspace} disabled={working}>← Edit workspace</button></div>
    {working && <div className="analysis-progress" role="status"><span className="spinner"/><div><strong>{status}</strong><p>Building repository understanding and structural relationships.</p></div></div>}
    {!working && error && <div className="results-error" role="alert"><strong>Analysis could not be completed</strong><p>{error}</p><button type="button" onClick={editWorkspace}>Review workspace inputs</button></div>}
    {overviewContent}
  </section>

  return <section className="workspace-section" id="workspace">
    <div className="section-heading"><div><div className="section-label">Phase 2 preview</div><h2>Map a system, not only a repository.</h2></div><p>Group related public repositories and build an evidence-backed orientation view.</p></div>
    <form className="workspace-form" onSubmit={submit}>
      <label htmlFor="workspace-name">Workspace name</label>
      <input id="workspace-name" value={name} onChange={(event) => setName(event.target.value)} placeholder="Payments platform" disabled={working}/>
      <div className="workspace-repositories">
        {repositories.map((repository, index) => <article className="repository-input-card" key={repository.key}>
          <div className="repository-input-heading"><strong>Repository {index + 1}</strong>{repositories.length > 1 && <button type="button" onClick={() => removeRepository(repository.key)} disabled={working}>Remove</button>}</div>
          <label>Repository URL<input value={repository.repositoryUrl} onChange={(event) => updateRepository(repository.key, { repositoryUrl: event.target.value })} placeholder="https://github.com/owner/repository.git" disabled={working}/></label>
          <div className="repository-input-grid">
            <label>Role<select value={repository.role} onChange={(event) => updateRepository(repository.key, { role: event.target.value as RepositoryRole })} disabled={working}>
              <option value="APPLICATION">Application</option><option value="DEPLOYMENT">Deployment / GitOps</option><option value="SERVICE">Service</option><option value="SHARED_LIBRARY">Shared library</option>
            </select></label>
          </div>
          <div className="repository-input-grid"><label>Compare from (base ref) <span>(optional)</span><input value={repository.baseRef} onChange={(event) => updateRepository(repository.key, { baseRef: event.target.value })} placeholder="main or commit SHA" disabled={working}/></label><label>Compare to (head ref) <span>(optional)</span><input value={repository.headRef} onChange={(event) => updateRepository(repository.key, { headRef: event.target.value })} placeholder="Leave empty for repository discovery" disabled={working}/></label></div>
          <p className="ref-help">Leave comparison empty for first-time discovery. Change intelligence runs only when both refs are provided.</p>
        </article>)}
      </div>
      <div className="system-anchor">
        <label htmlFor="system-anchor">System anchor</label>
        <select id="system-anchor" value={repositories.find((repository) => repository.primary)?.key || ''} onChange={(event) => selectPrimary(event.target.value)} disabled={working}>
          {repositories.map((repository, index) => <option key={repository.key} value={repository.key}>{repositoryLabel(repository, index)}</option>)}
        </select>
        <p>Select the repository that provides the main context for the system overview. Change analysis still runs for every repository with both refs supplied.</p>
      </div>
      <div className="workspace-actions"><button type="button" onClick={() => setRepositories((current) => [...current, newRepository()])} disabled={working || repositories.length >= 5}>+ Add repository</button><button className="primary-button" type="submit" disabled={working}>{working ? status : 'Build system overview'}</button></div>
      {error && <div className="form-error" role="alert">{error}</div>}
    </form>
  </section>
}
