import { test, expect } from '@playwright/test';

test.describe('Gerenciamento de Ativos', () => {

    // Fake DB state
    let db = {
        ativos: [],
        tipos: [{ id: 1, nome: 'Notebook' }, { id: 2, nome: 'Monitor' }],
        localizacoes: [{ id: 1, nome: 'Sala 1', filial: { id: 1 } }],
        fornecedores: [{ id: 1, nome: 'Dell' }],
        funcionarios: [{ id: 1, nome: 'João Silva', filiais: [{ id: 1 }] }]
    };

    test.beforeEach(async ({ page }) => {
        // Reset DB for each test
        db.ativos = [];

        // Mock Login removed to use Real Backend

        // Mock Dashboard Stats/Alerts
        await page.route('**/dashboard/stats', async route => {
            await route.fulfill({ json: { totalAtivos: db.ativos.length, predicaoCritica: 0 } });
        });
        await page.route('**/alerts/recent', async route => {
            await route.fulfill({ json: [] });
        });

        // Mock Aux Lists
        await page.route('**/tipos-ativos', async route => route.fulfill({ json: db.tipos }));
        await page.route('**/localizacoes', async route => route.fulfill({ json: db.localizacoes }));
        await page.route('**/fornecedores', async route => route.fulfill({ json: db.fornecedores }));
        await page.route('**/funcionarios', async route => route.fulfill({ json: { content: db.funcionarios } }));

        // Mock Ativos CRUD

        // GET /ativos (List)
        await page.route('**/ativos?*', async route => {
            // Simple mock: return all, ignoring pagination params for now
            // The URL might have query params like ?page=0&size=10
            await route.fulfill({
                json: {
                    content: db.ativos,
                    totalElements: db.ativos.length,
                    totalPages: 1,
                    size: 20,
                    number: 0
                }
            });
        });

        // POST /ativos (Create)
        await page.route('**/ativos', async route => {
            if (route.request().method() === 'POST') {
                const data = await route.request().postDataJSON();
                const newId = db.ativos.length + 1;
                const newAtivo = {
                    id: newId,
                    ...data,
                    // Enrich with names for display
                    tipoAtivoNome: db.tipos.find(t => t.id === data.tipoAtivoId)?.nome,
                    status: data.status || 'ATIVO'
                };
                db.ativos.push(newAtivo);
                await route.fulfill({ json: newAtivo });
            } else {
                // Fallback for GET (List) if regex matches loosely, but strict match usually handles it
                await route.continue();
            }
        });

        // GET /ativos/{id}
        await page.route(/\/ativos\/\d+$/, async route => {
            const url = route.request().url();
            const id = parseInt(url.split('/').pop());

            if (route.request().method() === 'GET') {
                const ativo = db.ativos.find(a => a.id === id);
                if (ativo) {
                    await route.fulfill({ json: ativo });
                } else {
                    await route.fulfill({ status: 404 });
                }
            } else if (route.request().method() === 'PUT') {
                // Update
                const data = await route.request().postDataJSON();
                const index = db.ativos.findIndex(a => a.id === id);
                if (index !== -1) {
                    db.ativos[index] = { ...db.ativos[index], ...data };
                    // Enrich again if needed
                    db.ativos[index].tipoAtivoNome = db.tipos.find(t => t.id === db.ativos[index].tipoAtivoId)?.nome;
                    await route.fulfill({ json: db.ativos[index] });
                } else {
                    await route.fulfill({ status: 404 });
                }
            } else if (route.request().method() === 'DELETE') {
                const index = db.ativos.findIndex(a => a.id === id);
                if (index !== -1) {
                    db.ativos.splice(index, 1);
                    await route.fulfill({ status: 204 });
                } else {
                    await route.fulfill({ status: 404 });
                }
            } else {
                await route.continue();
            }
        });

        // Mock specific endpoint for generating term PDF (used in DetailView)
        await page.route(/\/ativos\/\d+\/termo$/, async route => {
            await route.fulfill({
                status: 200,
                contentType: 'application/pdf',
                body: 'fake-pdf-content'
            });
        });

        // Login logic
        await page.goto('/login');
        await page.fill('#email', 'admin@aegis.com');
        await page.fill('#password', '123456');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL(/\/dashboard/);
    });

    test('Deve criar e listar um ativo', async ({ page }) => {
        // 1. Criar
        await page.goto('/ativos');
        await page.click('button:has-text("Adicionar Ativo")');

        const testName = `Ativo Teste ${Date.now()}`;
        await page.fill('#nome', testName);

        // Select Tipo (Index 1 -> Notebook)
        await page.locator('#tipo').selectOption({ index: 1 });

        // Select Localizacao (Index 1)
        await page.locator('#localizacao').selectOption({ index: 1 });

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
        // Ensure we are clicking the edit button for the specific row
        const row = page.locator(`tr:has-text("${testName}")`);
        await row.getByTitle('Editar').click();

        // Wait for form to load data
        await expect(page.locator('#nome')).toHaveValue(testName);

        const newName = `${testName} Editado`;
        await page.fill('#nome', newName);
        await page.click('button:has-text("Salvar")');

        // Verify update
        await expect(page.locator(`text=${newName}`)).toBeVisible();

        // 4. Excluir (Baixar) - Uses Detail View
        // Go to Details
        await page.locator(`tr:has-text("${newName}")`).getByTitle('Detalhes').click();

        // Handle confirm dialog
        page.on('dialog', dialog => dialog.accept());

        await page.click('button:has-text("Baixar Item")');

        // Should return to list
        await expect(page).toHaveURL(/\/ativos/);

        // Ensure it's gone
        await expect(page.locator(`text=${newName}`)).not.toBeVisible();
    });
});
