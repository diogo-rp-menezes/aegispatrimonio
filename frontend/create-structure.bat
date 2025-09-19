@echo off
setlocal enabledelayedexpansion

echo Criando estrutura de pastas e arquivos...

:: Criar diretórios principais
mkdir src
mkdir src\assets
mkdir src\assets\scss
mkdir src\components
mkdir src\components\layout
mkdir src\components\dashboard
mkdir src\components\common
mkdir src\views
mkdir src\stores
mkdir src\router
mkdir src\services

:: Criar arquivos SCSS
echo // Variáveis SCSS > src\assets\scss\_variables.scss
echo // Estilos globais > src\assets\scss\_global.scss
echo // Importar outros arquivos SCSS > src\assets\scss\main.scss
echo @import 'variables'; >> src\assets\scss\main.scss
echo @import 'global'; >> src\assets\scss\main.scss

:: Criar componentes Vue
echo "<template>" > src\components\layout\Sidebar.vue
echo "  <div>Sidebar Component</div>" >> src\components\layout\Sidebar.vue
echo "</template>" >> src\components\layout\Sidebar.vue

echo "<template>" > src\components\layout\TopBar.vue
echo "  <div>TopBar Component</div>" >> src\components\layout\TopBar.vue
echo "</template>" >> src\components\layout\TopBar.vue

echo "<template>" > src\components\dashboard\StatCard.vue
echo "  <div>StatCard Component</div>" >> src\components\dashboard\StatCard.vue
echo "</template>" >> src\components\dashboard\StatCard.vue

echo "<template>" > src\components\dashboard\QuickActions.vue
echo "  <div>QuickActions Component</div>" >> src\components\dashboard\QuickActions.vue
echo "</template>" >> src\components\dashboard\QuickActions.vue

echo "<template>" > src\components\dashboard\PatrimonyCategory.vue
echo "  <div>PatrimonyCategory Component</div>" >> src\components\dashboard\PatrimonyCategory.vue
echo "</template>" >> src\components\dashboard\PatrimonyCategory.vue

echo "<template>" > src\components\common\LoadingSpinner.vue
echo "  <div>LoadingSpinner Component</div>" >> src\components\common\LoadingSpinner.vue
echo "</template>" >> src\components\common\LoadingSpinner.vue

echo "<template>" > src\components\common\ErrorMessage.vue
echo "  <div>ErrorMessage Component</div>" >> src\components\common\ErrorMessage.vue
echo "</template>" >> src\components\common\ErrorMessage.vue

:: Criar views
echo "<template>" > src\views\Dashboard.vue
echo "  <div>Dashboard View</div>" >> src\views\Dashboard.vue
echo "</template>" >> src\views\Dashboard.vue

echo "<template>" > src\views\PatrimonyDetail.vue
echo "  <div>PatrimonyDetail View</div>" >> src\views\PatrimonyDetail.vue
echo "</template>" >> src\views\PatrimonyDetail.vue

echo "<template>" > src\views\Equipments.vue
echo "  <div>Equipments View</div>" >> src\views\Equipments.vue
echo "</template>" >> src\views\Equipments.vue

echo "<template>" > src\views\Settings.vue
echo "  <div>Settings View</div>" >> src\views\Settings.vue
echo "</template>" >> src\views\Settings.vue

:: Criar stores
echo "// Pinia store index" > src\stores\index.js
echo "// Patrimony store" > src\stores\patrimony.js

:: Criar router
echo "// Vue Router configuration" > src\router\index.js

:: Criar services
echo "// API configuration" > src\services\api.js
echo "// Patrimony service" > src\services\patrimonyService.js

:: Criar App.vue
echo "<template>" > src\App.vue
echo "  <div id=\"app\">" >> src\App.vue
echo "    <router-view />" >> src\App.vue
echo "  </div>" >> src\App.vue
echo "</template>" >> src\App.vue

echo.
echo Estrutura criada com sucesso!
echo.
echo Estrutura criada:
tree /f src
pause