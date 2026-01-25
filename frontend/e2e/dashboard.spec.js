import { test, expect } from '@playwright/test';

test.describe('Dashboard Analytics & SOTA Features', () => {

  test.beforeEach(async ({ page }) => {
    // 1. Mock Login (Must include token AND filiais)
    await page.route('**/auth/login', async route => {
        await route.fulfill({
            json: {
                token: 'fake-jwt-token-sota-mvp',
                filiais: [
                    { id: 1, nome: 'Matriz', codigo: 'MTZ' },
                    { id: 2, nome: 'Filial SP', codigo: 'SP' }
                ]
            }
        });
    });

    // 2. Mock the SOTA Dashboard Stats
    await page.route('**/dashboard/stats', async route => {
      const json = {
        totalAtivos: 1250,
        ativosEmManutencao: 15,
        valorTotal: 500000.00,
        totalLocalizacoes: 4,
        predicaoCritica: 7, // Critical for SOTA check
        predicaoAlerta: 20,
        predicaoSegura: 1223,
        ativosPorStatus: [
          { label: 'ATIVO', value: 1200 },
          { label: 'EM_MANUTENCAO', value: 15 },
          { label: 'BAIXADO', value: 35 }
        ],
        ativosPorTipo: [
          { label: 'Notebook', value: 800 },
          { label: 'Monitor', value: 450 }
        ]
      };
      await route.fulfill({ json });
    });

    // 3. Mock Recent Assets (Loose matcher to catch query params)
    await page.route('**/ativos*', async route => {
        // Only mock if it's the dashboard list (check params if needed, or just return mock)
        const json = {
            content: [
                { id: 101, nome: 'Dell Latitude 5420', tipoAtivoNome: 'Notebook', status: 'ATIVO' },
                { id: 102, nome: 'Dell P2419H', tipoAtivoNome: 'Monitor', status: 'ATIVO' }
            ]
        };
        await route.fulfill({ json });
    });

    // 4. Login Flow
    await page.goto('/login');
    await page.fill('#email', 'admin@aegis.com');
    await page.fill('#password', '123456');
    await page.click('button[type="submit"]');

    // Wait for Dashboard URL (Verification of successful login)
    await expect(page).toHaveURL(/\/dashboard/);
  });

  test('Deve renderizar os KPIs de Manutenção Preditiva (SOTA)', async ({ page }) => {
    // Verify "Total de Ativos"
    const totalCard = page.locator('.card', { hasText: 'Total de Ativos' });
    await expect(totalCard.locator('h4')).toHaveText('1250');

    // Verify "Críticos (< 7 dias)" - The Predictive Maintenance Value Prop
    const criticalCard = page.locator('.card', { hasText: 'Críticos (< 7 dias)' });
    await expect(criticalCard.locator('h3')).toHaveText('7 ativos');
    await expect(criticalCard.locator('i.bi-exclamation-triangle-fill')).toBeVisible();

    // Verify "Valor Total" formatting (Matches partial because of currency symbol/spacing)
    const valueCard = page.locator('.card', { hasText: 'Valor Total' });
    await expect(valueCard.locator('h4')).toContainText('500.000,00');
  });

  test('Deve renderizar os gráficos de Analytics', async ({ page }) => {
    // Check if Canvas elements exist for Chart.js
    const charts = page.locator('canvas');
    await expect(charts).toHaveCount(3); // Doughnut + Bar + Doughnut (Predictive)

    // Verify Titles
    await expect(page.getByText('Ativos por Status')).toBeVisible();
    await expect(page.getByText('Ativos por Tipo')).toBeVisible();
  });

  test('Deve listar os ativos recentes', async ({ page }) => {
      const table = page.locator('table');
      await expect(table).toBeVisible();
      await expect(page.getByText('Dell Latitude 5420')).toBeVisible();
  });

});
