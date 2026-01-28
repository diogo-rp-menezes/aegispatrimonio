from playwright.sync_api import Page, expect, sync_playwright
import os

def verify_login_page(page: Page):
    # 1. Go to Login page
    page.goto("http://localhost:5173/login")

    # 2. Wait for page load
    page.wait_for_load_state("networkidle")

    # 3. Check for "Entrar com Google" button
    google_btn = page.locator("a.btn-outline-danger")
    expect(google_btn).to_be_visible()
    expect(google_btn).to_contain_text("Entrar com Google")

    # 4. Screenshot
    os.makedirs("/home/jules/verification", exist_ok=True)
    page.screenshot(path="/home/jules/verification/login_google.png")
    print("Screenshot saved to /home/jules/verification/login_google.png")

if __name__ == "__main__":
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()
        try:
            verify_login_page(page)
        except Exception as e:
            print(f"Verification failed: {e}")
            page.screenshot(path="/home/jules/verification/error.png")
        finally:
            browser.close()
