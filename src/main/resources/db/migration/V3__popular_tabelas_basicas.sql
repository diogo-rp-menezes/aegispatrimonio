-- Inserir dados iniciais para filiais
INSERT INTO filiais (nome, codigo) VALUES
('Matriz - São Paulo', 'MATRIZ-SP'),
('Filial - Rio de Janeiro', 'FILIAL-RJ'),
('Filial - Minas Gerais', 'FILIAL-MG');

-- Inserir dados iniciais para tipos de ativo (com todas as colunas)
INSERT INTO tipos_ativo (nome, descricao, categoria_contabil, icone) VALUES
('Notebooks e Computadores', 'Equipamentos de informática como notebooks, desktops, tablets', 'Equipamentos de Informática', '💻'),
('Móveis e Utensílios', 'Móveis, mesas, cadeiras, armários e utensílios de escritório', 'Móveis e Utensílios', '🪑'),
('Veículos', 'Automóveis, motocicletas e outros veículos corporativos', 'Veículos', '🚗'),
('Imóveis', 'Prédios, terrenos e imóveis corporativos', 'Imóveis', '🏢'),
('Software', 'Softwares, licenças e sistemas corporativos', 'Softwares e Licenças', '📱');

-- Inserir dados iniciais para fornecedores
INSERT INTO fornecedores (nome, email_contato, telefone_contato) VALUES
('Tech Solutions Ltda', 'vendas@techsolutions.com', '(11) 9999-8888'),
('Office Furniture Brasil', 'contato@officefurniture.com', '(21) 7777-6666'),
('Auto Veículos S.A.', 'vendas@autoveiculos.com', '(31) 5555-4444'),
('Equipamentos Industriais RJ', 'compras@equipamentosrj.com', '(22) 3333-2222');