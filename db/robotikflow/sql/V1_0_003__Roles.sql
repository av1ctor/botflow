INSERT INTO public.users_roles (id, name, above_of_id, deleted_by_id, deleted_at) VALUES (1, 'ROLE_USER_PARTNER', NULL, NULL, NULL);
INSERT INTO public.users_roles (id, name, above_of_id, deleted_by_id, deleted_at) VALUES (2, 'ROLE_USER_CONTRIBUTOR', 1, NULL, NULL);
INSERT INTO public.users_roles (id, name, above_of_id, deleted_by_id, deleted_at) VALUES (3, 'ROLE_USER_ADMIN', 2, NULL, NULL);
INSERT INTO public.users_roles (id, name, above_of_id, deleted_by_id, deleted_at) VALUES (4, 'ROLE_ADMIN_EMPLOYEE', NULL, NULL, NULL);
INSERT INTO public.users_roles (id, name, above_of_id, deleted_by_id, deleted_at) VALUES (5, 'ROLE_ADMIN_SUPERVISOR', 4, NULL, NULL);
INSERT INTO public.users_roles (id, name, above_of_id, deleted_by_id, deleted_at) VALUES (6, 'ROLE_ADMIN_DIRECTOR', 5, NULL, NULL);
INSERT INTO public.users_roles (id, name, above_of_id, deleted_by_id, deleted_at) VALUES (7, 'ROLE_ADMIN_MASTER', 6, NULL, NULL);

