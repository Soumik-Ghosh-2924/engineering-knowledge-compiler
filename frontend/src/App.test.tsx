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
    fireEvent.click(screen.getByRole('button', { name: 'Compile repository' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('Enter a complete repository URL')
  })

  it('submits a repository and adds it to history', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(async (input, init) => {
      if (String(input).includes('/compile') && init?.method === 'POST') {
        return new Response(JSON.stringify({ status: 'ACCEPTED', message: 'Repository compiled successfully.' }), { status: 202 })
      }
      return new Response(JSON.stringify({ status: 'UP' }), { status: 200 })
    })
    render(<App />)
    fireEvent.change(screen.getByLabelText('Repository URL'), { target: { value: 'https://github.com/acme/platform.git' } })
    fireEvent.click(screen.getByRole('button', { name: 'Compile repository' }))
    await waitFor(() => expect(screen.getByText('platform')).toBeInTheDocument())
    expect(screen.getByText('Accepted', { selector: '.run-status' })).toBeInTheDocument()
  })
})
