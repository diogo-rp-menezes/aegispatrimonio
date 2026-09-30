import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import Sidebar from '../../components/Sidebar.vue'

/**
 * Smoke test do Sidebar: montagem, filtro por role e toggle de colapso.
 * Router real em memória para que router-link/useRoute funcionem.
 */
const mountSidebar = async (roles = []) => {
  localStorage.setItem('userRoles', JSON.stringify(roles))
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/dashboard', component: { template: '<div />' } },
      { path: '/ativos', component: { template: '<div />' } },
      { path: '/system-health', component: { template: '<div />' } },
      { path: '/admin', component: { template: '<div />' } },
    ],
  })
  await router.push('/dashboard')
  await router.isReady()
  const wrapper = mount(Sidebar, { global: { plugins: [router] } })
  return { wrapper, router }
}

beforeEach(() => {
  localStorage.clear()
})

describe('Sidebar', () => {
  it('monta e renderiza o logo Aegis', async () => {
    const { wrapper } = await mountSidebar()
    expect(wrapper.find('.sidebar-logo').exists()).toBe(true)
    expect(wrapper.text()).toContain('Aegis')
  })

  it('filtra itens com role quando o usuário não tem ROLE_ADMIN', async () => {
    const { wrapper } = await mountSidebar(['ROLE_USER'])
    const titles = wrapper.findAll('.nav-item').map((n) => n.text())
    expect(titles.join(' ')).not.toContain('Saúde do Sistema')
    expect(titles.join(' ')).not.toContain('Administração')
    expect(titles.join(' ')).toContain('Dashboard')
  })

  it('exibe itens de admin para ROLE_ADMIN', async () => {
    const { wrapper } = await mountSidebar(['ROLE_ADMIN'])
    expect(wrapper.text()).toContain('Saúde do Sistema')
    expect(wrapper.text()).toContain('Administração')
  })

  it('toggle de colapso emite evento e oculta textos', async () => {
    const { wrapper } = await mountSidebar()
    expect(wrapper.find('.sidebar').classes()).not.toContain('collapsed')

    await wrapper.find('.toggle-btn').trigger('click')

    expect(wrapper.find('.sidebar').classes()).toContain('collapsed')
    expect(wrapper.emitted('toggle')).toEqual([[true]])
    // Textos do menu ficam ocultos quando colapsado
    expect(wrapper.find('.sidebar-subtitle').exists()).toBe(false)
  })
})
