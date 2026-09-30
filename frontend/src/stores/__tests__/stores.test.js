import { describe, it, expect } from 'vitest'
import * as patrimonyStore from '../patrimony.js'
import * as storesIndex from '../index.js'

/**
 * Guarda de regressão: os arquivos de store hoje são stubs de 1 linha.
 * Se alguém adicionar lógica real aqui, este teste força a criar suíte própria.
 */
describe('stores (stubs atuais)', () => {
  it('patrimony.js não exporta lógica testável ainda', () => {
    expect(Object.keys(patrimonyStore).length).toBe(0)
  })

  it('index.js não exporta lógica testável ainda', () => {
    expect(Object.keys(storesIndex).length).toBe(0)
  })
})
