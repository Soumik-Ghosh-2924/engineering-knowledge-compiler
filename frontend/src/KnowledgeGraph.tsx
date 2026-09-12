import { KeyboardEvent, useId, useMemo, useState } from 'react'
import type { KnowledgeEdge, KnowledgeNode, RepositoryKnowledgeGraph } from './types'

type GraphMode = 'architecture' | 'internals'
type GraphFilter = 'connected' | 'layers' | 'all'
type Point = { x: number; y: number }

const kindOrder = ['APPLICATION', 'CONTROLLER', 'SERVICE', 'REPOSITORY', 'MODEL', 'INTERFACE', 'CLASS', 'RECORD', 'ENUM', 'ANNOTATION']
const architectureKinds = new Set(['APPLICATION', 'CONTROLLER', 'SERVICE', 'REPOSITORY', 'MODEL'])
const importantEdges = new Set(['DEPENDS_ON', 'EXTENDS', 'IMPLEMENTS', 'RETURNS', 'ACCEPTS', 'USES', 'DISCOVERS'])
const short = (value: string, length = 22) => value.length <= length ? value : `${value.slice(0, length - 1)}…`

function architectureLayout(nodes: KnowledgeNode[]) {
  const groups = new Map<string, KnowledgeNode[]>()
  nodes.forEach((node) => groups.set(node.kind, [...(groups.get(node.kind) || []), node]))
  const kinds = kindOrder.filter((kind) => groups.has(kind))
  const positions = new Map<string, Point>()
  const columnWidth = Math.max(180, 1040 / Math.max(1, kinds.length))
  let rows = 1
  kinds.forEach((kind, column) => {
    const group = groups.get(kind) || []
    rows = Math.max(rows, group.length)
    group.forEach((node, row) => positions.set(node.id, { x: 35 + column * columnWidth, y: 60 + row * 86 }))
  })
  return { positions, width: Math.max(1080, kinds.length * columnWidth + 80), height: Math.max(440, rows * 86 + 90) }
}

function GraphNode({ node, point, selected, onSelect }: { node: KnowledgeNode; point: Point; selected: boolean; onSelect: () => void }) {
  const keySelect = (event: KeyboardEvent<SVGGElement>) => {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault()
      onSelect()
    }
  }
  return <g className={`graph-node kind-${node.kind.toLowerCase()}${selected ? ' selected' : ''}`} transform={`translate(${point.x} ${point.y})`} role="button" tabIndex={0} aria-label={`Select ${node.kind.toLowerCase()} ${node.label}`} onClick={onSelect} onKeyDown={keySelect}>
    <rect width="154" height="58" rx="6"/>
    <text className="node-kind" x="12" y="17">{node.kind}</text>
    <text className="node-label" x="12" y="39">{short(node.label)}</text>
  </g>
}

function ArchitectureGraph({ nodes, edges, selectedId, onSelect, markerId, zoom }: { nodes: KnowledgeNode[]; edges: KnowledgeEdge[]; selectedId: string; onSelect: (id: string) => void; markerId: string; zoom: number }) {
  const layout = useMemo(() => architectureLayout(nodes), [nodes])
  return <svg className="knowledge-svg" viewBox={`0 0 ${layout.width} ${layout.height}`} style={{ width: `${layout.width * zoom}px`, height: `${layout.height * zoom}px` }} aria-label="Repository architecture graph">
    <defs><marker id={markerId} viewBox="0 0 10 10" refX="8" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse"><path d="M 0 0 L 10 5 L 0 10 z"/></marker></defs>
    <g className="graph-edges">{edges.map((edge, index) => {
      const source = layout.positions.get(edge.source)
      const target = layout.positions.get(edge.target)
      if (!source || !target) return null
      return <line key={`${edge.source}:${edge.target}:${edge.kind}:${index}`} x1={source.x + 154} y1={source.y + 29} x2={target.x} y2={target.y + 29} className={`edge-${edge.kind.toLowerCase()}`} markerEnd={`url(#${markerId})`}><title>{edge.label}: {edge.evidence} ({edge.confidence.toLowerCase()} confidence)</title></line>
    })}</g>
    {nodes.map((node) => <GraphNode key={node.id} node={node} point={layout.positions.get(node.id)!} selected={node.id === selectedId} onSelect={() => onSelect(node.id)}/>)}
  </svg>
}

type InternalNode = { id: string; label: string; detail: string; kind: string; point: Point }

function InternalsGraph({ node, markerId, zoom }: { node: KnowledgeNode; markerId: string; zoom: number }) {
  const [methodName, setMethodName] = useState(node.methods[0]?.name || '')
  const activeMethod = node.methods.find((method) => method.name === methodName) || node.methods[0]
  const imports = node.imports.slice(0, 8)
  const fields = node.fields.slice(0, 8)
  const methods = node.methods.slice(0, 10)
  const variables = activeMethod ? [...activeMethod.parameters.map((item) => ({ ...item, scope: 'PARAMETER' })), ...activeMethod.localVariables.map((item) => ({ ...item, scope: 'LOCAL' }))].slice(0, 10) : []
  const rows = Math.max(imports.length, fields.length, methods.length, variables.length, 3)
  const height = Math.max(500, rows * 68 + 100)
  const internalNodes: InternalNode[] = [
    ...imports.map((item, index) => ({ id: `import:${index}`, label: short(item.split('.').pop() || item), detail: item, kind: 'IMPORT', point: { x: 25, y: 54 + index * 68 } })),
    { id: 'class', label: node.label, detail: node.kind, kind: node.kind, point: { x: 245, y: Math.max(150, height / 2 - 30) } },
    ...fields.map((item, index) => ({ id: `field:${index}`, label: short(item.name), detail: item.type, kind: 'FIELD', point: { x: 475, y: 54 + index * 68 } })),
    ...methods.map((item, index) => ({ id: `method:${index}`, label: short(item.name), detail: item.returnType, kind: 'METHOD', point: { x: 700, y: 54 + index * 68 } })),
    ...variables.map((item, index) => ({ id: `variable:${index}`, label: short(item.name), detail: `${item.scope} · ${item.type}`, kind: item.scope, point: { x: 925, y: 54 + index * 68 } })),
  ]
  const center = internalNodes.find((item) => item.id === 'class')!
  const activeMethodIndex = methods.findIndex((method) => method.name === activeMethod?.name)
  const activeMethodNode = internalNodes.find((item) => item.id === `method:${activeMethodIndex}`)
  return <div className="internals-wrap">
    <div className="method-focus"><span>Variable scope</span><select value={activeMethod?.name || ''} onChange={(event) => setMethodName(event.target.value)} aria-label="Method variable scope">{methods.map((method) => <option key={method.name} value={method.name}>{method.name}()</option>)}</select></div>
    <svg className="knowledge-svg internals-svg" viewBox={`0 0 1120 ${height}`} style={{ width: `${1120 * zoom}px`, height: `${height * zoom}px` }} aria-label={`${node.label} class internals graph`}>
      <defs><marker id={markerId} viewBox="0 0 10 10" refX="8" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse"><path d="M 0 0 L 10 5 L 0 10 z"/></marker></defs>
      <g className="internal-edges">
        {internalNodes.filter((item) => item.kind === 'IMPORT').map((item) => <line key={item.id} x1={item.point.x + 150} y1={item.point.y + 25} x2={center.point.x} y2={center.point.y + 25} markerEnd={`url(#${markerId})`}/>)}
        {internalNodes.filter((item) => item.kind === 'FIELD' || item.kind === 'METHOD').map((item) => <line key={item.id} x1={center.point.x + 150} y1={center.point.y + 25} x2={item.point.x} y2={item.point.y + 25} markerEnd={`url(#${markerId})`}/>)}
        {activeMethodNode && internalNodes.filter((item) => item.kind === 'PARAMETER' || item.kind === 'LOCAL').map((item) => <line key={item.id} x1={activeMethodNode.point.x + 150} y1={activeMethodNode.point.y + 25} x2={item.point.x} y2={item.point.y + 25} markerEnd={`url(#${markerId})`}/>)}
      </g>
      {internalNodes.map((item) => <g key={item.id} className={`internal-node internal-${item.kind.toLowerCase()}${item.id === `method:${activeMethodIndex}` ? ' active' : ''}`} transform={`translate(${item.point.x} ${item.point.y})`} onClick={() => item.kind === 'METHOD' && setMethodName(methods[Number(item.id.split(':')[1])].name)} role={item.kind === 'METHOD' ? 'button' : undefined} tabIndex={item.kind === 'METHOD' ? 0 : undefined}>
        <rect width="150" height="50" rx="5"/><text className="node-kind" x="10" y="15">{item.kind}</text><text className="node-label" x="10" y="33">{item.label}</text><title>{item.detail}</title>
      </g>)}
      {imports.length === 0 && <text className="empty-column" x="30" y="35">No imports</text>}
      {fields.length === 0 && <text className="empty-column" x="480" y="35">No fields</text>}
      {methods.length === 0 && <text className="empty-column" x="705" y="35">No methods</text>}
      {variables.length === 0 && <text className="empty-column" x="930" y="35">No variables for selected method</text>}
    </svg>
  </div>
}

function NodeInspector({ node, incoming, outgoing, onInternals }: { node: KnowledgeNode; incoming: KnowledgeEdge[]; outgoing: KnowledgeEdge[]; onInternals: () => void }) {
  return <aside className="node-inspector" aria-label="Selected graph node details">
    <span className={`node-kind-pill kind-${node.kind.toLowerCase()}`}>{node.kind}</span>
    <h4>{node.label}</h4><code>{node.qualifiedName}</code>
    <p>{node.sourcePath}</p>
    <div className="inspector-counts"><span><strong>{node.imports.length}</strong> imports</span><span><strong>{node.fields.length}</strong> fields</span><span><strong>{node.methods.length}</strong> methods</span></div>
    {node.annotations.length > 0 && <div className="annotation-list">{node.annotations.map((item) => <span key={item}>@{item}</span>)}</div>}
    <button type="button" onClick={onInternals}>Explore class internals →</button>
    <div className="relationship-list"><strong>Connected through</strong>{[...outgoing, ...incoming].slice(0, 8).map((edge, index) => <span key={`${edge.source}:${edge.target}:${edge.kind}:${index}`} title={edge.evidence}>{edge.kind.replace('_', ' ')} · {edge.confidence}</span>)}</div>
  </aside>
}

export default function KnowledgeGraph({ graph, repositoryName }: { graph: RepositoryKnowledgeGraph; repositoryName: string }) {
  const markerPrefix = useId().replace(/:/g, '')
  const [mode, setMode] = useState<GraphMode>('architecture')
  const [filter, setFilter] = useState<GraphFilter>('layers')
  const [zoom, setZoom] = useState(1)
  const connectedIds = useMemo(() => new Set(graph.edges.flatMap((edge) => [edge.source, edge.target])), [graph.edges])
  const filteredNodes = useMemo(() => {
    const matched = graph.nodes.filter((node) => filter === 'all' || (filter === 'layers' ? architectureKinds.has(node.kind) : connectedIds.has(node.id)))
    return (matched.length > 0 ? matched : graph.nodes).slice(0, 80)
  }, [graph.nodes, connectedIds, filter])
  const visibleIds = useMemo(() => new Set(filteredNodes.map((node) => node.id)), [filteredNodes])
  const filteredEdges = useMemo(() => graph.edges.filter((edge) => visibleIds.has(edge.source) && visibleIds.has(edge.target) && importantEdges.has(edge.kind)), [graph.edges, visibleIds])
  const [selectedId, setSelectedId] = useState(graph.nodes.find((node) => node.kind === 'APPLICATION')?.id || graph.nodes[0]?.id || '')
  const selectedNode = graph.nodes.find((node) => node.id === selectedId) || graph.nodes[0]
  const selected = mode === 'architecture' && !filteredNodes.some((node) => node.id === selectedNode?.id)
    ? filteredNodes[0]
    : selectedNode

  if (!selected) return <div className="knowledge-empty">No class structure was extracted for this repository.</div>
  const selectNode = (id: string) => { setSelectedId(id); setMode('architecture') }
  return <section className="knowledge-graph" aria-label={`${repositoryName} knowledge graph`}>
    <div className="knowledge-heading"><div><span>Interactive code map</span><h4>Repository knowledge graph</h4><p>Solid links come from code structure. Dashed discovery links are framework inferences.</p></div><div className="graph-summary"><strong>{graph.nodes.length}</strong><span>types mapped</span><strong>{graph.edges.length}</strong><span>relationships</span></div></div>
    <div className="graph-toolbar">
      <div className="graph-tabs" role="tablist"><button type="button" className={mode === 'architecture' ? 'active' : ''} onClick={() => setMode('architecture')} role="tab" aria-selected={mode === 'architecture'}>Architecture</button><button type="button" className={mode === 'internals' ? 'active' : ''} onClick={() => setMode('internals')} role="tab" aria-selected={mode === 'internals'}>Class internals</button></div>
      {mode === 'architecture' && <label>Show<select value={filter} onChange={(event) => setFilter(event.target.value as GraphFilter)}><option value="connected">Connected types</option><option value="layers">Application layers</option><option value="all">All extracted types</option></select></label>}
      <div className="zoom-controls"><button type="button" onClick={() => setZoom((value) => Math.max(.7, value - .15))} aria-label="Zoom out">−</button><span>{Math.round(zoom * 100)}%</span><button type="button" onClick={() => setZoom((value) => Math.min(1.8, value + .15))} aria-label="Zoom in">+</button><button type="button" onClick={() => setZoom(1)}>Reset</button></div>
    </div>
    <div className="graph-body">
      <div className="graph-viewport">{mode === 'architecture' ? <ArchitectureGraph nodes={filteredNodes} edges={filteredEdges} selectedId={selected.id} onSelect={selectNode} markerId={`${markerPrefix}-architecture-arrow`} zoom={zoom}/> : <InternalsGraph key={selected.id} node={selected} markerId={`${markerPrefix}-internal-arrow`} zoom={zoom}/>}</div>
      <NodeInspector node={selected} outgoing={graph.edges.filter((edge) => edge.source === selected.id)} incoming={graph.edges.filter((edge) => edge.target === selected.id)} onInternals={() => setMode('internals')}/>
    </div>
    <div className="graph-legend">{kindOrder.filter((kind) => graph.nodes.some((node) => node.kind === kind)).map((kind) => <span key={kind}><i className={`kind-${kind.toLowerCase()}`}/>{kind.replace('_', ' ')}</span>)}<span><i className="inferred-edge"/>Inferred discovery</span></div>
    {graph.truncated && <p className="graph-limit">Showing the first {graph.nodes.length} of {graph.totalTypeCount} types. Refine package filtering in a later iteration.</p>}
  </section>
}
