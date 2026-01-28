import { test, expect } from '@playwright/test';

test.describe('Gestão de Manutenções', () => {

  // Fake DB
  let db = {
    manutencoes: [],
    ativos: [{id: 1, nome: 'Notebook Dell', numeroPatrimonio: 'PAT-001'}]
  };

  test.beforeEach(async ({ page }) => {
    db.manutencoes = [];

    // Mock Login
    await page.route('**/auth/login', async route => {
      await route.fulfill({
        json: {
          token: 'fake-jwt-token',
          filiais: [{ id: 1, nome: 'Matriz', codigo: 'MTZ' }]
        }
      });
    });

    // Mock Dashboard and Alerts (ignored for this test but needed for layout)
    await page.route('**/dashboard/stats', async route => route.fulfill({ json: {} }));
    await page.route('**/alerts/recent', async route => route.fulfill({ json: [] }));

    // Mock Manutencoes API
    // GET /manutencoes
    await page.route('**/manutencoes?*', async route => {
        if (route.request().method() === 'GET') {
            await route.fulfill({
                json: {
                    content: db.manutencoes,
                    totalElements: db.manutencoes.length,
                    totalPages: 1,
                    size: 10,
                    number: 0
                }
            });
        } else {
            await route.continue();
        }
    });

    // POST /manutencoes (Create)
    await page.route('**/manutencoes', async route => {
        if (route.request().method() === 'POST') {
            const data = await route.request().postDataJSON();
            const newId = db.manutencoes.length + 1;
            const newManutencao = {
                id: newId,
                ...data,
                ativoNome: 'Notebook Dell', // Mocked name
                ativoNumeroPatrimonio: 'PAT-001',
                solicitanteNome: 'Admin User',
                status: 'SOLICITADA',
                dataSolicitacao: new Date().toISOString()
            };
            db.manutencoes.push(newManutencao);
            await route.fulfill({ json: newManutencao });
        } else {
            await route.continue();
        }
    });

    // Login
    await page.goto('/login');
    await page.fill('#email', 'admin@aegis.com');
    await page.fill('#password', '123456');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/dashboard/);
  });

  test('Deve criar e listar uma solicitação de manutenção', async ({ page }) => {
    // Navigate to Maintenance
    await page.click('text=Manutenções'); // Sidebar link
    await expect(page).toHaveURL(/\/manutencoes/);

    // Open Form
    await page.click('button:has-text("Nova Solicitação")');

    // Wait for form header
    await expect(page.locator('h5:has-text("Nova Solicitação de Manutenção")')).toBeVisible();

    // Fill Form
    // Using placeholders or labels to find inputs since I used generic inputs in Vue
    await page.fill('input[placeholder="ID do Ativo"]', '1');
    await page.fill('input[placeholder="ID do Funcionário Solicitante"]', '1');

    // Select Type
    await page.selectOption('label:has-text("Tipo de Manutenção") + select', 'PREVENTIVA');

    // Description
    await page.fill('textarea', 'Limpeza interna e troca de pasta térmica');

    // Save
    await page.click('button:has-text("Salvar Solicitação")');

    // Verify List
    await expect(page.locator('text=Notebook Dell')).toBeVisible();
    await expect(page.locator('td:has-text("PREVENTIVA")')).toBeVisible();
    await expect(page.locator('span.badge:has-text("SOLICITADA")')).toBeVisible();
  });
});
