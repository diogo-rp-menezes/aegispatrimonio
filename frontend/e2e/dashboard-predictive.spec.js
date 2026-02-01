import { test, expect } from '@playwright/test';

test.describe('Dashboard Predictive Visualization (SOTA)', () => {

  test.beforeEach(async ({ page }) => {
    // 1. Mock Login
    await page.route('**/auth/login', async route => {
        await route.fulfill({
            json: {
                token: 'fake-jwt-token-sota-mvp',
                filiais: [
                    { id: 1, nome: 'Matriz', codigo: 'MTZ' }
                ]
            }
        });
    });

    // 2. Mock Stats with Predictive Data
    await page.route('**/dashboard/stats', async route => {
      const json = {
        totalAtivos: 1000,
        ativosEmManutencao: 10,
        valorTotal: 100000.00,
        totalLocalizacoes: 2,
        predicaoCritica: 5,
        predicaoAlerta: 10,
        predicaoSegura: 985,
        predicaoIndeterminada: 5,
        ativosPorStatus: [
          { label: 'ATIVO', value: 100 }
        ],
        ativosPorTipo: [
          { label: 'Notebook', value: 50 }
        ],
        failureTrend: [
          { label: 'Semana 1', value: 2 },
          { label: 'Semana 2', value: 5 },
          { label: 'Semana 3', value: 1 },
          { label: 'Semana 4', value: 0 },
          { label: 'Semana 5', value: 3 },
          { label: 'Semana 6', value: 1 },
          { label: 'Semana 7', value: 4 },
          { label: 'Semana 8', value: 2 }
        ]
      };
      await route.fulfill({ json });
    });

    // 3. Mock Recent Assets
    await page.route('**/ativos*', async route => {
        const json = { content: [] };
        await route.fulfill({ json });
    });

    // 4. Login and go to Dashboard
    await page.goto('/login');
    await page.fill('#email', 'admin@aegis.com');
    await page.fill('#password', '123456');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/dashboard/);
  });

  test('Deve exibir o gráfico de Distribuição de Risco', async ({ page }) => {
    // Verify the new section title
    await expect(page.getByText('Distribuição de Saúde (Preditiva)')).toBeVisible();

    // Verify the presence of all 4 charts (Status, Type, Trend, Risk)
    const charts = page.locator('canvas');
    await expect(charts).toHaveCount(4);

    // Verify the Trend Chart title
    await expect(page.getByText('Tendência de Falhas')).toBeVisible();

    // Verify the new predictive stats are displayed in cards as well
    const criticalCard = page.locator('.card', { hasText: 'Críticos (< 7 dias)' });
    await expect(criticalCard.locator('h3')).toHaveText('5 ativos');

    const unknownCard = page.locator('.card', { hasText: 'Indeterminado' });
    await expect(unknownCard.locator('h3')).toHaveText('5 ativos');
  });

  test('Deve navegar para lista de ativos filtrada ao clicar no card', async ({ page }) => {
    // Click on the "Indeterminado" card
    const unknownCard = page.locator('.card', { hasText: 'Indeterminado' });
    await unknownCard.click();

    // Verify URL parameters
    await expect(page).toHaveURL(/.*\/ativos\?health=INDETERMINADO/);
  });

});
