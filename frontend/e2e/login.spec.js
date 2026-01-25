import { test, expect } from '@playwright/test';

test.describe('Authentication Flow', () => {

  test('should login successfully with valid credentials', async ({ page }) => {
    // Mock Login Success
    await page.route('**/auth/login', async route => {
      const json = {
        token: 'fake-jwt-token',
        filiais: [
          { id: 1, nome: 'Matriz', codigo: 'MTZ' }
        ]
      };
      await route.fulfill({ json });
    });

    // Mock Dashboard Stats (to prevent network errors on redirect)
    await page.route('**/dashboard/stats', async route => {
       await route.fulfill({ json: { totalAtivos: 100, predicaoCritica: 0 } });
    });

    // Mock Alerts (to prevent network errors on redirect)
    await page.route('**/alerts/recent', async route => {
        await route.fulfill({ json: [] });
    });

    // Mock Recent Assets
    await page.route('**/ativos*', async route => {
         await route.fulfill({ json: { content: [] } });
    });

    await page.goto('/login');
    await page.fill('input[type="email"]', 'admin@aegis.com');
    await page.fill('input[type="password"]', '123456');
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL(/\/dashboard/);
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
    await expect(page.getByText('Total de Ativos')).toBeVisible();

    const token = await page.evaluate(() => localStorage.getItem('authToken'));
    expect(token).toBeTruthy();
  });

  test('should show error with invalid credentials', async ({ page }) => {
    // Mock Login Failure
    await page.route('**/auth/login', async route => {
      await route.fulfill({
        status: 401,
        json: { message: 'Bad credentials' }
      });
    });

    await page.goto('/login');
    await page.fill('input[type="email"]', 'admin@aegis.com');
    await page.fill('input[type="password"]', 'wrongpassword');
    await page.click('button[type="submit"]');

    const alert = page.locator('.alert-danger');
    await expect(alert).toBeVisible();
    await expect(alert).toContainText('Falha no login');
  });

});
