-- Inserir dados iniciais para filiais
INSERT INTO filiais (nome, codigo, status) VALUES
('Matriz - São Paulo', 'MATRIZ-SP', 'ATIVO'),
('Filial - Rio de Janeiro', 'FILIAL-RJ', 'ATIVO'),
('Filial - Minas Gerais', 'FILIAL-MG', 'ATIVO');

-- Inserir dados iniciais para tipos de ativo (com todas as colunas da entidade JPA)
INSERT INTO tipos_ativo (nome, descricao, categoria_contabil, icone, status) VALUES
('Notebooks e Computadores', 'Equipamentos de informática como notebooks, desktops, tablets', 'Equipamentos de Informática', '💻', 'ATIVO'),
('Móveis e Utensílios', 'Móveis, mesas, cadeiras, armários e utensílios de escritório', 'Móveis e Utensílios', '🪑', 'ATIVO'),
('Veículos', 'Automóveis, motocicletas e outros veículos corporativos', 'Veículos', '🚗', 'ATIVO'),
('Imóveis', 'Prédios, terrenos e imóveis corporativos', 'Imóveis', '🏢', 'ATIVO'),
('Software', 'Softwares, licenças e sistemas corporativos', 'Softwares e Licenças', '📱', 'ATIVO');

-- Inserir dados iniciais para fornecedores
INSERT INTO fornecedores (nome, email_contato, telefone_contato, status) VALUES
('Tech Solutions Ltda', 'vendas@techsolutions.com', '(11) 9999-8888', 'ATIVO'),
('Office Furniture Brasil', 'contato@officefurniture.com', '(21) 7777-6666', 'ATIVO'),
('Auto Veículos S.A.', 'vendas@autoveiculos.com', '(31) 5555-4444', 'ATIVO'),
('Equipamentos Industriais RJ', 'compras@equipamentosrj.com', '(22) 3333-2222', 'ATIVO');