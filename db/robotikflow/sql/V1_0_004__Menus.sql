INSERT INTO public.menus (id, label, "order", icon, command, parent_id, workspace_id, alias) VALUES (1, 'Admin', 999, 'inter-safebox', NULL, NULL, NULL, 'ADMIN');
INSERT INTO public.menus (id, label, "order", icon, command, parent_id, workspace_id, alias) VALUES (2, 'Collections', 1, 'layer-group', NULL, NULL, NULL, 'COLLECTION');
INSERT INTO public.menus (id, label, "order", icon, command, parent_id, workspace_id, alias) VALUES (11, 'Collections', 0, 'layer-group', 'admin/collections', 1, NULL, NULL);
INSERT INTO public.menus (id, label, "order", icon, command, parent_id, workspace_id, alias) VALUES (12, 'Documents', 1, 'folder-open', 'admin/docs', 1, NULL, NULL);
INSERT INTO public.menus (id, label, "order", icon, command, parent_id, workspace_id, alias) VALUES (3, 'Config', 2, 'cog', NULL, NULL, NULL, 'CONFIG');
INSERT INTO public.menus (id, label, "order", icon, command, parent_id, workspace_id, alias) VALUES (31, 'Account', 0, 'id-card', NULL, 3, NULL, NULL);
INSERT INTO public.menus (id, label, "order", icon, command, parent_id, workspace_id, alias) VALUES (32, 'Workspace', 1, 'desktop', NULL, 3, NULL, NULL);
INSERT INTO public.menus (id, label, "order", icon, command, parent_id, workspace_id, alias) VALUES (311, 'Options', 0, 'user-edit', 'config/account/options', 31, NULL, NULL);
INSERT INTO public.menus (id, label, "order", icon, command, parent_id, workspace_id, alias) VALUES (312, 'Workspaces', 1, 'desktop', 'config/account/workspaces', 31, NULL, NULL);
INSERT INTO public.menus (id, label, "order", icon, command, parent_id, workspace_id, alias) VALUES (321, 'Options', 0, 'cog', 'config/workspace/options', 32, NULL, NULL);
INSERT INTO public.menus (id, label, "order", icon, command, parent_id, workspace_id, alias) VALUES (322, 'Plan', 1, 'money-bill', 'config/workspace/plan', 32, NULL, NULL);
INSERT INTO public.menus (id, label, "order", icon, command, parent_id, workspace_id, alias) VALUES (323, 'Users', 2, 'users', 'config/workspace/users', 32, NULL, NULL);
INSERT INTO public.menus (id, label, "order", icon, command, parent_id, workspace_id, alias) VALUES (324, 'Groups', 3, 'sitemap', 'config/workspace/groups', 32, NULL, NULL);
INSERT INTO public.menus (id, label, "order", icon, command, parent_id, workspace_id, alias) VALUES (325, 'Credentials', 4, 'plug', 'config/workspace/credentials', 32, NULL, NULL);
INSERT INTO public.menus (id, label, "order", icon, command, parent_id, workspace_id, alias) VALUES (326, 'Providers', 5, 'box', 'config/workspace/providers', 32, NULL, NULL);
INSERT INTO public.menus (id, label, "order", icon, command, parent_id, workspace_id, alias) VALUES (327, 'Apps', 6, 'file-word', 'config/workspace/apps', 32, NULL, NULL);

INSERT INTO public.menus_roles (role_id, menu_id) VALUES (5,	1);
INSERT INTO public.menus_roles (role_id, menu_id) VALUES (2,	2);
INSERT INTO public.menus_roles (role_id, menu_id) VALUES (5,	11);
INSERT INTO public.menus_roles (role_id, menu_id) VALUES (5,	12);
INSERT INTO public.menus_roles (role_id, menu_id) VALUES (2,	3);
INSERT INTO public.menus_roles (role_id, menu_id) VALUES (2,	31);
INSERT INTO public.menus_roles (role_id, menu_id) VALUES (3,	32);
INSERT INTO public.menus_roles (role_id, menu_id) VALUES (3,	311);
INSERT INTO public.menus_roles (role_id, menu_id) VALUES (2,	312);
INSERT INTO public.menus_roles (role_id, menu_id) VALUES (3,	321);
INSERT INTO public.menus_roles (role_id, menu_id) VALUES (3,	322);
INSERT INTO public.menus_roles (role_id, menu_id) VALUES (3,	323);
INSERT INTO public.menus_roles (role_id, menu_id) VALUES (3,	324);
INSERT INTO public.menus_roles (role_id, menu_id) VALUES (3,	325);
INSERT INTO public.menus_roles (role_id, menu_id) VALUES (3,	326);
INSERT INTO public.menus_roles (role_id, menu_id) VALUES (3,	327);
