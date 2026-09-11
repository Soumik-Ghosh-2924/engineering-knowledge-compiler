import { FormEvent, useState } from 'react'
import { createWorkspace, getWorkspaceAnalysis, getWorkspaceOverview, startWorkspaceAnalysis } from './api'
import type { RepositoryRole, WorkspaceOverviewResponse, WorkspaceRepositoryInput } from './types'

type RepositoryDraft = WorkspaceRepositoryInput & { key: string }

const newRepository = (primary = false): RepositoryDraft => ({
  key: crypto.randomUUID(), repositoryUrl: '', role: primary ? 'APPLICATION' : 'SERVICE', primary,
  baseRef: '', headRef: '', declaredPurpose: '',
})

const terminalStatuses = new Set(['COMPLETED', 'PARTIALLY_COMPLETED', 'FAILED'])
const wait = (milliseconds: number) => new Promise((resolve) => window.setTimeout(resolve, milliseconds))

export default function WorkspaceAnalyzer() {
  const [name, setName] = useState('')
  const [repositories, setRepositories] = useState<RepositoryDraft[]>([newRepository(true)])
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [overview, setOverview] = useState<WorkspaceOverviewResponse | null>(null)
  const [working, setWorking] = useState(false)

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
    setWorking(true)
    try {
      setStatus('Creating workspace')
      const workspace = await createWorkspace(name.trim(), repositories.map((repository) => ({
        repositoryUrl: repository.repositoryUrl.trim(),
        role: repository.role,
        primary: repository.primary,
        baseRef: repository.baseRef?.trim(),
        headRef: repository.headRef?.trim(),
        declaredPurpose: repository.declaredPurpose?.trim(),
      })))
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
            <label className="primary-choice"><input type="radio" name="primary-repository" checked={repository.primary} onChange={() => selectPrimary(repository.key)} disabled={working}/> Primary change target</label>
          </div>
          <div className="repository-input-grid"><label>Base ref<input value={repository.baseRef} onChange={(event) => updateRepository(repository.key, { baseRef: event.target.value })} placeholder="main" disabled={working}/></label><label>Head ref<input value={repository.headRef} onChange={(event) => updateRepository(repository.key, { headRef: event.target.value })} placeholder="feature/my-change" disabled={working}/></label></div>
          <label>Declared purpose <span>(optional)</span><textarea value={repository.declaredPurpose} onChange={(event) => updateRepository(repository.key, { declaredPurpose: event.target.value })} placeholder="What does this repository provide, and who benefits?" disabled={working}/></label>
        </article>)}
      </div>
      <div className="workspace-actions"><button type="button" onClick={() => setRepositories((current) => [...current, newRepository()])} disabled={working || repositories.length >= 5}>+ Add repository</button><button className="primary-button" type="submit" disabled={working}>{working ? status : 'Build system overview'}</button></div>
      {error && <div className="form-error" role="alert">{error}</div>}
    </form>
    {overview && <div className="workspace-overview" aria-label="Workspace system overview">
      <div className="overview-summary"><div><span>Workspace</span><strong>{overview.workspaceName}</strong></div><div><span>Analyzed</span><strong>{overview.systemOverview.analyzedRepositories}/{overview.systemOverview.repositories}</strong></div><div><span>Relationships</span><strong>{overview.systemOverview.suggestedRelationships.length}</strong></div></div>
      <div className="repository-briefs">{overview.repositoryBriefs.map((brief) => <article key={brief.repositoryId} className="repository-brief"><div className="brief-role">{brief.role.replace('_', ' ')}{brief.primary ? ' · PRIMARY' : ''}</div><h3>{brief.analysis?.repositoryName || new URL(brief.repositoryUrl).pathname.split('/').pop()?.replace('.git', '')}</h3><p>{brief.purpose.value || 'Purpose is unknown. Add declared context rather than relying on an unsupported inference.'}</p><span className={`purpose-class ${brief.purpose.classification.toLowerCase()}`}>{brief.purpose.classification} · {brief.purpose.confidence}</span>{brief.analysis && <dl><div><dt>Sources</dt><dd>{brief.analysis.sourceFiles}</dd></div><div><dt>Types</dt><dd>{brief.analysis.types}</dd></div><div><dt>Methods</dt><dd>{brief.analysis.methods}</dd></div></dl>}{brief.status === 'FAILED' && <div className="brief-error">{brief.message}</div>}</article>)}</div>
    </div>}
  </section>
}
