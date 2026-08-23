import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import App from './App'

describe('EKC console', () => {
  afterEach(() => {
    cleanup()
    vi.restoreAllMocks()
    window.localStorage.clear()
  })

  it('validates repository URLs before submitting', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(JSON.stringify({ status: 'UP' }), { status: 200 }))
    render(<App />)
    fireEvent.change(screen.getByLabelText('Repository URL'), { target: { value: 'not-a-url' } })
    fireEvent.click(screen.getByRole('button', { name: 'Analyze repository' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('Enter a complete repository URL')
  })

  it('submits a repository and adds it to history', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (input, init) => {
      if (String(input).includes('/analyze') && init?.method === 'POST') {
        return new Response(JSON.stringify({
          status: 'ANALYZED',
          message: 'Repository analyzed successfully. No target build was executed.',
          durationMs: 42,
          analysis: {
            repositoryName: 'platform', defaultBranch: 'main', sourceFiles: 3, parsedFiles: 3,
            extractedFiles: 3, packages: 1, imports: 4, types: 2, fields: 1, methods: 5,
            annotations: 1, diagnostics: [],
          },
        }), { status: 200 })
      }
      return new Response(JSON.stringify({ status: 'UP' }), { status: 200 })
    })
    render(<App />)
    fireEvent.change(screen.getByLabelText('Repository URL'), { target: { value: 'https://github.com/acme/platform.git' } })
    fireEvent.click(screen.getByRole('button', { name: 'Analyze repository' }))
    await waitFor(() => expect(screen.getByText('platform')).toBeInTheDocument())
    expect(screen.getByText('Analyzed', { selector: '.run-status' })).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'View platform details' }))
    expect(screen.getByLabelText('Repository analysis summary')).toHaveTextContent('Sources')
    expect(screen.getByLabelText('Repository analysis summary')).toHaveTextContent('Methods')
  })
})
