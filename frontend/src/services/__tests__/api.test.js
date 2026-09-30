import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { clearSession, handleResponse, request, logout } from '../api.js'

/**
 * Mocks de fronteira externa: localStorage, fetch e window.location.
 * A lógica de api.js (headers, parsing, redirecionamento) é o que se testa.
 */
const assignMock = vi.fn()

beforeEach(() => {
  vi.stubGlobal('fetch', vi.fn())
  Object.defineProperty(window, 'location', {
    value: { ...window.location, assign: assignMock, pathname: '/' },
    writable: true,
  })
  localStorage.clear()
  assignMock.mockClear()
  window.fetch.mockClear()
})

afterEach(() => {
  vi.unstubAllGlobals()
  vi.restoreAllMocks()
})

const jsonResponse = (body, status = 200) => ({
  ok: status >= 200 && status < 300,
  status,
  json: async () => body,
  text: async () => JSON.stringify(body),
})

describe('clearSession', () => {
  it('remove todas as chaves de sessão do localStorage', () => {
    localStorage.setItem('authToken', 'tok')
    localStorage.setItem('userRoles', '["ADMIN"]')
    localStorage.setItem('allowedFiliais', '[1]')
    localStorage.setItem('currentFilial', '1')
    localStorage.setItem('outraChave', 'mantida')

    clearSession()

    expect(localStorage.getItem('authToken')).toBeNull()
    expect(localStorage.getItem('userRoles')).toBeNull()
    expect(localStorage.getItem('allowedFiliais')).toBeNull()
    expect(localStorage.getItem('currentFilial')).toBeNull()
    // Chaves fora da sessão não são tocadas
    expect(localStorage.getItem('outraChave')).toBe('mantida')
  })
})

describe('handleResponse', () => {
  it('retorna o JSON parseado em resposta 200', async () => {
    const data = await handleResponse(jsonResponse({ id: 1 }))
    expect(data).toEqual({ id: 1 })
  })

  it('retorna null em 204 No Content', async () => {
    const data = await handleResponse({ ok: true, status: 204, json: async () => { throw new Error('no body') } })
    expect(data).toBeNull()
  })

  it('em 401 limpa a sessão e redireciona para /login', async () => {
    localStorage.setItem('authToken', 'expirado')
    const res = jsonResponse({ detail: 'Sessão expirada' }, 401)

    await expect(handleResponse(res)).rejects.toThrow('Sessão expirada')
    expect(localStorage.getItem('authToken')).toBeNull()
    expect(assignMock).toHaveBeenCalledWith('/login')
  })

  it('em 401 no /login não redireciona (evita loop)', async () => {
    window.location.pathname = '/login'
    const res = jsonResponse({ detail: 'Credenciais inválidas' }, 401)

    await expect(handleResponse(res)).rejects.toThrow('Credenciais inválidas')
    expect(assignMock).not.toHaveBeenCalled()
  })

  it('extrai detail de ProblemDetail RFC 7807', async () => {
    const res = jsonResponse({ type: 'about:blank', title: 'Recurso não encontrado', detail: 'Ativo 99 não existe' }, 404)
    await expect(handleResponse(res)).rejects.toThrow('Ativo 99 não existe')
  })

  it('extrai title quando detail está ausente', async () => {
    const res = jsonResponse({ title: 'Conflito de estado' }, 409)
    await expect(handleResponse(res)).rejects.toThrow('Conflito de estado')
  })

  it('usa o corpo bruto como mensagem quando não é JSON', async () => {
    const res = { ok: false, status: 500, text: async () => 'Internal Server Error', json: async () => { throw new Error() } }
    await expect(handleResponse(res)).rejects.toThrow('Internal Server Error')
  })

  it('usa fallback com status quando corpo é vazio', async () => {
    const res = { ok: false, status: 503, text: async () => '', json: async () => { throw new Error() } }
    await expect(handleResponse(res)).rejects.toThrow('Erro na requisição (HTTP 503)')
  })
})

describe('request', () => {
  it('injeta Authorization Bearer e X-Filial-ID do localStorage', async () => {
    localStorage.setItem('authToken', 'meu-jwt')
    localStorage.setItem('currentFilial', '7')
    window.fetch.mockResolvedValue(jsonResponse({ ok: true }))

    await request('/ativos')

    const [url, init] = window.fetch.mock.calls[0]
    expect(url).toBe('/ativos')
    expect(init.headers['Authorization']).toBe('Bearer meu-jwt')
    expect(init.headers['X-Filial-ID']).toBe('7')
    expect(init.headers['Content-Type']).toBe('application/json')
  })

  it('não injeta headers de auth quando não há sessão', async () => {
    window.fetch.mockResolvedValue(jsonResponse({ ok: true }))
    await request('/publico')
    const init = window.fetch.mock.calls[0][1]
    expect(init.headers['Authorization']).toBeUndefined()
    expect(init.headers['X-Filial-ID']).toBeUndefined()
  })

  it('serializa body objeto como JSON string', async () => {
    window.fetch.mockResolvedValue(jsonResponse({ ok: true }))
    const body = { nome: 'Notebook', valor: 10 }

    await request('/ativos', { method: 'POST', body })

    const init = window.fetch.mock.calls[0][1]
    expect(init.body).toBe(JSON.stringify(body))
    expect(init.headers['Content-Type']).toBe('application/json')
  })

  it('não serializa nem define Content-Type para FormData', async () => {
    window.fetch.mockResolvedValue(jsonResponse({ ok: true }))
    const fd = new FormData()
    fd.append('file', 'x')

    await request('/upload', { method: 'POST', body: fd })

    const init = window.fetch.mock.calls[0][1]
    expect(init.body).toBe(fd)
    expect(init.headers['Content-Type']).toBeUndefined()
  })

  it('anexa params como query string', async () => {
    window.fetch.mockResolvedValue(jsonResponse([]))
    await request('/ativos', { params: { page: 2, size: 10 } })
    expect(window.fetch.mock.calls[0][0]).toBe('/ativos?page=2&size=10')
  })

  it('propaga o resultado via handleResponse (204 → null)', async () => {
    window.fetch.mockResolvedValue({ ok: true, status: 204, json: async () => { throw new Error() } })
    const data = await request('/ativos/1', { method: 'DELETE' })
    expect(data).toBeNull()
  })

  it('com responseType blob retorna o blob sem passar por handleResponse', async () => {
    const blob = new Blob(['pdf'])
    window.fetch.mockResolvedValue({ ok: true, status: 200, blob: async () => blob })

    const result = await request('/relatorios/1', { responseType: 'blob' })

    expect(result).toBe(blob)
  })
})

describe('logout', () => {
  it('chama /auth/logout via POST e limpa a sessão', async () => {
    localStorage.setItem('authToken', 'tok')
    window.fetch.mockResolvedValue({ ok: true, status: 204, json: async () => null })

    await logout()

    expect(window.fetch.mock.calls[0][0]).toBe('/auth/logout')
    expect(window.fetch.mock.calls[0][1].method).toBe('POST')
    expect(localStorage.getItem('authToken')).toBeNull()
    expect(assignMock).toHaveBeenCalledWith('/login')
  })

  it('fire-and-forget: limpa sessão e redireciona mesmo se o request falhar', async () => {
    localStorage.setItem('authToken', 'tok')
    window.fetch.mockRejectedValue(new Error('network down'))
    const debugSpy = vi.spyOn(console, 'debug').mockImplementation(() => {})

    await expect(logout()).resolves.toBeUndefined()

    expect(localStorage.getItem('authToken')).toBeNull()
    expect(assignMock).toHaveBeenCalledWith('/login')
    debugSpy.mockRestore()
  })
})
