INSERT INTO public.collections_templates_categories(
	pub_id, 
    name, 
    "desc", 
    icon, 
    thumb, 
    "order", 
    workspace_id, 
    created_by_id, 
    created_at)
    values (
    '608710889d0f72784fba674b',
    'General',
    'General collections',
    'square-o',
    null,
    0,
    null,
    1,
    CURRENT_TIMESTAMP
    );

INSERT INTO public.collections_templates(
	pub_id, category_id, name, "desc", icon, thumb, "order", workspace_id, created_by_id, created_at, schema)
	VALUES (
    '608711bb68652bd49283b054',
    LASTVAL(),
    'Basic',
    'Basic collection',
    'table',
    null,
    0,
    null,
    1,
    CURRENT_TIMESTAMP,
    '{"version":1,"columns":{"id":{"auto":true,"unique":true,"type":"number","label":"Id","disabled":{"insert":{"and":[{"cond":{"when":"ALWAYS"}}]},"update":{"and":[{"cond":{"when":"ALWAYS"}}]}}},"ordem":{"type":"number","label":"Ordem","positional":true},"valor":{"type":"string","label":"Valor","nullable":true}},"indexes":[{"dir":"asc","columns":["id"]},{"dir":"asc","columns":["ordem"]},{"dir":"asc","columns":["valor"]}],"views":[{"id": "table", "name":"Tabela","type":"table","style":{"height":60,"border":{"style":"solid","color":"white"}},"sort":{"id":{"dir":"desc"}},"fields":{"id":{"index":0,"width":80,"frozen":true},"valor":{"index":1,"width":150}}}]}'
    );