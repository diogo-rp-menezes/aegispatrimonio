ALTER TABLE ativos ADD COLUMN previsao_esgotamento_disco DATE;

UPDATE ativos
SET previsao_esgotamento_disco = CAST(JSON_UNQUOTE(JSON_EXTRACT(atributos, '$.previsaoEsgotamentoDisco')) AS DATE)
WHERE JSON_EXTRACT(atributos, '$.previsaoEsgotamentoDisco') IS NOT NULL;
