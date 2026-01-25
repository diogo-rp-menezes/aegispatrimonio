from playwright.sync_api import sync_playwright, expect

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()
    page = context.new_page()

    # Mock the API response (specific to API URL)
    page.route("**/api/v1/ativos/1", lambda route: route.fulfill(
        status=200,
        content_type="application/json",
        body='''{
            "id": 1,
            "nome": "Notebook Dell Latitude",
            "numeroPatrimonio": "PAT-001",
            "valorAquisicao": 5000.00,
            "dataAquisicao": "2023-01-15",
            "atributos": {
                "Processador": "Intel Core i7",
                "Memória RAM": "16GB",
                "prediction_slope": 0.5,
                "prediction_intercept": 100,
                "prediction_base_epoch_day": 19000,
                "prediction_calculated_at": "2023-10-27T10:00:00"
            }
        }'''
    ))

    page.route("**/api/v1/ativos/1/health-history", lambda route: route.fulfill(
        status=200, content_type="application/json", body='[]'
    ))

    page.route("**/audit/ativos/1", lambda route: route.fulfill(
        status=200, content_type="application/json", body='[]'
    ))

    page.route("**/api/v1/ativos/1/qrcode", lambda route: route.fulfill(status=200))
    page.route("**/api/v1/ativos/1/termo", lambda route: route.fulfill(status=200))

    page.add_init_script("""
        localStorage.setItem('authToken', 'fake-token');
        localStorage.setItem('currentFilial', '1');
    """)

    try:
        # Navigate to the detail page
        page.goto("http://localhost:5173/ativos/1")

        # Wait for the specific section header
        expect(page.get_by_text("Especificações Técnicas")).to_be_visible(timeout=5000)

        # Wait a bit for rendering
        page.wait_for_timeout(1000)

        # Take a screenshot
        page.screenshot(path="verification/detail_view.png", full_page=True)
        print("Screenshot saved to verification/detail_view.png")

    except Exception as e:
        print(f"Error: {e}")
        page.screenshot(path="verification/error_screenshot.png", full_page=True)
        print("Error screenshot saved to verification/error_screenshot.png")

    finally:
        browser.close()

with sync_playwright() as playwright:
    run(playwright)
