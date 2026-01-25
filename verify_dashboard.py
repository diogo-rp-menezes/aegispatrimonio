from playwright.sync_api import sync_playwright, expect
import os

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()

    # Mock authentication
    context.add_init_script("localStorage.setItem('authToken', 'fake-token');")

    page = context.new_page()

    # Mock API responses
    page.route("**/api/v1/dashboard/stats", lambda route: route.fulfill(
        status=200,
        content_type="application/json",
        body='{"totalAtivos":100,"ativosEmManutencao":5,"valorTotal":50000.00,"totalLocalizacoes":10,"predicaoCritica":2,"predicaoAlerta":3,"predicaoSegura":90,"predicaoIndeterminada":5}'
    ))

    page.route("**/api/v1/ativos?size=5&sort=id,desc", lambda route: route.fulfill(
        status=200,
        content_type="application/json",
        body='{"content":[{"id":101,"nome":"Laptop Gamer","tipoAtivoNome":"Notebook","status":"ATIVO"}]}'
    ))

    # Mocking the new endpoint
    page.route("**/api/v1/alertas/recent", lambda route: route.fulfill(
        status=200,
        content_type="application/json",
        body='[{"id":1,"ativoId":101,"ativoNome":"Laptop Gamer","tipo":"CRITICO","titulo":"Disco Cheio","mensagem":"O disco está 99% cheio.","dataCriacao":"2024-05-21T10:00:00","lido":false}]'
    ))

    try:
        page.goto("http://localhost:5173/dashboard")

        # Verify Dashboard Loaded
        expect(page.get_by_role("heading", name="Dashboard")).to_be_visible()

        # Verify Alerts Section
        expect(page.get_by_text("Alertas do Sistema")).to_be_visible()

        # Verify Alert Content (using the new field usage)
        expect(page.get_by_text("Disco Cheio")).to_be_visible()
        expect(page.get_by_text("Laptop Gamer")).to_be_visible() # This proves `alert.ativoNome` works

        # Take screenshot
        os.makedirs("/home/jules/verification", exist_ok=True)
        page.screenshot(path="/home/jules/verification/dashboard_alerts.png")
        print("Verification successful!")

    except Exception as e:
        print(f"Verification failed: {e}")
        page.screenshot(path="/home/jules/verification/failure.png")
        raise e

    browser.close()

with sync_playwright() as playwright:
    run(playwright)
