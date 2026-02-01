from playwright.sync_api import sync_playwright, expect
import os

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()
    page = context.new_page()

    # Mock Login Response for ADMIN
    page.route("**/api/v1/auth/login", lambda route: route.fulfill(
        status=200,
        content_type="application/json",
        body='{"token": "fake-admin-token", "filiais": [{"id":1, "nome":"Matriz"}], "roles": ["ROLE_ADMIN"]}'
    ))

    # Mock System Health Data
    page.route("**/api/v1/health-check/system/last", lambda route: route.fulfill(
        status=200,
        content_type="application/json",
        body='{"host": "server-01", "cpuUsage": 0.45, "memFreePercent": 0.30, "disks": "C: 50% free; D: 20% free", "nets": "eth0: UP", "createdAt": "2024-05-21T10:00:00"}'
    ))

    # Mock Dashboard Stats (needed for redirect or initial load)
    page.route("**/api/v1/dashboard/stats", lambda route: route.fulfill(
        status=200, body='{}'
    ))

    # Mock Alerts (needed for dashboard)
    page.route("**/api/v1/alertas/recent", lambda route: route.fulfill(
        status=200, body='[]'
    ))

    # Mock Ativos (needed for dashboard)
    page.route("**/api/v1/ativos*", lambda route: route.fulfill(
        status=200, body='{"content":[]}'
    ))


    # 1. Login Flow (Admin)
    print("Testing Admin Login...")
    page.goto("http://localhost:5173/login")
    page.fill("#email", "admin@aegis.com")
    page.fill("#password", "admin123")
    page.click("button[type=submit]")

    # Wait for navigation to dashboard
    page.wait_for_url("**/dashboard")

    # Check Sidebar for "Saúde do Sistema"
    expect(page.get_by_text("Saúde do Sistema")).to_be_visible()

    # Navigate to System Health
    page.click("text=Saúde do Sistema")

    # Check Content
    expect(page.get_by_text("Informações do Host")).to_be_visible()
    expect(page.get_by_text("server-01")).to_be_visible()
    expect(page.get_by_text("45.0%")).to_be_visible() # CPU Usage 0.45 formatted

    # Screenshot Admin View
    os.makedirs("/home/jules/verification", exist_ok=True)
    page.screenshot(path="/home/jules/verification/system_health_admin.png")
    print("Admin verification passed. Screenshot saved.")

    # 2. Test User Login (No Admin Role)
    print("Testing User Login...")
    context.close()
    context = browser.new_context()
    page = context.new_page()

    # Mock Login Response for USER
    page.route("**/api/v1/auth/login", lambda route: route.fulfill(
        status=200,
        content_type="application/json",
        body='{"token": "fake-user-token", "filiais": [{"id":1, "nome":"Matriz"}], "roles": ["ROLE_USER"]}'
    ))

    # Mock other endpoints again for new context
    page.route("**/api/v1/dashboard/stats", lambda route: route.fulfill(status=200, body='{}'))
    page.route("**/api/v1/alertas/recent", lambda route: route.fulfill(status=200, body='[]'))
    page.route("**/api/v1/ativos*", lambda route: route.fulfill(status=200, body='{"content":[]}'))

    page.goto("http://localhost:5173/login")
    page.fill("#email", "user@aegis.com")
    page.fill("#password", "user123")
    page.click("button[type=submit]")

    page.wait_for_url("**/dashboard")

    # Check Sidebar - Should NOT see "Saúde do Sistema"
    expect(page.get_by_text("Saúde do Sistema")).not_to_be_visible()

    # Try direct navigation
    page.goto("http://localhost:5173/system-health")
    # Should redirect to dashboard (as per my guard logic)
    page.wait_for_url("**/dashboard")

    print("User verification passed (Access Denied).")

    browser.close()

if __name__ == "__main__":
    with sync_playwright() as playwright:
        run(playwright)
