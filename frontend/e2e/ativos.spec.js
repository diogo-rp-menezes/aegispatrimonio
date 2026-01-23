import { test, expect } from '@playwright/test';

test.describe('Asset CRUD Flow', () => {

  test.beforeEach(async ({ page }) => {
    // Login
    await page.goto('/login');
    await page.fill('input[type="email"]', 'admin@aegis.com');
    await page.fill('input[type="password"]', '123456');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/dashboard/);
  });

  test('should create, list, update and delete an asset', async ({ page }) => {
    // 1. Navigate to Assets
    await page.goto('/ativos');
    await expect(page.getByText('Inventário de Ativos')).toBeVisible();

    // 2. Create Asset
    await page.click('button[aria-label="Adicionar ativo"]');
    await expect(page.getByText('Novo Ativo')).toBeVisible();

    const assetName = `E2E Asset ${Date.now()}`;
    await page.fill('#nome', assetName);
    await page.fill('#numeroPatrimonio', `PAT-${Date.now()}`);

    // Select Type (first available option)
    await page.locator('#tipo').selectOption({ index: 1 });

    // Select Supplier (first available option)
    await page.locator('#fornecedor').selectOption({ index: 1 });

    // Select Location (first available option)
    await page.locator('#localizacao').selectOption({ index: 1 });

    await page.fill('#valor', '1500.50');
    await page.fill('#dataAquisicao', '2023-01-01');

    await page.click('button[type="submit"]');

    // 3. Verify in List
    await expect(page.getByText('Inventário de Ativos')).toBeVisible();
    await expect(page.getByText(assetName)).toBeVisible();

    // 4. Update Asset
    // Find row by text and click Edit
    // Using explicit wait for stability
    await page.waitForTimeout(1000);

    const row = page.getByRole('row').filter({ hasText: assetName });
    await row.getByRole('button', { name: 'Editar' }).click();

    await expect(page.getByText('Editar Ativo')).toBeVisible();
    const newName = assetName + ' Updated';
    await page.fill('#nome', newName);
    await page.click('button[type="submit"]');

    // 5. Verify Update
    await expect(page.getByText(newName)).toBeVisible();
    await expect(page.getByText(assetName)).not.toBeVisible();

    // 6. Delete Asset
    page.on('dialog', dialog => dialog.accept());

    const rowUpdated = page.getByRole('row').filter({ hasText: newName });
    await rowUpdated.getByRole('button', { name: 'Excluir' }).click();

    // 7. Verify Deletion
    // Wait for list refresh
    await page.waitForTimeout(1000);
    await expect(page.getByText(newName)).not.toBeVisible();
  });

});
