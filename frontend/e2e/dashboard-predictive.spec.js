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
        ativosPorStatus: [
          { label: 'ATIVO', value: 100 }
        ],
        ativosPorTipo: [
          { label: 'Notebook', value: 50 }
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
    await expect(page.getByText('Distribuição de Risco')).toBeVisible();

    // Verify the presence of the 3rd chart (Doughnut)
    // The dashboard already had 2 charts, we added a 3rd one.
    const charts = page.locator('canvas');
    await expect(charts).toHaveCount(3);

    // Verify the new predictive stats are displayed in cards as well
    const criticalCard = page.locator('.card', { hasText: 'Críticos (< 7 dias)' });
    await expect(criticalCard.locator('h3')).toHaveText('5 ativos');
  });

});
