import { test, expect } from '@playwright/test';

test.describe('Authentication Flow', () => {

  test('should login successfully with valid credentials', async ({ page }) => {
    // Listen to console events
    page.on('console', msg => console.log('PAGE LOG:', msg.text()));

    // Listen to network failures
    page.on('requestfailed', request => {
      console.log(`REQ FAILED: ${request.url()} - ${request.failure().errorText}`);
    });

    page.on('response', async response => {
      if (response.url().includes('/auth/login') && response.status() >= 400) {
        console.log(`LOGIN REQ ERROR: ${response.url()} - ${response.status()}`);
        console.log(await response.text());
      }
    });

    await page.goto('/login');
    await page.fill('input[type="email"]', 'admin@aegis.com');
    await page.fill('input[type="password"]', '123456');
    await page.click('button[type="submit"]');

    // Allow some time for network request
    await page.waitForTimeout(2000);

    // Check if error message appears
    const errorAlert = page.locator('.alert-danger');
    if (await errorAlert.isVisible()) {
        const errorText = await errorAlert.innerText();
        console.log('LOGIN ERROR ALERT:', errorText);
    }

    await expect(page).toHaveURL(/\/dashboard/);
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
    await expect(page.getByText('Total de Ativos')).toBeVisible();

    const token = await page.evaluate(() => localStorage.getItem('authToken'));
    expect(token).toBeTruthy();
  });

  test('should show error with invalid credentials', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', 'admin@aegis.com');
    await page.fill('input[type="password"]', 'wrongpassword');
    await page.click('button[type="submit"]');

    const alert = page.locator('.alert-danger');
    await expect(alert).toBeVisible();
    await expect(alert).toContainText('Falha no login');
  });

});
