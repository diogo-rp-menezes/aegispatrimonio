from playwright.sync_api import sync_playwright, expect
import json

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()
    page = context.new_page()

    # Mock Data
    ativo_mock = {
        "id": 1,
        "nome": "Notebook Dell G15",
        "numeroPatrimonio": "PAT-1001",
        "dataAquisicao": "2023-01-01",
        "valorAquisicao": 5000.0,
        "previsaoEsgotamentoDisco": "2023-12-31",
        "detalheHardware": {
            "computerName": "DESKTOP-01",
            "osName": "Windows",
            "osVersion": "11"
        }
    }

    history_mock = [
        {"dataRegistro": "2023-01-01T10:00:00", "componente": "DISK:0", "valor": 500.0, "metrica": "FREE_SPACE_GB"},
        {"dataRegistro": "2023-02-01T10:00:00", "componente": "DISK:0", "valor": 450.0, "metrica": "FREE_SPACE_GB"},
        {"dataRegistro": "2023-03-01T10:00:00", "componente": "DISK:0", "valor": 400.0, "metrica": "FREE_SPACE_GB"},
        {"dataRegistro": "2023-04-01T10:00:00", "componente": "DISK:0", "valor": 350.0, "metrica": "FREE_SPACE_GB"},
    ]

    # Intercept API calls
    page.route("**/api/v1/ativos/1", lambda route: route.fulfill(
        status=200,
        content_type="application/json",
        body=json.dumps(ativo_mock)
    ))

    page.route("**/api/v1/ativos/1/health-history", lambda route: route.fulfill(
        status=200,
        content_type="application/json",
        body=json.dumps(history_mock)
    ))

    page.route("**/api/v1/audit/ativos/1", lambda route: route.fulfill(
        status=200, body=json.dumps([])
    ))

    # Mock specific file requests (images/pdf) to avoid 404s
    page.route("**/api/v1/ativos/1/qrcode", lambda route: route.fulfill(status=200, body=b""))

    # Set Auth Token to bypass login
    page.goto("http://localhost:5173/login") # Go to login first to initialize context
    page.evaluate("localStorage.setItem('authToken', 'fake-token')")

    # Navigate to Detail View
    page.goto("http://localhost:5173/ativos/1")

    # Wait for the chart to be visible
    # We look for the card title "Histórico de Saúde (Disco)"
    expect(page.get_by_text("Histórico de Saúde (Disco)")).to_be_visible()

    # Wait a bit for chart animation
    page.wait_for_timeout(1000)

    # Take screenshot
    page.screenshot(path="verification.png")
    print("Screenshot saved to verification.png")

    browser.close()

with sync_playwright() as playwright:
    run(playwright)
