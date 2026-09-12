import { FormEvent, useCallback, useEffect, useMemo, useState } from 'react'
import { analyzeRepository, getHealth } from './api'
import { Icon } from './icons'
import { clearRuns, loadRuns, saveRuns } from './storage'
import type { AnalysisRun, ServiceState } from './types'
import WorkspaceAnalyzer from './WorkspaceAnalyzer'

const SAMPLE_REPO = 'https://github.com/spring-projects/spring-petclinic.git'
const PIPELINE = [
  ['01', 'Acquire', 'Clone and validate the repository'],
  ['02', 'Discover', 'Locate supported source files'],
  ['03', 'Parse', 'Build the abstract syntax tree'],
  ['04', 'Summarize', 'Return structured analysis and diagnostics'],
]
const PHASE2_ENABLED = import.meta.env.VITE_PHASE2_ENABLED === 'true'

function repositoryName(url: string) {
  try {
    const path = new URL(url).pathname.replace(/\/$/, '')
    return path.split('/').pop()?.replace(/\.git$/, '') || 'repository'
  } catch {
    return 'repository'
  }
}

function isRepositoryUrl(value: string) {
  try {
    const url = new URL(value)
    return ['http:', 'https:', 'ssh:'].includes(url.protocol) && url.pathname.split('/').filter(Boolean).length >= 2
  } catch {
    return false
  }
}

const formatTime = (iso: string) => new Intl.DateTimeFormat(undefined, {
  month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit',
}).format(new Date(iso))

export default function App() {
  const [repoUrl, setRepoUrl] = useState('')
  const [runs, setRuns] = useState<AnalysisRun[]>(loadRuns)
  const [service, setService] = useState<ServiceState>('checking')
  const [analyzing, setAnalyzing] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [menuOpen, setMenuOpen] = useState(false)
  const [selectedRun, setSelectedRun] = useState<AnalysisRun | null>(null)
  const [theme, setTheme] = useState(() => window.localStorage.getItem('ekc.theme') || 'light')
  const [phase2ResultsActive, setPhase2ResultsActive] = useState(false)

  const checkHealth = useCallback(async () => {
    setService('checking')
    const controller = new AbortController()
    const timer = window.setTimeout(() => controller.abort(), 5000)
    try {
      const response = await getHealth(controller.signal)
      setService(response.status === 'UP' ? 'online' : 'offline')
    } catch {
      setService('offline')
    } finally {
      window.clearTimeout(timer)
    }
  }, [])

  useEffect(() => {
    // The first health request intentionally transitions the initial checking state.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    checkHealth()
  }, [checkHealth])
  useEffect(() => {
    document.documentElement.dataset.theme = theme
    window.localStorage.setItem('ekc.theme', theme)
  }, [theme])
  useEffect(() => saveRuns(runs), [runs])
  useEffect(() => {
    if (!notice) return
    const timer = window.setTimeout(() => setNotice(''), 2600)
    return () => window.clearTimeout(timer)
  }, [notice])

  const stats = useMemo(() => ({
    total: runs.length,
    success: runs.filter((run) => run.status === 'ANALYZED').length,
    lastRun: runs[0]?.createdAt,
  }), [runs])

  async function handleAnalyze(event: FormEvent) {
    event.preventDefault()
    const normalized = repoUrl.trim()
    if (!isRepositoryUrl(normalized)) {
      setError('Enter a complete repository URL, for example https://github.com/owner/repository.git')
      return
    }
    setError('')
    setAnalyzing(true)
    const started = performance.now()
    try {
      const response = await analyzeRepository(normalized)
      const run: AnalysisRun = {
        id: crypto.randomUUID(), repositoryUrl: normalized, repositoryName: repositoryName(normalized),
        status: response.status === 'ANALYZED' ? 'ANALYZED' : 'FAILED', message: response.message,
        createdAt: new Date().toISOString(),
        durationMs: response.durationMs ?? Math.round(performance.now() - started),
        analysis: response.analysis,
      }
      setRuns((previous) => [run, ...previous].slice(0, 20))
      setRepoUrl('')
      setNotice('Repository analyzed successfully')
      checkHealth()
    } catch (caught) {
      const message = caught instanceof Error ? caught.message : 'Analysis failed. Please try again.'
      const run: AnalysisRun = {
        id: crypto.randomUUID(), repositoryUrl: normalized, repositoryName: repositoryName(normalized),
        status: 'FAILED', message, createdAt: new Date().toISOString(),
        durationMs: Math.round(performance.now() - started),
      }
      setRuns((previous) => [run, ...previous].slice(0, 20))
      setError(message)
    } finally {
      setAnalyzing(false)
    }
  }

  function copy(value: string, message = 'Copied to clipboard') {
    navigator.clipboard.writeText(value).then(() => setNotice(message)).catch(() => setNotice('Could not copy'))
  }

  function exportRuns() {
    const blob = new Blob([JSON.stringify(runs, null, 2)], { type: 'application/json' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `ekc-history-${new Date().toISOString().slice(0, 10)}.json`
    link.click()
    URL.revokeObjectURL(link.href)
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <a href="#main" className="brand" aria-label="EKC home">
          <span className="brand-mark">E</span>
          <span><strong>EKC</strong><small>Engineering Knowledge Compiler</small></span>
        </a>
        <nav className={menuOpen ? 'nav open' : 'nav'} aria-label="Primary navigation">
          {PHASE2_ENABLED ? <a href="#workspace" onClick={() => setMenuOpen(false)}>{phase2ResultsActive ? 'System overview' : 'Analyze system'}</a> : <>
            <a href="#compiler" onClick={() => setMenuOpen(false)}>Analyze</a>
            <a href="#pipeline" onClick={() => setMenuOpen(false)}>How it works</a>
            <a href="#history" onClick={() => setMenuOpen(false)}>History</a>
            <a href="#quickstart" onClick={() => setMenuOpen(false)}>Quick start</a>
          </>}
        </nav>
        <div className="topbar-actions">
          <button className="icon-button" onClick={() => setTheme(theme === 'light' ? 'dark' : 'light')} aria-label={`Use ${theme === 'light' ? 'dark' : 'light'} theme`}>
            <Icon name={theme === 'light' ? 'moon' : 'sun'} />
          </button>
          <a className="github-link" href="https://github.com/Soumik-Ghosh-2924" target="_blank" rel="noreferrer"><Icon name="github" /> GitHub</a>
          <button className="menu-button" onClick={() => setMenuOpen(!menuOpen)} aria-label="Toggle menu"><Icon name={menuOpen ? 'close' : 'menu'} /></button>
        </div>
      </header>

      <main id="main">
        {!phase2ResultsActive && <section className="hero">
          <div className="eyebrow"><span /> From source code to structured knowledge</div>
          <h1>{PHASE2_ENABLED ? <>Understand the system.<br/><em>Navigate every connection.</em></> : <>Understand a codebase.<br/><em>Before it understands you.</em></>}</h1>
          <p className="hero-copy">{PHASE2_ENABLED ? 'Group related Java repositories, discover their purpose, map their architecture, and inspect the risk of a proposed change.' : 'Submit a Java repository and let EKC acquire, discover, parse, and extract its engineering structure through one focused workflow.'}</p>
          <div className="hero-meta">
            <button className={`service-pill ${service}`} onClick={checkHealth} title="Refresh service status">
              <span className="status-dot" />
              {service === 'checking' ? 'Checking API' : service === 'online' ? 'Analyzer online' : 'Analyzer offline'}
              <Icon name="refresh" />
            </button>
            <span>{PHASE2_ENABLED ? 'API v2' : 'API v1'}</span><span>Java repositories</span>
          </div>
        </section>}

        {!PHASE2_ENABLED && <section className="compile-grid" id="compiler">
          <div className="compile-card">
            <div className="section-label">New repository analysis</div>
            <h2>Point us to the repository.</h2>
            <p>Use a public HTTPS Git URL. EKC performs static source analysis; it does not execute the repository's Maven or Gradle build.</p>
            <form onSubmit={handleAnalyze} noValidate>
              <label htmlFor="repository-url">Repository URL</label>
              <div className={`url-control ${error ? 'has-error' : ''}`}>
                <Icon name="github" />
                <input id="repository-url" value={repoUrl} onChange={(e) => { setRepoUrl(e.target.value); setError('') }} placeholder="https://github.com/owner/repository.git" autoComplete="url" disabled={analyzing} />
                {repoUrl && !analyzing && <button type="button" className="input-clear" onClick={() => setRepoUrl('')} aria-label="Clear URL"><Icon name="close" /></button>}
              </div>
              {error && <div className="form-error" role="alert"><Icon name="warning" />{error}</div>}
              <div className="form-actions">
                <button className="primary-button" type="submit" disabled={analyzing || service === 'offline'}>
                  {analyzing ? <><span className="spinner" /> Analyzing repository…</> : <><Icon name="play" /> Analyze repository</>}
                </button>
                <button className="text-button" type="button" onClick={() => { setRepoUrl(SAMPLE_REPO); setError('') }} disabled={analyzing}>Try an example</button>
              </div>
            </form>
            {service === 'offline' && <div className="offline-note"><Icon name="health" /><span><strong>The API is not reachable.</strong> Start the Spring Boot service on port 8080, then refresh status.</span></div>}
          </div>

          <aside className="signal-card">
            <div className="signal-top"><span>Compiler signal</span><Icon name="health" /></div>
            <div className="signal-visual" aria-hidden="true">
              <span className="axis axis-one"/><span className="axis axis-two"/><span className="axis axis-three"/>
              <svg viewBox="0 0 500 150" preserveAspectRatio="none"><path d="M0 105 C45 105 48 106 74 101 S108 84 129 91 157 120 181 100 202 52 230 74 259 102 281 82 304 60 326 78 350 100 374 79 400 55 420 70 455 104 500 89"/></svg>
            </div>
            <div className="signal-stats">
              <div><strong>{stats.total.toString().padStart(2, '0')}</strong><span>Local runs</span></div>
              <div><strong>{stats.success.toString().padStart(2, '0')}</strong><span>Analyzed</span></div>
              <div><strong>{stats.lastRun ? formatTime(stats.lastRun).split(',')[0] : '—'}</strong><span>Last run</span></div>
            </div>
            <p>Run history is kept locally in this browser. Source code is sent only to your configured EKC API.</p>
          </aside>
        </section>}

        {PHASE2_ENABLED && <WorkspaceAnalyzer onViewChange={setPhase2ResultsActive} />}

        {!phase2ResultsActive && <section className="pipeline-section" id="pipeline">
          <div className="section-heading"><div><div className="section-label">The pipeline</div><h2>Four stages. One clear outcome.</h2></div><p>Each request moves through the backend’s acquisition and AST processing flow.</p></div>
          <div className="pipeline-list">
            {PIPELINE.map(([number, title, description], index) => <article className="pipeline-item" key={number}>
              <span className="pipeline-number">{number}</span>
              <div className="pipeline-icon"><Icon name={index === 0 ? 'download' : index === 1 ? 'code' : index === 2 ? 'terminal' : 'book'} /></div>
              <div><h3>{title}</h3><p>{description}</p></div>
              {index < PIPELINE.length - 1 && <Icon name="chevron" className="pipeline-arrow" />}
            </article>)}
          </div>
        </section>}

        {!PHASE2_ENABLED && <section className="history-section" id="history">
          <div className="section-heading history-heading">
            <div><div className="section-label">Your workspace</div><h2>Recent analyses</h2></div>
            {runs.length > 0 && <div className="history-actions"><button onClick={exportRuns}><Icon name="download"/> Export</button><button onClick={() => { clearRuns(); setRuns([]); setNotice('History cleared') }}><Icon name="trash"/> Clear</button></div>}
          </div>
          {runs.length === 0 ? <div className="empty-state">
            <div className="empty-icon"><Icon name="history" /></div><h3>No analyses yet</h3><p>Your completed and failed runs will appear here.</p><a href="#compiler">Analyze your first repository <Icon name="arrow" /></a>
          </div> : <div className="history-table-wrap"><table>
            <thead><tr><th>Repository</th><th>Status</th><th>Submitted</th><th>Duration</th><th><span className="sr-only">Details</span></th></tr></thead>
            <tbody>{runs.map((run) => <tr key={run.id}>
              <td><div className="repo-cell"><span><Icon name="code" /></span><div><strong>{run.repositoryName}</strong><small>{run.repositoryUrl}</small></div></div></td>
              <td><span className={`run-status ${run.status.toLowerCase()}`}><i />{run.status === 'ANALYZED' ? 'Analyzed' : 'Failed'}</span></td>
              <td>{formatTime(run.createdAt)}</td><td>{(run.durationMs / 1000).toFixed(1)}s</td>
              <td><button className="row-button" onClick={() => setSelectedRun(run)} aria-label={`View ${run.repositoryName} details`}><Icon name="arrow" /></button></td>
            </tr>)}</tbody>
          </table></div>}
        </section>}

        {!phase2ResultsActive && <section className="quickstart" id="quickstart">
          <div><div className="section-label light">Local development</div><h2>Start both sides<br/>in two terminals.</h2><p>The frontend proxy already points to the Spring Boot server. No CORS setup or environment variables are required.</p></div>
          <div className="command-stack">
            <div className="command-card"><div><span>01</span> Backend</div><code>cd backend &amp;&amp; ./mvnw spring-boot:run</code><button onClick={() => copy('cd backend && ./mvnw spring-boot:run', 'Backend command copied')}><Icon name="copy" /></button></div>
            <div className="command-card"><div><span>02</span> Frontend</div><code>cd frontend &amp;&amp; npm install &amp;&amp; npm run dev</code><button onClick={() => copy('cd frontend && npm install && npm run dev', 'Frontend command copied')}><Icon name="copy" /></button></div>
          </div>
        </section>}
      </main>

      <footer><div className="brand"><span className="brand-mark">E</span><span><strong>EKC</strong><small>Engineering Knowledge Compiler</small></span></div><p>Built for engineers who inherit complexity.</p><a href="#main">Back to top ↑</a></footer>

      {notice && <div className="toast" role="status"><Icon name="check" />{notice}</div>}
      {selectedRun && <div className="modal-backdrop" role="presentation" onMouseDown={() => setSelectedRun(null)}>
        <section className="modal" role="dialog" aria-modal="true" aria-labelledby="run-title" onMouseDown={(e) => e.stopPropagation()}>
          <button className="modal-close" onClick={() => setSelectedRun(null)} aria-label="Close"><Icon name="close" /></button>
          <div className={`modal-status ${selectedRun.status.toLowerCase()}`}><Icon name={selectedRun.status === 'ANALYZED' ? 'check' : 'warning'} /></div>
          <div className="section-label">Analysis detail</div><h2 id="run-title">{selectedRun.repositoryName}</h2><p>{selectedRun.message}</p>
          <dl><div><dt>Status</dt><dd>{selectedRun.status}</dd></div><div><dt>Duration</dt><dd>{(selectedRun.durationMs / 1000).toFixed(2)} seconds</dd></div><div><dt>Submitted</dt><dd>{formatTime(selectedRun.createdAt)}</dd></div></dl>
          <div className="modal-url"><span>{selectedRun.repositoryUrl}</span><button onClick={() => copy(selectedRun.repositoryUrl)}><Icon name="copy" /></button></div>
          {selectedRun.analysis && <>
            <div className="analysis-metrics" aria-label="Repository analysis summary">
              <div><strong>{selectedRun.analysis.sourceFiles}</strong><span>Sources</span></div>
              <div><strong>{selectedRun.analysis.parsedFiles}</strong><span>Parsed</span></div>
              <div><strong>{selectedRun.analysis.types}</strong><span>Types</span></div>
              <div><strong>{selectedRun.analysis.methods}</strong><span>Methods</span></div>
              <div><strong>{selectedRun.analysis.fields}</strong><span>Fields</span></div>
              <div><strong>{selectedRun.analysis.imports}</strong><span>Imports</span></div>
            </div>
            <div className="analysis-branch">Default branch <strong>{selectedRun.analysis.defaultBranch}</strong></div>
            {selectedRun.analysis.diagnostics.length > 0 && <div className="diagnostics">
              <h3>Diagnostics ({selectedRun.analysis.diagnostics.length})</h3>
              {selectedRun.analysis.diagnostics.slice(0, 5).map((diagnostic, index) => <div className={`diagnostic ${diagnostic.severity.toLowerCase()}`} key={`${diagnostic.sourcePath}-${index}`}>
                <strong>{diagnostic.stage.replaceAll('_', ' ')}</strong>
                <span>{diagnostic.sourcePath || 'Repository'}</span>
                <p>{diagnostic.message}</p>
              </div>)}
            </div>}
          </>}
        </section>
      </div>}
    </div>
  )
}
