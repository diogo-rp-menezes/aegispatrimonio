import { test, expect } from '@playwright/test';

test.describe('Gerenciamento de Ativos', () => {
  test.beforeEach(async ({ page }) => {
    // Debug console
    page.on('console', msg => console.log(`BROWSER LOG: ${msg.text()}`));
    page.on('pageerror', exception => console.log(`BROWSER ERROR: ${exception}`));

    // Login
    await page.goto('/login');
    await page.fill('#email', 'admin@aegis.com');
    await page.fill('#password', '123456');
    await page.click('button[type="submit"]');

    // Wait for navigation
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });
  });

  test('Deve criar e listar um ativo', async ({ page }) => {
    // 1. Criar
    await page.goto('/ativos');
    await page.click('button:has-text("Adicionar Ativo")');

    const testName = `Ativo Teste ${Date.now()}`;
    await page.fill('#nome', testName);

    // Select Tipo (Index 1)
    await page.locator('#tipo').selectOption({ index: 1 });

    // Select Localizacao (Index 1) if available
    const locOptions = await page.locator('#localizacao option').count();
    if (locOptions > 1) {
        await page.locator('#localizacao').selectOption({ index: 1 });
    }

    // Select Fornecedor (Index 1)
    await page.locator('#fornecedor').selectOption({ index: 1 });

    // Fill Patrimonio
    await page.fill('#numeroPatrimonio', `PAT-${Date.now()}`);

    // Fill Data Aquisicao
    await page.fill('#dataAquisicao', new Date().toISOString().split('T')[0]);

    await page.fill('#valor', '1500.00');
    await page.click('button:has-text("Salvar")');

    // 2. Listar (Verify creation)
    await expect(page.locator(`text=${testName}`)).toBeVisible();

    // 3. Editar
    await page.locator(`tr:has-text("${testName}")`).getByTitle('Editar').click();

    // Wait for form to load data
    await expect(page.locator('#nome')).toHaveValue(testName);

    const newName = `${testName} Editado`;
    await page.fill('#nome', newName);
    await page.click('button:has-text("Salvar")');

    // Verify update
    await expect(page.locator(`text=${newName}`)).toBeVisible();

    // 4. Excluir (Baixar)
    await page.locator(`tr:has-text("${newName}")`).getByTitle('Detalhes').click();

    // Handle confirm dialog
    page.on('dialog', dialog => dialog.accept());

    await page.click('button:has-text("Baixar Item")');

    // Should return to list
    await expect(page).toHaveURL(/\/ativos/);
    await expect(page.locator(`text=${newName}`)).not.toBeVisible();
  });
});
