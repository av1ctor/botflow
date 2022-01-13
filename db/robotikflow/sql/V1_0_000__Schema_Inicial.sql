/*CREATE DATABASE robotikflow
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    CONNECTION LIMIT = -1;*/

ALTER DATABASE robotikflow OWNER TO postgres;

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;
SET timezone to 'GMT';

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;
COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';

CREATE FUNCTION public.proc_collections_tree_afterinsert_collections() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
begin
		insert into collections_tree (parent_id, child_id, depth) values 
    	(new.id, new.id, 0);
		insert into collections_tree (parent_id, child_id, depth)
    	select p.parent_id, new.id, p.depth + 1
        	from collections_tree p
            where p.child_id = new.parent_id;
	return new;
end;
$$;

--ALTER FUNCTION public.proc_collections_tree_afterinsert_collections() owner to botflow_user;

CREATE FUNCTION public.proc_collections_tree_afterupdate_collections() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
begin
	if new.parent_id != old.parent_id
    then
		        delete from collections_tree d
			using (select 
					p.parent_id, c.child_id 
					from collections_tree p, collections_tree c 
					where p.child_id = new.id 
						and c.parent_id = new.id) cte
			where d.parent_id = cte.parent_id
					and d.child_id = cte.child_id
						and cte.parent_id != new.id;
	    
				insert into collections_tree (parent_id, child_id, depth)
	    	select p.parent_id, c.child_id, p.depth+c.depth+1
	        	from collections_tree p, collections_tree c
					where p.child_id = new.parent_id
						and c.parent_id = new.id;
	end if;
	
	return new;
end;
$$;

--ALTER FUNCTION public.proc_collections_tree_afterupdate_collections() owner to botflow_user;

CREATE FUNCTION public.proc_documents_tree_afterinsert_documents() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
begin
		insert into documents_tree (parent_id, child_id, depth) values 
    	(new.id, new.id, 0);
		insert into documents_tree (parent_id, child_id, depth)
    	select p.parent_id, new.id, p.depth + 1
        	from documents_tree p
            where p.child_id = new.parent_id;
	return new;
end;
$$;

--ALTER FUNCTION public.proc_documents_tree_afterinsert_documents() owner to botflow_user;

CREATE FUNCTION public.proc_documents_tree_afterupdate_documents() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
begin
	if new.parent_id != old.parent_id
    then
		        delete from documents_tree d
			using (select 
					p.parent_id, c.child_id 
					from documents_tree p, documents_tree c 
					where p.child_id = new.id 
						and c.parent_id = new.id) cte
			where d.parent_id = cte.parent_id
					and d.child_id = cte.child_id
						and cte.parent_id != new.id;
	    
				insert into documents_tree (parent_id, child_id, depth)
	    	select p.parent_id, c.child_id, p.depth+c.depth+1
	        	from documents_tree p, documents_tree c
					where p.child_id = new.parent_id
						and c.parent_id = new.id;
	end if;
	
	return new;
end;
$$;

--ALTER FUNCTION public.proc_documents_tree_afterupdate_documents() owner to botflow_user;

CREATE FUNCTION public.proc_groups_tree_afterinsert_groups() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
begin
	insert into groups_tree (parent_id, child_id, depth) values 
    	(new.id, new.id, 0);
    insert into groups_tree (parent_id, child_id, depth)
    	select p.parent_id, new.id, p.depth+1
        	from groups_tree p
            where p.child_id = new.parent_id;
	return new;
end;
$$;

ALTER FUNCTION public.proc_groups_tree_afterinsert_groups() OWNER TO postgres;

CREATE FUNCTION public.proc_groups_tree_afterupdate_groups() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
begin
	if new.parent_id != old.parent_id
    then
		        delete from groups_tree g
			using (select 
					p.parent_id, c.child_id 
					from groups_tree p, groups_tree c 
					where p.child_id = new.id 
						and c.parent_id = new.id) cte
			where g.parent_id = cte.parent_id
					and g.child_id = cte.child_id
						and cte.parent_id != new.id;
	    
				insert into groups_tree (parent_id, child_id, depth)
	    	select p.parent_id, c.child_id, p.depth+c.depth+1
	        	from groups_tree p, groups_tree c
					where p.child_id = new.parent_id
						and c.parent_id = new.id;
	end if;
	return new;
end;
$$;

ALTER FUNCTION public.proc_groups_tree_afterupdate_groups() OWNER TO postgres;

CREATE FUNCTION public.proc_workspaces_posts_tree_afterinsert_workspaces_posts() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
begin
		insert into workspaces_posts_tree (parent_id, child_id, depth) values 
    	(new.id, new.id, 0);
		insert into workspaces_posts_tree (parent_id, child_id, depth)
    	select p.parent_id, new.id, p.depth + 1
        	from workspaces_posts_tree p
            where p.child_id = new.parent_id;
	return new;
end;
$$;

--ALTER FUNCTION public.proc_workspaces_posts_tree_afterinsert_workspaces_posts() owner to botflow_user;

CREATE FUNCTION public.proc_workspaces_posts_tree_afterupdate_workspaces_posts() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
begin
	if new.parent_id != old.parent_id
    then
		        delete from workspaces_posts_tree d
			using (select 
					p.parent_id, c.child_id 
					from workspaces_posts_tree p, workspaces_posts_tree c 
					where p.child_id = new.id 
						and c.parent_id = new.id) cte
			where d.parent_id = cte.parent_id
					and d.child_id = cte.child_id
						and cte.parent_id != new.id;
	    
				insert into workspaces_posts_tree (parent_id, child_id, depth)
	    	select p.parent_id, c.child_id, p.depth+c.depth+1
	        	from workspaces_posts_tree p, workspaces_posts_tree c
					where p.child_id = new.parent_id
						and c.parent_id = new.id;
	end if;
	
	return new;
end;
$$;

--ALTER FUNCTION public.proc_workspaces_posts_tree_afterupdate_workspaces_posts() owner to botflow_user;
SET default_tablespace = '';
SET default_with_oids = false;

CREATE TABLE public.collections (
    id bigint NOT NULL,
    pub_id character(24) NOT NULL,
    workspace_id bigint NOT NULL,
    name character varying(64) NOT NULL,
    "desc" character varying(512),
    created_by_id bigint NOT NULL,
    created_at timestamp NOT NULL,
    updated_by_id bigint,
    updated_at timestamp,
    deleted_by_id bigint,
    deleted_at timestamp,
    published_at timestamp,
    published_by_id bigint,
    icon character varying(24),
    "order" smallint DEFAULT 0,
    type smallint DEFAULT 0 NOT NULL,
    parent_id bigint
);

--ALTER TABLE public.collections owner to botflow_user;

CREATE TABLE public.collections_tree (
    parent_id bigint NOT NULL,
    child_id bigint NOT NULL,
    depth smallint NOT NULL
);

--ALTER TABLE public.collections_tree owner to botflow_user;

CREATE TABLE public.collections_automations (
    id bigint NOT NULL,
    pub_id character(24) NOT NULL,
    type smallint NOT NULL,
    trigger smallint NOT NULL,
    collection_id bigint NOT NULL,
    "desc" character varying(1024),
    priority smallint DEFAULT 0 NOT NULL,
    created_by_id bigint NOT NULL,
    created_at timestamp NOT NULL,
    updated_by_id bigint,
    updated_at timestamp
);

--ALTER TABLE public.collections_automations owner to botflow_user;

CREATE TABLE public.collections_automations_activities (
    id bigint NOT NULL,
    pub_id character(24) NOT NULL,
    automation_id bigint NOT NULL,
    activity_id bigint NOT NULL,
    state_id bigint NULL
);

--ALTER TABLE public.collections_automations_activities owner to botflow_user;
ALTER TABLE public.collections_automations_activities ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.collections_automations_activities_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.collections_automations_fields (
    id bigint NOT NULL,
    schema text
);

--ALTER TABLE public.collections_automations_fields owner to botflow_user;

CREATE TABLE public.collections_automations_dates (
    id bigint NOT NULL,
    start timestamp NOT NULL,
    repeat integer NOT NULL,
    next timestamp NOT NULL
);

--ALTER TABLE public.collections_automations_dates owner to botflow_user;

ALTER TABLE public.collections_automations ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.collections_automations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.collections_automations_rows (
    id bigint NOT NULL
);

--ALTER TABLE public.collections_automations_rows owner to botflow_user;

CREATE TABLE public.collections_automations_logs (
    id bigint NOT NULL,
    automation_id bigint NOT NULL
);

--ALTER TABLE public.collections_automations_logs owner to botflow_user;

CREATE TABLE public.collections_auxs (
    id bigint NOT NULL,
    principal_id bigint NOT NULL,
    aux_id bigint NOT NULL,
    "order" integer DEFAULT 0 NOT NULL
);

--ALTER TABLE public.collections_auxs owner to botflow_user;

ALTER TABLE public.collections_auxs ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.collections_auxs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

ALTER TABLE public.collections ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.collections_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.collections_integrations (
    id bigint NOT NULL,
    pub_id character(24) NOT NULL,
    collection_id bigint NOT NULL,
    trigger_id bigint not null,
    trigger_state_id bigint not null,
    "desc" character varying(1024),
    priority smallint DEFAULT 0 NOT NULL,
    freq integer,
    start timestamp,
    started boolean DEFAULT true NOT NULL,
    active boolean DEFAULT true NOT NULL,
    min_of_day integer,
    rerun_at timestamp,
    reruns integer,
    created_by_id bigint NOT NULL,
    created_at timestamp NOT NULL,
    updated_by_id bigint,
    updated_at timestamp
);

--ALTER TABLE public.collections_integrations owner to botflow_user;

CREATE TABLE public.collections_integrations_activities (
    id bigint NOT NULL,
    pub_id character(24) NOT NULL,
    integration_id bigint NOT NULL,
    activity_id bigint NOT NULL,
    state_id bigint NULL
);

--ALTER TABLE public.collections_integrations_activities owner to botflow_user;

ALTER TABLE public.collections_integrations_activities ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.collections_integrations_activities_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

ALTER TABLE public.collections_integrations ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.collections_integrations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.collections_integrations_logs (
    id bigint NOT NULL,
    integration_id bigint NOT NULL
);

--ALTER TABLE public.collections_integrations_logs owner to botflow_user;

CREATE TABLE public.collections_items_documents (
    id bigint NOT NULL,
    pub_id character(24) NOT NULL,
    workspace_id bigint NOT NULL,
    collection_id bigint NOT NULL,
    item_id character varying(64) NOT NULL,
    document_id bigint NOT NULL,
    created_by_id bigint NOT NULL,
    created_at timestamp NOT NULL,
    updated_by_id bigint,
    updated_at timestamp,
    deleted_by_id bigint,
    deleted_at timestamp
);

--ALTER TABLE public.collections_items_documents owner to botflow_user;

ALTER TABLE public.collections_items_documents ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.collections_items_documents_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.collections_items_logs (
    id bigint NOT NULL,
    item_id character varying(64) NOT NULL
);

--ALTER TABLE public.collections_items_logs owner to botflow_user;

CREATE TABLE public.collections_items_posts (
    id bigint NOT NULL,
    item_id character varying(64) NOT NULL
);

--ALTER TABLE public.collections_items_posts owner to botflow_user;

CREATE TABLE public.collections_logs (
    id bigint NOT NULL,
    collection_id bigint NOT NULL
);

--ALTER TABLE public.collections_logs owner to botflow_user;

CREATE TABLE public.collections_auths (
    id bigint NOT NULL,
    pub_id character(24) NOT NULL,
    collection_id bigint NOT NULL,
    user_id bigint,
    group_id bigint,
    role smallint NOT NULL,
    reverse boolean NOT NULL,
    created_by_id bigint NOT NULL,
    created_at timestamp NOT NULL,
    updated_by_id bigint,
    updated_at timestamp
);

--ALTER TABLE public.collections_auths owner to botflow_user;

ALTER TABLE public.collections_auths ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.collections_auths_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.collections_posts (
    id bigint NOT NULL,
    collection_id bigint NOT NULL
);

--ALTER TABLE public.collections_posts owner to botflow_user;

CREATE TABLE public.collections_schemas (
    id bigint NOT NULL,
    auto_gen_id character varying(64),
    positional_id character varying(64),
    options bigint DEFAULT 0 NOT NULL,
    provider_id bigint,
    schema text NOT NULL
);

--ALTER TABLE public.collections_schemas owner to botflow_user;

CREATE TABLE public.collections_templates (
    id bigint NOT NULL,
    pub_id character(24) NOT NULL,
    category_id bigint NOT NULL,
    name character varying(256) NOT NULL,
    "desc" text NOT NULL,
    icon character varying(64),
    thumb character varying(256),
    "order" smallint DEFAULT 0 NOT NULL,
    workspace_id bigint,
    created_by_id bigint NOT NULL,
    created_at timestamp NOT NULL,
    updated_by_id bigint,
    updated_at timestamp,
    deleted_by_id bigint,
    deleted_at timestamp,
    schema text NOT NULL
);

--ALTER TABLE public.collections_templates owner to botflow_user;

CREATE TABLE public.collections_templates_categories (
    id bigint NOT NULL,
    pub_id character(24) NOT NULL,
    name character varying(256) NOT NULL,
    "desc" text NOT NULL,
    icon character varying(64),
    thumb character varying(256),
    "order" smallint DEFAULT 0 NOT NULL,
    workspace_id bigint,
    created_by_id bigint NOT NULL,
    created_at timestamp NOT NULL,
    updated_by_id bigint,
    updated_at timestamp,
    deleted_by_id bigint,
    deleted_at timestamp
);

--ALTER TABLE public.collections_templates_categories owner to botflow_user;

ALTER TABLE public.collections_templates_categories ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.collections_templates_categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

ALTER TABLE public.collections_templates ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.collections_templates_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.collections_versions (
    id bigint NOT NULL,
    pub_id character(24) NOT NULL,
    collection_id bigint NOT NULL,
    diff text NOT NULL,
    change_id bigint NOT NULL,
    created_at timestamp NOT NULL
);

--ALTER TABLE public.collections_versions owner to botflow_user;

CREATE TABLE public.collections_versions_changes (
    id bigint not null GENERATED BY DEFAULT AS IDENTITY,
    "desc" character varying(256) NOT NULL,
    PRIMARY KEY ("id")
);

--ALTER TABLE public.collections_versions_changes owner to botflow_user;

ALTER TABLE public.collections_versions ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.collections_versions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.documents_tags (
    document_id bigint NOT NULL,
    tag_id bigint NOT NULL
);

--ALTER TABLE public.documents_tags owner to botflow_user;

CREATE TABLE public.documents_tree (
    parent_id bigint NOT NULL,
    child_id bigint NOT NULL,
    depth smallint NOT NULL
);

--ALTER TABLE public.documents_tree owner to botflow_user;

create table public.documents_collections_integrations (
    id bigint not null GENERATED BY DEFAULT AS IDENTITY,
    document_id bigint not null,
    integration_id bigint not null,
    PRIMARY KEY ("id")
);

CREATE TABLE public.documents (
    id bigint NOT NULL,
    pub_id character(24) NOT NULL,
    workspace_id bigint NOT NULL,
    provider_id bigint NOT NULL,
    type smallint NOT NULL,
    name character varying(128) NOT NULL,
    size bigint,
    version smallint NOT NULL,
    extension character varying(16),
    parent_id bigint,
    owner_id bigint,
    group_id bigint,
    owner_auth smallint DEFAULT 2 NOT NULL,
    group_auth smallint DEFAULT 2 NOT NULL,
    others_auth smallint DEFAULT 2 NOT NULL,
    created_by_id bigint,
    created_at timestamp,
    updated_by_id bigint,
    updated_at timestamp,
    deleted_by_id bigint,
    deleted_at timestamp
);

--ALTER TABLE public.documents owner to botflow_user;

ALTER TABLE public.documents ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.documents_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.documents_logs (
    id bigint NOT NULL,
    document_id bigint NOT NULL
);

--ALTER TABLE public.documents_logs owner to botflow_user;

CREATE TABLE public.documents_posts (
    id bigint NOT NULL,
    document_id bigint NOT NULL
);

--ALTER TABLE public.documents_posts owner to botflow_user;

CREATE TABLE public.documents_exts (
    id bigint NOT NULL,
    file_id character varying(256),
    mime_type character varying(256),
    url_download character varying(1024),
    file_path character varying(4096)
);

--ALTER TABLE public.documents_exts owner to botflow_user;

CREATE TABLE public.documents_ints (
    id bigint NOT NULL,
    blob_id character varying(36) DEFAULT NULL::character varying,
    thumb_id character varying(36) DEFAULT NULL::character varying,
    preview_id character varying(36) DEFAULT NULL::character varying,
    locked boolean NOT NULL,
    locked_by_id bigint,
    locked_since timestamp,
    checked boolean NOT NULL,
    checked_by_id bigint,
    checked_since timestamp
);

--ALTER TABLE public.documents_ints owner to botflow_user;


CREATE TABLE public.groups_tree (
    parent_id bigint NOT NULL,
    child_id bigint NOT NULL,
    depth smallint NOT NULL
);

--ALTER TABLE public.groups_tree owner to botflow_user;

CREATE TABLE public.groups (
    id bigint NOT NULL,
    pub_id character(24) NOT NULL,
    name character varying(128) NOT NULL,
    parent_id bigint,
    workspace_id bigint NOT NULL,
    deletable boolean NOT NULL,
    created_at timestamp,
    created_by_id bigint,
    updated_at timestamp,
    updated_by_id bigint,
    deleted_by_id bigint,
    deleted_at timestamp
);

--ALTER TABLE public.groups owner to botflow_user;

ALTER TABLE public.groups ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.groups_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.icons (
    id bigint NOT NULL,
    name character varying(255) NOT NULL
);

--ALTER TABLE public.icons owner to botflow_user;

ALTER TABLE public.icons ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.icons_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.menus_groups (
    menu_id bigint NOT NULL,
    group_id bigint NOT NULL
);

--ALTER TABLE public.menus_groups owner to botflow_user;

CREATE TABLE public.menus_roles (
    role_id bigint NOT NULL,
    menu_id bigint NOT NULL
);

--ALTER TABLE public.menus_roles owner to botflow_user;

CREATE TABLE public.menus (
    id bigint NOT NULL,
    label character varying(64) NOT NULL,
    "order" smallint NOT NULL,
    icon character varying(32) NOT NULL,
    command character varying(510) DEFAULT NULL::character varying,
    parent_id bigint,
    workspace_id bigint,
    alias character varying(32)
);

--ALTER TABLE public.menus owner to botflow_user;

ALTER TABLE public.menus ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.menus_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.users_roles (
    id bigint NOT NULL,
    name character varying(64) NOT NULL,
    above_of_id bigint,
    deleted_by_id bigint,
    deleted_at timestamp
);

--ALTER TABLE public.users_roles owner to botflow_user;

ALTER TABLE public.users_roles ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.users_roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.documents_auths (
    id bigint NOT NULL,
    document_id bigint NOT NULL,
    user_id bigint,
    group_id bigint,
    type integer DEFAULT 0 NOT NULL,
    reverse boolean NOT NULL
);

--ALTER TABLE public.documents_auths owner to botflow_user;

ALTER TABLE public.documents_auths ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.documents_auths_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);



CREATE TABLE public.tags (
    id bigint NOT NULL,
    name character varying(32) NOT NULL,
    type smallint NOT NULL
);

--ALTER TABLE public.tags owner to botflow_user;

ALTER TABLE public.tags ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.tags_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.timezones (
    id character varying(64) NOT NULL,
    diff character varying(6) NOT NULL
);

--ALTER TABLE public.timezones owner to botflow_user;

CREATE TABLE public.users_groups_workspaces (
    group_id bigint NOT NULL,
    user_id bigint NOT NULL,
    workspace_id bigint NOT NULL
);

--ALTER TABLE public.users_groups_workspaces owner to botflow_user;

CREATE TABLE public.users_roles_workspaces (
    user_id bigint NOT NULL,
    workspace_id bigint NOT NULL,
    role_id bigint NOT NULL
);

--ALTER TABLE public.users_roles_workspaces owner to botflow_user;

CREATE TABLE public.users_props_workspaces (
    user_id bigint NOT NULL,
    workspace_id bigint NOT NULL,
    active boolean NOT NULL,
    invited_at timestamp,
    accepted_at timestamp
);

--ALTER TABLE public.users_props_workspaces owner to botflow_user;

CREATE TABLE public.users (
    id bigint NOT NULL,
    pub_id character(24) NOT NULL,
    name character varying(256),
    email character varying(128) NOT NULL,
    password character varying(128),
    active boolean NOT NULL,
    icon character varying(64) NOT NULL,
    timezone character varying(64) NOT NULL,
    nick character varying(64) NOT NULL,
    lang character varying(16) NOT NULL,
    theme character varying(64),
    created_at timestamp NOT NULL,
    confirmed_at timestamp,
    updated_at timestamp,
    updated_by_id bigint,
    deleted_by_id bigint,
    deleted_at timestamp
);

--ALTER TABLE public.users owner to botflow_user;

ALTER TABLE public.users ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.documents_versions (
    id bigint NOT NULL,
    version smallint NOT NULL,
    document_id bigint NOT NULL,
    created_at timestamp NOT NULL,
    created_by_id bigint NOT NULL
);

--ALTER TABLE public.documents_versions owner to botflow_user;

ALTER TABLE public.documents_versions ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.documents_versions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.workspaces (
    id bigint NOT NULL,
    pub_id character(24) NOT NULL,
    name character varying(64) NOT NULL,
    provider_id bigint,
    root_doc_id bigint,
    root_collection_id bigint,
    owner_id bigint,
    created_at timestamp NOT NULL,
    updated_by_id bigint,
    updated_at timestamp,
    deleted_by_id bigint,
    deleted_at timestamp
);

--ALTER TABLE public.workspaces owner to botflow_user;

ALTER TABLE public.workspaces ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.workspaces_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.workspaces_logs (
    id bigint NOT NULL,
    workspace_id bigint NOT NULL,
    type smallint NOT NULL,
    message text NOT NULL,
    extra text,
    date timestamp NOT NULL,
    user_id bigint
);

--ALTER TABLE public.workspaces_logs owner to botflow_user;

ALTER TABLE public.workspaces_logs ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.workspaces_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.workspaces_posts (
    id bigint NOT NULL,
    pub_id character(24) NOT NULL,
    workspace_id bigint NOT NULL,
    options bigint DEFAULT 0 NOT NULL,
    parent_id bigint,
    level smallint NOT NULL,
    posts integer NOT NULL,
    "order" smallint DEFAULT 0 NOT NULL,
    type smallint NOT NULL,
    title character varying(256),
    message text NOT NULL,
    created_at timestamp NOT NULL,
    created_by_id bigint NOT NULL,
    updated_at timestamp,
    updated_by_id bigint
);

--ALTER TABLE public.workspaces_posts owner to botflow_user;

ALTER TABLE public.workspaces_posts ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.workspaces_posts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);

CREATE TABLE public.workspaces_posts_tree (
    parent_id bigint NOT NULL,
    child_id bigint NOT NULL,
    depth smallint NOT NULL
);

create table public.objects_schemas (
    id bigint not null GENERATED BY DEFAULT AS IDENTITY,
    pub_id character(24) NOT NULL,
    version numeric(13,4) not null,
    type smallint not null,
    name character varying(64) NOT NULL,
    title character varying(128) NOT NULL,
    "desc" text NOT NULL,
    icon character varying(16) NOT NULL,
    schema text NOT NULL,
    hidden boolean null,
    statefull boolean not null,
    category character varying(64) NOT NULL, 
    fields text NOT NULL,
    methods text NULL,
    operations text NULL,
    created_at timestamp NOT NULL,
    created_by_id bigint NOT NULL,
    updated_at timestamp,
    updated_by_id bigint,
    deleted_at timestamp,
    deleted_by_id bigint,
    PRIMARY KEY ("id"),
    unique("pub_id")
);

create table public.activities_schemas (
    id bigint not null,
    dir smallint not null,
    PRIMARY KEY ("id")
);

create table public.credentials_schemas (
    id bigint not null,
    vendor character varying(64) NOT NULL,
    mode smallint NOT NULL,
    PRIMARY KEY ("id")
);

create table public.providers_schemas (
    id bigint not null,
    vendor character varying(64) NOT NULL,
    PRIMARY KEY ("id")
);

create table public.triggers_schemas (
    id bigint not null,
    PRIMARY KEY ("id")
);

create table public.objects (
    id bigint not null GENERATED BY DEFAULT AS IDENTITY,
    pub_id character(24) NOT NULL,
    workspace_id bigint not null,
    fields text not null,
    created_at timestamp NOT NULL,
    created_by_id bigint NOT NULL,
    updated_at timestamp,
    updated_by_id bigint,
    deleted_at timestamp,
    deleted_by_id bigint,
    version bigint,
    PRIMARY KEY ("id"),
    unique("pub_id")
);

create table public.objects_states (
    id bigint not null GENERATED BY DEFAULT AS IDENTITY,
    state text not null,
    PRIMARY KEY ("id")
);

create table public.activities (
    id bigint not null,
    schema_id bigint not null,
    PRIMARY KEY ("id")
);

create table public.credentials (
    id bigint not null,
    schema_id bigint not null,
    PRIMARY KEY ("id")
);

create table public.providers (
    id bigint not null,
    schema_id bigint not null,
    PRIMARY KEY ("id")
);

create table public.triggers (
    id bigint not null,
    schema_id bigint not null,
    PRIMARY KEY ("id")
);

--ALTER TABLE public.workspaces_posts_tree owner to botflow_user;--ONSTRAINT collectionautomations_activities_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections_automations
    ADD CONSTRAINT collectionautomations_pub_id_key UNIQUE (pub_id);

ALTER TABLE ONLY public.collections_automations
    ADD CONSTRAINT collectionautomations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections_automations_fields
    ADD CONSTRAINT collectionautomationsfield_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections_automations_dates
    ADD CONSTRAINT collectionautomationsdata_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections_automations_rows
    ADD CONSTRAINT collectionautomationslinha_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections_integrations_activities
    ADD CONSTRAINT collectionintegrations_activities_pub_id_key UNIQUE (pub_id);

ALTER TABLE ONLY public.collections_integrations_activities
    ADD CONSTRAINT collectionintegrations_activities_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections_integrations
    ADD CONSTRAINT collectionintegrations_pub_id_key UNIQUE (pub_id);

ALTER TABLE ONLY public.collections_integrations
    ADD CONSTRAINT collectionintegrations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections_schemas
    ADD CONSTRAINT collectiontabela_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections_templates_categories
    ADD CONSTRAINT collectiontemplatecategories_pub_id_key UNIQUE (pub_id);

ALTER TABLE ONLY public.collections_templates_categories
    ADD CONSTRAINT collectiontemplatecategories_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections_templates
    ADD CONSTRAINT collectiontemplates_pub_id_key UNIQUE (pub_id);

ALTER TABLE ONLY public.collections_templates
    ADD CONSTRAINT collectiontemplates_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections_automations_logs
    ADD CONSTRAINT collections_automations_logs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections
    ADD CONSTRAINT collections_pub_id_key UNIQUE (pub_id);

ALTER TABLE ONLY public.collections_integrations_logs
    ADD CONSTRAINT collections_integrations_logs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections_items_documents
    ADD CONSTRAINT collections_item_documents_pub_id_key UNIQUE (pub_id);

ALTER TABLE ONLY public.collections_items_documents
    ADD CONSTRAINT collections_item_documents_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections_items_logs
    ADD CONSTRAINT collections_items_logs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections_items_posts
    ADD CONSTRAINT collections_items_posts_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections_logs
    ADD CONSTRAINT collections_logs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections
    ADD CONSTRAINT collections_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.collections_posts
    ADD CONSTRAINT collections_posts_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.documents_tags
    ADD CONSTRAINT documents_tags_pkey PRIMARY KEY (document_id, tag_id);

ALTER TABLE ONLY public.documents_tree
    ADD CONSTRAINT documents_tree_parent_id_child_id_depth_key UNIQUE (parent_id, child_id, depth);

ALTER TABLE ONLY public.documents_tree
    ADD CONSTRAINT documents_tree_pkey PRIMARY KEY (parent_id, child_id);

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT documents_pub_id_key UNIQUE (pub_id);

ALTER TABLE ONLY public.documents_logs
    ADD CONSTRAINT documents_logs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT documents_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.documents_posts
    ADD CONSTRAINT documents_posts_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.documents_exts
    ADD CONSTRAINT documents_exts_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.documents_ints
    ADD CONSTRAINT documents_ints_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.users_groups_workspaces
    ADD CONSTRAINT users_groups_workspaces_pkey PRIMARY KEY (group_id, user_id, workspace_id);

ALTER TABLE ONLY public.groups_tree
    ADD CONSTRAINT groups_tree_parent_id_child_id_depth_key UNIQUE (parent_id, child_id, depth);

ALTER TABLE ONLY public.groups_tree
    ADD CONSTRAINT groups_tree_pkey PRIMARY KEY (parent_id, child_id);

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT groups_pub_id_key UNIQUE (pub_id);

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT groups_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.icons
    ADD CONSTRAINT icons_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.menus_groups
    ADD CONSTRAINT menus_groups_pkey PRIMARY KEY (menu_id, group_id);

ALTER TABLE ONLY public.menus_roles
    ADD CONSTRAINT menus_roles_pkey PRIMARY KEY (role_id, menu_id);

ALTER TABLE ONLY public.menus
    ADD CONSTRAINT menus_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.users_roles
    ADD CONSTRAINT roles_name_key UNIQUE (name);

ALTER TABLE ONLY public.users_roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.documents_auths
    ADD CONSTRAINT documents_auths_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.workspaces
    ADD CONSTRAINT workspaces_pub_id_key UNIQUE (pub_id);

ALTER TABLE ONLY public.workspaces_logs
    ADD CONSTRAINT workspaces_logs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.workspaces
    ADD CONSTRAINT workspaces_name_key UNIQUE (name);

ALTER TABLE ONLY public.workspaces
    ADD CONSTRAINT workspaces_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.workspaces_posts_tree
    ADD CONSTRAINT workspaces_posts_arvore_parent_id_child_id_depth_key UNIQUE (parent_id, child_id, depth);

ALTER TABLE ONLY public.workspaces_posts_tree
    ADD CONSTRAINT workspaces_posts_arvore_pkey PRIMARY KEY (parent_id, child_id);

ALTER TABLE ONLY public.workspaces_posts
    ADD CONSTRAINT workspaces_posts_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT tags_name_key UNIQUE (name);

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT tags_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.timezones
    ADD CONSTRAINT timezones_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.users_roles_workspaces
    ADD CONSTRAINT users_roles_workspaces_pkey PRIMARY KEY (user_id, workspace_id, role_id);

ALTER TABLE ONLY public.users_props_workspaces
    ADD CONSTRAINT users_props_workspaces_pkey PRIMARY KEY (user_id, workspace_id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pub_id_key UNIQUE (pub_id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.documents_versions
    ADD CONSTRAINT documents_vesions_pkey PRIMARY KEY (id);


CREATE INDEX collection_aux_principal_id_idx ON public.collections_auxs USING btree (principal_id);

CREATE INDEX collection_aux_principal_id_order_idx ON public.collections_auxs USING btree (principal_id, "order");

CREATE INDEX collection_user_role_collection_id_group_id_reverse_idx ON public.collections_auths USING btree (collection_id, group_id, reverse);

CREATE INDEX collection_user_role_collection_id_idx ON public.collections_auths USING btree (collection_id);

CREATE INDEX collection_user_role_collection_id_user_id_reverse_idx ON public.collections_auths USING btree (collection_id, user_id, reverse);

CREATE INDEX collection_user_role_group_id_idx ON public.collections_auths USING btree (group_id);

CREATE INDEX collection_user_role_user_id_idx ON public.collections_auths USING btree (user_id);

CREATE INDEX collection_versions_collection_id_idx ON public.collections_versions USING btree (collection_id);

CREATE INDEX collectionarvore_child_id_idx ON public.collections_tree USING btree (child_id);

CREATE INDEX collectionarvore_parent_id_idx ON public.collections_tree USING btree (parent_id);

CREATE INDEX collectionautomations_activities_automation_id_idx ON public.collections_automations_activities USING btree (automation_id);

CREATE INDEX collectionautomations_updated_by_id_idx ON public.collections_automations USING btree (updated_by_id);

CREATE INDEX collectionautomations_collection_id_idx ON public.collections_automations USING btree (collection_id);

CREATE INDEX collectionautomations_created_by_id_idx ON public.collections_automations USING btree (created_by_id);

CREATE INDEX collectionautomations_priority_idx ON public.collections_automations USING btree (priority);

CREATE INDEX collectionautomations_type_priority_idx ON public.collections_automations USING btree (type, priority);

CREATE INDEX collectionautomationsfield_id_idx ON public.collections_automations_fields USING btree (id);

CREATE INDEX collectionautomationsdata_id_idx ON public.collections_automations_dates USING btree (id);

CREATE INDEX collectionautomationslinha_id_idx ON public.collections_automations_rows USING btree (id);

CREATE INDEX collectionintegrations_activities_activity_idx ON public.collections_integrations_activities USING btree (activity_id);

CREATE INDEX collectionintegrations_activities_integration_id_idx ON public.collections_integrations_activities USING btree (integration_id);

CREATE INDEX collectionintegrations_updated_by_id_idx ON public.collections_integrations USING btree (updated_by_id);

CREATE INDEX collectionintegrations_collection_id_idx ON public.collections_integrations USING btree (collection_id);

CREATE INDEX collectionintegrations_created_by_id_idx ON public.collections_integrations USING btree (created_by_id);

CREATE INDEX collectionintegrations_priority_idx ON public.collections_integrations USING btree (priority);

CREATE INDEX collectiontabela_id_idx ON public.collections_schemas USING btree (id);

CREATE INDEX collectiontemplatecategories_updated_by_id_idx ON public.collections_templates_categories USING btree (updated_by_id);

CREATE INDEX collectiontemplatecategories_created_by_id_idx ON public.collections_templates_categories USING btree (created_by_id);

CREATE INDEX collectiontemplatecategories_deleted_by_id_idx ON public.collections_templates_categories USING btree (deleted_by_id);

CREATE INDEX collectiontemplatecategories_workspace_id_idx ON public.collections_templates_categories USING btree (workspace_id);

CREATE INDEX collectiontemplates_updated_by_id_idx ON public.collections_templates USING btree (updated_by_id);

CREATE INDEX collectiontemplates_category_id_idx ON public.collections_templates USING btree (category_id);

CREATE INDEX collectiontemplates_created_by_id_idx ON public.collections_templates USING btree (created_by_id);

CREATE INDEX collectiontemplates_deleted_by_id_idx ON public.collections_templates USING btree (deleted_by_id);

CREATE INDEX collectiontemplates_workspace_id_idx ON public.collections_templates USING btree (workspace_id);

CREATE INDEX collections_updated_by_id_idx ON public.collections USING btree (updated_by_id);

CREATE INDEX collections_automations_logs_automation_id_idx ON public.collections_automations_logs USING btree (automation_id);

CREATE INDEX collections_created_by_id_idx ON public.collections USING btree (created_by_id);

CREATE INDEX collections_integrations_freq_active_idx ON public.collections_integrations USING btree (freq, active);

CREATE INDEX collections_integrations_freq_active_started_idx ON public.collections_integrations USING btree (freq, active, started);

CREATE INDEX collections_integrations_start_started_idx ON public.collections_integrations USING btree (start, started);

CREATE INDEX collections_integrations_logs_integration_id_idx ON public.collections_integrations_logs USING btree (integration_id);

CREATE INDEX collections_integrations_min_of_day_active_idx ON public.collections_integrations USING btree (min_of_day, active);

CREATE INDEX collections_integrations_min_of_day_active_started_idx ON public.collections_integrations USING btree (min_of_day, active, started);

CREATE INDEX collections_integrations_rerun_at_active_started_idx ON public.collections_integrations USING btree (rerun_at, active, started);

CREATE INDEX collections_integrations_active_idx ON public.collections_integrations USING btree (active);

CREATE INDEX collections_item_documents_updated_by_id_idx ON public.collections_items_documents USING btree (updated_by_id);

CREATE INDEX collections_item_documents_collection_id_idx ON public.collections_items_documents USING btree (collection_id);

CREATE INDEX collections_item_documents_created_by_id_idx ON public.collections_items_documents USING btree (created_by_id);

CREATE INDEX collections_item_documents_document_id_idx ON public.collections_items_documents USING btree (document_id);

CREATE INDEX collections_item_documents_item_id_collection_workspace_id_idx ON public.collections_items_documents USING btree (item_id, collection_id, workspace_id);

CREATE INDEX collections_item_documents_deleted_by_id_idx ON public.collections_items_documents USING btree (deleted_by_id);

CREATE INDEX collections_item_documents_workspace_id_idx ON public.collections_items_documents USING btree (workspace_id);

CREATE INDEX collections_items_logs_item_id_idx ON public.collections_items_logs USING btree (item_id);

CREATE INDEX collections_items_posts_item_id_idx ON public.collections_items_posts USING btree (item_id);

CREATE INDEX collections_logs_collection_id_idx ON public.collections_logs USING btree (collection_id);

CREATE INDEX collections_parent_id_idx ON public.collections USING btree (parent_id);

CREATE INDEX collections_posts_collection_id_idx ON public.collections_posts USING btree (collection_id);

CREATE INDEX collections_published_by_id_idx ON public.collections USING btree (published_by_id);

CREATE INDEX collections_deleted_by_id_idx ON public.collections USING btree (deleted_by_id);

CREATE INDEX collections_workspace_id_idx ON public.collections USING btree (workspace_id);

CREATE INDEX collections_workspace_id_type_idx ON public.collections USING btree (workspace_id, type);

CREATE UNIQUE INDEX collections_workspace_parent_name_idx ON public.collections USING btree (workspace_id, parent_id, name);

CREATE INDEX collections_type_idx ON public.collections USING btree (type);

CREATE INDEX documents_tags_document_id_idx ON public.documents_tags USING btree (document_id);

CREATE INDEX documents_tags_tag_id_idx ON public.documents_tags USING btree (tag_id);

CREATE INDEX documents_tree_child_id_idx ON public.documents_tree USING btree (child_id);

CREATE INDEX documents_tree_parent_id_idx ON public.documents_tree USING btree (parent_id);

CREATE INDEX documents_updated_at_idx ON public.documents USING btree (updated_at);

CREATE INDEX documents_updated_by_id_idx ON public.documents USING btree (updated_by_id);

CREATE INDEX documents_created_at_idx ON public.documents USING btree (created_at);

CREATE INDEX documents_created_by_id_idx ON public.documents USING btree (created_by_id);

CREATE INDEX documents_owner_id_idx ON public.documents USING btree (owner_id);

CREATE INDEX documents_extension_idx ON public.documents USING btree (extension);

CREATE INDEX documents_group_id_idx ON public.documents USING btree (group_id);

CREATE INDEX documents_logs_document_id_idx ON public.documents_logs USING btree (document_id);

CREATE INDEX documents_parent_id_idx ON public.documents USING btree (parent_id);

CREATE INDEX documents_posts_document_id_idx ON public.documents_posts USING btree (document_id);

CREATE INDEX documents_provider_name_idx ON public.documents USING btree (provider_id, name);

CREATE UNIQUE INDEX documents_provider_parent_name_idx ON public.documents USING btree (provider_id, parent_id, name);

CREATE INDEX documents_deleted_by_id_idx ON public.documents USING btree (deleted_by_id);

CREATE INDEX documents_workspace_id_idx ON public.documents USING btree (workspace_id);

CREATE INDEX documents_size_idx ON public.documents USING btree (size);

CREATE INDEX users_groups_workspaces_group_id_idx ON public.users_groups_workspaces USING btree (group_id);

CREATE INDEX users_groups_workspaces_workspace_id_idx ON public.users_groups_workspaces USING btree (workspace_id);

CREATE INDEX users_groups_workspaces_user_id_idx ON public.users_groups_workspaces USING btree (user_id);

CREATE INDEX groups_tree_child_id_idx ON public.groups_tree USING btree (child_id);

CREATE INDEX groups_tree_parent_id_idx ON public.groups_tree USING btree (parent_id);

CREATE UNIQUE INDEX groups_name ON public.groups USING btree (workspace_id, parent_id, name);

CREATE INDEX groups_parent_id_idx ON public.groups USING btree (parent_id);

CREATE INDEX groups_deleted_by_id_idx ON public.groups USING btree (deleted_by_id);

CREATE INDEX groups_workspace_id_idx ON public.groups USING btree (workspace_id);

CREATE UNIQUE INDEX icons_name_idx ON public.icons USING btree (name);

CREATE INDEX menus_groups_group_id_idx ON public.menus_groups USING btree (group_id);

CREATE INDEX menus_groups_menu_id_idx ON public.menus_groups USING btree (menu_id);

CREATE INDEX menus_roles_menu_id_idx ON public.menus_roles USING btree (menu_id);

CREATE INDEX menus_roles_role_id_idx ON public.menus_roles USING btree (role_id);

CREATE INDEX menus_parent_id_idx ON public.menus USING btree (parent_id);

CREATE INDEX menus_workspace_id_idx ON public.menus USING btree (workspace_id);

CREATE INDEX roles_above_of_id_idx ON public.users_roles USING btree (above_of_id);

CREATE INDEX roles_deleted_by_id_idx ON public.users_roles USING btree (deleted_by_id);

CREATE INDEX documents_auths_document_id_idx ON public.documents_auths USING btree (document_id);

CREATE INDEX documents_auths_group_id_idx ON public.documents_auths USING btree (group_id);

CREATE INDEX documents_auths_user_id_idx ON public.documents_auths USING btree (user_id);

CREATE INDEX workspaces_logs_workspace_id_date_idx ON public.workspaces_logs USING btree (workspace_id, date);

CREATE INDEX workspaces_logs_workspace_id_id_idx ON public.workspaces_logs USING btree (workspace_id, id);

CREATE INDEX workspaces_logs_workspace_id_idx ON public.workspaces_logs USING btree (workspace_id);

CREATE INDEX workspaces_logs_workspace_id_type_idx ON public.workspaces_logs USING btree (workspace_id, type);

CREATE INDEX workspaces_logs_type_idx ON public.workspaces_logs USING btree (type);

CREATE INDEX workspaces_posts_arvore_child_id_idx ON public.workspaces_posts_tree USING btree (child_id);

CREATE INDEX workspaces_posts_arvore_parent_id_idx ON public.workspaces_posts_tree USING btree (parent_id);

CREATE INDEX workspaces_posts_parent_id_idx ON public.workspaces_posts USING btree (parent_id);

CREATE INDEX workspaces_posts_repo_id_created_at_idx ON public.workspaces_posts USING btree (workspace_id, created_at);

CREATE INDEX workspaces_posts_repo_id_pub_id_idx ON public.workspaces_posts USING btree (workspace_id, pub_id);

CREATE INDEX workspaces_posts_repo_id_idx ON public.workspaces_posts USING btree (workspace_id);

CREATE INDEX workspaces_posts_repo_id_level_idx ON public.workspaces_posts USING btree (workspace_id, level);

CREATE INDEX workspaces_posts_repo_id_parent_id_idx ON public.workspaces_posts USING btree (workspace_id, parent_id);

CREATE INDEX workspaces_posts_repo_id_parent_id_level_idx ON public.workspaces_posts USING btree (workspace_id, parent_id, level);

CREATE INDEX workspaces_posts_repo_id_type_idx ON public.workspaces_posts USING btree (workspace_id, type);

CREATE INDEX workspaces_posts_type_idx ON public.workspaces_posts USING btree (type);

CREATE INDEX workspaces_root_id_idx ON public.workspaces USING btree (root_doc_id);

CREATE INDEX workspaces_deleted_by_id_idx ON public.workspaces USING btree (deleted_by_id);

CREATE INDEX timezones_diff_idx ON public.timezones USING btree (diff);

CREATE INDEX users_roles_workspaces_role_id_idx ON public.users_roles_workspaces USING btree (role_id);

CREATE INDEX users_roles_workspaces_workspace_id_idx ON public.users_roles_workspaces USING btree (workspace_id);

CREATE INDEX users_roles_workspaces_user_id_idx ON public.users_roles_workspaces USING btree (user_id);

CREATE INDEX users_props_workspaces_workspace_id_idx ON public.users_props_workspaces USING btree (workspace_id);

CREATE INDEX users_props_workspaces_user_id_idx ON public.users_props_workspaces USING btree (user_id);

CREATE INDEX users_deleted_by_id_idx ON public.users USING btree (deleted_by_id);

CREATE INDEX documents_vesions_created_by_id_idx ON public.documents_versions USING btree (created_by_id);

CREATE INDEX documents_vesions_document_id_idx ON public.documents_versions USING btree (document_id);

CREATE INDEX workspaces_owner_id_idx ON public.workspaces USING btree (owner_id);

CREATE INDEX documents_collections_integrations_id_idx on public.documents_collections_integrations using btree(document_id, integration_id);

CREATE TRIGGER trig_collections_tree_afterinsert_collections AFTER INSERT ON public.collections FOR EACH ROW EXECUTE PROCEDURE public.proc_collections_tree_afterinsert_collections();

CREATE TRIGGER trig_collections_tree_afterupdate_collections AFTER UPDATE ON public.collections FOR EACH ROW EXECUTE PROCEDURE public.proc_collections_tree_afterupdate_collections();

CREATE TRIGGER trig_documents_tree_afterinsert_documents AFTER INSERT ON public.documents FOR EACH ROW EXECUTE PROCEDURE public.proc_documents_tree_afterinsert_documents();

CREATE TRIGGER trig_documents_tree_afterupdate_documents AFTER UPDATE ON public.documents FOR EACH ROW EXECUTE PROCEDURE public.proc_documents_tree_afterupdate_documents();

CREATE TRIGGER trig_groups_tree_afterinsert_groups AFTER INSERT ON public.groups FOR EACH ROW EXECUTE PROCEDURE public.proc_groups_tree_afterinsert_groups();

CREATE TRIGGER trig_groups_tree_afterupdate_groups AFTER UPDATE ON public.groups FOR EACH ROW EXECUTE PROCEDURE public.proc_groups_tree_afterupdate_groups();

CREATE TRIGGER trig_workspaces_posts_tree_afterinsert_workspaces_posts AFTER INSERT ON public.workspaces_posts FOR EACH ROW EXECUTE PROCEDURE public.proc_workspaces_posts_tree_afterinsert_workspaces_posts();

CREATE TRIGGER trig_workspaces_posts_tree_afterupdate_workspaces_posts AFTER UPDATE ON public.workspaces_posts FOR EACH ROW EXECUTE PROCEDURE public.proc_workspaces_posts_tree_afterupdate_workspaces_posts();

ALTER TABLE ONLY public.collections_auxs
    ADD CONSTRAINT collection_aux_ibfk_0 FOREIGN KEY (principal_id) REFERENCES public.collections(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_auxs
    ADD CONSTRAINT collection_aux_ibfk_1 FOREIGN KEY (aux_id) REFERENCES public.collections(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_auths
    ADD CONSTRAINT collection_user_role_ibfk_0 FOREIGN KEY (collection_id) REFERENCES public.collections(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_auths
    ADD CONSTRAINT collection_user_role_ibfk_1 FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_auths
    ADD CONSTRAINT collection_user_role_ibfk_2 FOREIGN KEY (group_id) REFERENCES public.groups(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_versions
    ADD CONSTRAINT collection_versions_ibfk_0 FOREIGN KEY (collection_id) REFERENCES public.collections(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_versions
    ADD CONSTRAINT collection_versions_ibfk_1 FOREIGN KEY (change_id) REFERENCES public.collections_versions_changes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_tree
    ADD CONSTRAINT collectionarvore_ibfk_1 FOREIGN KEY (parent_id) REFERENCES public.collections(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_tree
    ADD CONSTRAINT collectionarvore_ibfk_2 FOREIGN KEY (child_id) REFERENCES public.collections(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_automations_activities
    ADD CONSTRAINT collectionautomations_activities_ibfk_0 FOREIGN KEY (automation_id) REFERENCES public.collections_automations(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_automations
    ADD CONSTRAINT collectionautomations_ibfk_0 FOREIGN KEY (collection_id) REFERENCES public.collections(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_automations
    ADD CONSTRAINT collectionautomations_ibfk_1 FOREIGN KEY (created_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_automations
    ADD CONSTRAINT collectionautomations_ibfk_2 FOREIGN KEY (updated_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_automations_fields
    ADD CONSTRAINT collectionautomationsfield_ibfk_id FOREIGN KEY (id) REFERENCES public.collections_automations(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_automations_dates
    ADD CONSTRAINT collectionautomationsdata_ibfk_id FOREIGN KEY (id) REFERENCES public.collections_automations(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_automations_rows
    ADD CONSTRAINT collectionautomationslinha_ibfk_id FOREIGN KEY (id) REFERENCES public.collections_automations(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_integrations_activities
    ADD CONSTRAINT collectionintegrations_activities_ibfk_0 FOREIGN KEY (integration_id) REFERENCES public.collections_integrations(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_integrations
    ADD CONSTRAINT collectionintegrations_ibfk_0 FOREIGN KEY (collection_id) REFERENCES public.collections(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_integrations
    ADD CONSTRAINT collectionintegrations_ibfk_1 FOREIGN KEY (created_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_integrations
    ADD CONSTRAINT collectionintegrations_ibfk_2 FOREIGN KEY (updated_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_schemas
    ADD CONSTRAINT collectiontabela_ibfk_id FOREIGN KEY (id) REFERENCES public.collections(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_templates_categories
    ADD CONSTRAINT collectiontemplatecategories_ibfk_0 FOREIGN KEY (workspace_id) REFERENCES public.workspaces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_templates_categories
    ADD CONSTRAINT collectiontemplatecategories_ibfk_1 FOREIGN KEY (created_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_templates_categories
    ADD CONSTRAINT collectiontemplatecategories_ibfk_2 FOREIGN KEY (updated_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_templates_categories
    ADD CONSTRAINT collectiontemplatecategories_ibfk_3 FOREIGN KEY (deleted_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_templates
    ADD CONSTRAINT collectiontemplates_ibfk_0 FOREIGN KEY (workspace_id) REFERENCES public.workspaces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_templates
    ADD CONSTRAINT collectiontemplates_ibfk_1 FOREIGN KEY (created_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_templates
    ADD CONSTRAINT collectiontemplates_ibfk_2 FOREIGN KEY (updated_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_templates
    ADD CONSTRAINT collectiontemplates_ibfk_3 FOREIGN KEY (deleted_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_templates
    ADD CONSTRAINT collectiontemplates_ibfk_4 FOREIGN KEY (category_id) REFERENCES public.collections_templates_categories(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_automations_logs
    ADD CONSTRAINT collections_automations_logs_ibfk_0 FOREIGN KEY (id) REFERENCES public.collections_logs(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.collections_automations_logs
    ADD CONSTRAINT collections_automations_logs_ibfk_1 FOREIGN KEY (automation_id) REFERENCES public.collections_automations(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.collections
    ADD CONSTRAINT collections_ibfk_0 FOREIGN KEY (workspace_id) REFERENCES public.workspaces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections
    ADD CONSTRAINT collections_ibfk_1 FOREIGN KEY (created_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections
    ADD CONSTRAINT collections_ibfk_2 FOREIGN KEY (updated_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections
    ADD CONSTRAINT collections_ibfk_3 FOREIGN KEY (deleted_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections
    ADD CONSTRAINT collections_ibfk_parent FOREIGN KEY (parent_id) REFERENCES public.collections(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections
    ADD CONSTRAINT collections_ibfk_published_by_id FOREIGN KEY (published_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_integrations_logs
    ADD CONSTRAINT collections_integrations_logs_ibfk_0 FOREIGN KEY (id) REFERENCES public.collections_logs(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.collections_integrations_logs
    ADD CONSTRAINT collections_integrations_logs_ibfk_1 FOREIGN KEY (integration_id) REFERENCES public.collections_integrations(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.collections_items_documents
    ADD CONSTRAINT collections_item_documents_ibfk_0 FOREIGN KEY (collection_id) REFERENCES public.collections(id) ON DELETE SET NULL DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_items_documents
    ADD CONSTRAINT collections_item_documents_ibfk_1 FOREIGN KEY (created_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_items_documents
    ADD CONSTRAINT collections_item_documents_ibfk_2 FOREIGN KEY (updated_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_items_documents
    ADD CONSTRAINT collections_item_documents_ibfk_3 FOREIGN KEY (workspace_id) REFERENCES public.workspaces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_items_documents
    ADD CONSTRAINT collections_item_documents_ibfk_4 FOREIGN KEY (deleted_by_id) REFERENCES public.users(id) ON DELETE SET NULL DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.collections_items_documents
    ADD CONSTRAINT collections_item_documents_ibfk_5 FOREIGN KEY (document_id) REFERENCES public.documents(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.collections_items_logs
    ADD CONSTRAINT collections_items_logs_ibfk_0 FOREIGN KEY (id) REFERENCES public.collections_logs(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.collections_items_posts
    ADD CONSTRAINT collections_items_posts_ibfk_0 FOREIGN KEY (id) REFERENCES public.collections_posts(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.collections_logs
    ADD CONSTRAINT collections_logs_ibfk_0 FOREIGN KEY (id) REFERENCES public.workspaces_logs(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.collections_logs
    ADD CONSTRAINT collections_logs_ibfk_1 FOREIGN KEY (collection_id) REFERENCES public.collections(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.collections_posts
    ADD CONSTRAINT collections_posts_ibfk_0 FOREIGN KEY (id) REFERENCES public.workspaces_posts(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.collections_posts
    ADD CONSTRAINT collections_posts_ibfk_1 FOREIGN KEY (collection_id) REFERENCES public.collections(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.documents_tags
    ADD CONSTRAINT documents_tags_ibfk_1 FOREIGN KEY (document_id) REFERENCES public.documents(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents_tags
    ADD CONSTRAINT documents_tags_ibfk_2 FOREIGN KEY (tag_id) REFERENCES public.tags(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents_tree
    ADD CONSTRAINT documents_tree_ibfk_1 FOREIGN KEY (parent_id) REFERENCES public.documents(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents_tree
    ADD CONSTRAINT documents_tree_ibfk_2 FOREIGN KEY (child_id) REFERENCES public.documents(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT documents_ibfk_1 FOREIGN KEY (workspace_id) REFERENCES public.workspaces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT documents_ibfk_3 FOREIGN KEY (created_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT documents_ibfk_4 FOREIGN KEY (updated_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT documents_ibfk_owner FOREIGN KEY (owner_id) REFERENCES public.users(id) ON DELETE SET NULL DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT documents_ibfk_group FOREIGN KEY (group_id) REFERENCES public.groups(id) ON DELETE SET NULL DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT documents_ibfk_parent FOREIGN KEY (parent_id) REFERENCES public.documents(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT documents_ibfk_provider FOREIGN KEY (provider_id) REFERENCES public.providers(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT documents_ibfk_deleted_by_id FOREIGN KEY (deleted_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents_logs
    ADD CONSTRAINT documents_logs_ibfk_0 FOREIGN KEY (id) REFERENCES public.workspaces_logs(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.documents_logs
    ADD CONSTRAINT documents_logs_ibfk_1 FOREIGN KEY (document_id) REFERENCES public.documents(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.documents_posts
    ADD CONSTRAINT documents_posts_ibfk_0 FOREIGN KEY (id) REFERENCES public.workspaces_posts(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.documents_posts
    ADD CONSTRAINT documents_posts_ibfk_1 FOREIGN KEY (document_id) REFERENCES public.documents(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.documents_exts
    ADD CONSTRAINT documents_exts_ibfk_0 FOREIGN KEY (id) REFERENCES public.documents(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents_ints
    ADD CONSTRAINT documents_ints_ibfk_0 FOREIGN KEY (id) REFERENCES public.documents(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents_ints
    ADD CONSTRAINT documents_ints_ibfk_7 FOREIGN KEY (locked_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents_ints
    ADD CONSTRAINT documents_ints_ibfk_8 FOREIGN KEY (checked_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.users_groups_workspaces
    ADD CONSTRAINT users_groups_workspaces_ibfk_1 FOREIGN KEY (group_id) REFERENCES public.groups(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.users_groups_workspaces
    ADD CONSTRAINT users_groups_workspaces_ibfk_2 FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.users_groups_workspaces
    ADD CONSTRAINT users_groups_workspaces_ibfk_3 FOREIGN KEY (workspace_id) REFERENCES public.workspaces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.groups_tree
    ADD CONSTRAINT groups_tree_ibfk_1 FOREIGN KEY (parent_id) REFERENCES public.groups(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.groups_tree
    ADD CONSTRAINT groups_tree_ibfk_2 FOREIGN KEY (child_id) REFERENCES public.groups(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT groups_ibfk_1 FOREIGN KEY (parent_id) REFERENCES public.groups(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT groups_ibfk_2 FOREIGN KEY (workspace_id) REFERENCES public.workspaces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT groups_ibfk_created_by FOREIGN KEY (created_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT groups_ibfk_deleted_by_id FOREIGN KEY (deleted_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT groups_ibfk_updated_by FOREIGN KEY (updated_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.menus_groups
    ADD CONSTRAINT menus_groups_ibfk_1 FOREIGN KEY (group_id) REFERENCES public.groups(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.menus_groups
    ADD CONSTRAINT menus_groups_ibfk_2 FOREIGN KEY (menu_id) REFERENCES public.menus(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.menus_roles
    ADD CONSTRAINT menus_roles_ibfk_1 FOREIGN KEY (role_id) REFERENCES public.users_roles(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.menus_roles
    ADD CONSTRAINT menus_roles_ibfk_2 FOREIGN KEY (menu_id) REFERENCES public.menus(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.menus
    ADD CONSTRAINT menus_ibfk_1 FOREIGN KEY (parent_id) REFERENCES public.menus(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.menus
    ADD CONSTRAINT menus_ibfk_workspace_id FOREIGN KEY (workspace_id) REFERENCES public.workspaces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.users_roles
    ADD CONSTRAINT roles_ibfk_1 FOREIGN KEY (above_of_id) REFERENCES public.users_roles(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.users_roles
    ADD CONSTRAINT roles_ibfk_deleted_by_id FOREIGN KEY (deleted_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents_auths
    ADD CONSTRAINT documents_auths_ibfk_1 FOREIGN KEY (document_id) REFERENCES public.documents(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents_auths
    ADD CONSTRAINT documents_auths_ibfk_2 FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents_auths
    ADD CONSTRAINT documents_auths_ibfk_3 FOREIGN KEY (group_id) REFERENCES public.groups(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.workspaces
    ADD CONSTRAINT workspaces_ibfk_1 FOREIGN KEY (root_doc_id) REFERENCES public.documents(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.workspaces
    ADD CONSTRAINT workspaces_ibfk_provider FOREIGN KEY (provider_id) REFERENCES public.providers(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.workspaces
    ADD CONSTRAINT workspaces_ibfk_deleted_by_id FOREIGN KEY (deleted_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.workspaces
    ADD CONSTRAINT workspaces_ibfk_updated_by_id FOREIGN KEY (updated_by_id) REFERENCES public.users(id) on delete set null;

ALTER TABLE ONLY public.workspaces_logs
    ADD CONSTRAINT workspaces_logs_ibfk_0 FOREIGN KEY (workspace_id) REFERENCES public.workspaces(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.workspaces_logs
    ADD CONSTRAINT workspaces_logs_ibfk_1 FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.workspaces_posts_tree
    ADD CONSTRAINT workspaces_posts_arvore_ibfk_1 FOREIGN KEY (parent_id) REFERENCES public.workspaces_posts(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.workspaces_posts_tree
    ADD CONSTRAINT workspaces_posts_arvore_ibfk_2 FOREIGN KEY (child_id) REFERENCES public.workspaces_posts(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.workspaces_posts
    ADD CONSTRAINT workspaces_posts_ibfk_0 FOREIGN KEY (workspace_id) REFERENCES public.workspaces(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.workspaces_posts
    ADD CONSTRAINT workspaces_posts_ibfk_1 FOREIGN KEY (parent_id) REFERENCES public.workspaces_posts(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.users_roles_workspaces
    ADD CONSTRAINT users_roles_workspaces_ibfk_1 FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.users_roles_workspaces
    ADD CONSTRAINT users_roles_workspaces_ibfk_2 FOREIGN KEY (workspace_id) REFERENCES public.workspaces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.users_roles_workspaces
    ADD CONSTRAINT users_roles_workspaces_ibfk_3 FOREIGN KEY (role_id) REFERENCES public.users_roles(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.users_props_workspaces
    ADD CONSTRAINT users_props_workspaces_ibfk_1 FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.users_props_workspaces
    ADD CONSTRAINT users_props_workspaces_ibfk_2 FOREIGN KEY (workspace_id) REFERENCES public.workspaces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_ibfk_deleted_by_id FOREIGN KEY (deleted_by_id) REFERENCES public.users(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_ibfk_updated_by_id FOREIGN KEY (updated_by_id) REFERENCES public.users(id) on delete set null;

ALTER TABLE ONLY public.documents_versions
    ADD CONSTRAINT documents_vesions_ibfk_1 FOREIGN KEY (document_id) REFERENCES public.documents(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents_versions
    ADD CONSTRAINT documents_vesions_ibfk_2 FOREIGN KEY (created_by_id) REFERENCES public.users(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.workspaces
    ADD CONSTRAINT workspaces_owner_id_fk FOREIGN KEY (owner_id) REFERENCES public.users(id) ON DELETE SET NULL DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.objects_schemas
    ADD CONSTRAINT objects_schemas_ibfk_0 FOREIGN KEY (created_by_id) REFERENCES public.users (id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE public.objects_schemas 
    ADD CONSTRAINT objects_schemas_ibfk_1 FOREIGN KEY (updated_by_id) REFERENCES public.users (id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE public.objects_schemas 
    ADD CONSTRAINT objects_schemas_ibfk_2 FOREIGN KEY (deleted_by_id) REFERENCES public.users (id) DEFERRABLE INITIALLY DEFERRED;
CREATE INDEX ON public.objects_schemas (deleted_by_id);
CREATE INDEX ON public.objects_schemas (name);
CREATE INDEX ON public.objects_schemas (category);

ALTER TABLE public.activities_schemas
    ADD CONSTRAINT activities_schemas_ibfk_0 FOREIGN KEY (id) REFERENCES public.objects_schemas ON DELETE CASCADE;

ALTER TABLE public.credentials_schemas
    ADD CONSTRAINT credentials_schemas_ibfk_0 FOREIGN KEY (id) REFERENCES public.objects_schemas ON DELETE CASCADE;

create index on public.credentials_schemas(vendor);
create index on public.credentials_schemas(mode);

ALTER TABLE public.providers_schemas
    ADD CONSTRAINT providers_schemas_ibfk_0 FOREIGN KEY (id) REFERENCES public.objects_schemas ON DELETE CASCADE;

create index on public.providers_schemas(vendor);

ALTER TABLE public.triggers_schemas
    ADD CONSTRAINT triggers_schemas_ibfk_0 FOREIGN KEY (id) REFERENCES public.objects_schemas ON DELETE CASCADE;

ALTER TABLE public.objects
    ADD CONSTRAINT objects_ibfk_0 FOREIGN KEY (created_by_id) REFERENCES public.users (id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE public.objects
    ADD CONSTRAINT objects_ibfk_1 FOREIGN KEY (updated_by_id) REFERENCES public.users (id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE public.objects
    ADD CONSTRAINT objects_ibfk_2 FOREIGN KEY (deleted_by_id) REFERENCES public.users (id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE public.objects
    ADD CONSTRAINT objects_ibfk_3 FOREIGN KEY (workspace_id) REFERENCES public.workspaces (id) on delete cascade;

CREATE INDEX ON public.objects (deleted_by_id);
CREATE INDEX ON public.objects (pub_id, workspace_id);
CREATE INDEX ON public.objects (created_at, workspace_id);

ALTER TABLE public.activities
    ADD CONSTRAINT activities_ibfk_0 FOREIGN KEY (id) REFERENCES public.objects ON DELETE CASCADE;
ALTER TABLE public.activities
    ADD CONSTRAINT activities_ibfk_schema_id FOREIGN KEY (schema_id) REFERENCES public.objects_schemas (id) on delete cascade;

ALTER TABLE public.credentials
    ADD CONSTRAINT credentials_ibfk_0 FOREIGN KEY (id) REFERENCES public.objects ON DELETE CASCADE;
ALTER TABLE public.credentials
    ADD CONSTRAINT credentials_ibfk_schema_id FOREIGN KEY (schema_id) REFERENCES public.objects_schemas (id) on delete cascade;

ALTER TABLE public.providers
    ADD CONSTRAINT providers_ibfk_0 FOREIGN KEY (id) REFERENCES public.objects ON DELETE CASCADE;
ALTER TABLE public.providers
    ADD CONSTRAINT providers_ibfk_schema_id FOREIGN KEY (schema_id) REFERENCES public.objects_schemas (id) on delete cascade;

ALTER TABLE public.triggers
    ADD CONSTRAINT triggers_ibfk_0 FOREIGN KEY (id) REFERENCES public.objects ON DELETE CASCADE;
ALTER TABLE public.triggers
    ADD CONSTRAINT triggers_ibfk_schema_id FOREIGN KEY (schema_id) REFERENCES public.objects_schemas (id) on delete cascade;

ALTER TABLE public.collections_automations_activities
    ADD CONSTRAINT collections_automations_activities_ibfk_a FOREIGN KEY (activity_id) REFERENCES public.activities ON DELETE CASCADE;
ALTER TABLE public.collections_automations_activities
    ADD CONSTRAINT collections_automations_activities_ibfk_b FOREIGN KEY (state_id) REFERENCES public.objects_states DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.collections_integrations_activities
    ADD CONSTRAINT collections_integrations_activities_ibfk_a FOREIGN KEY (activity_id) REFERENCES public.activities ON DELETE CASCADE;
ALTER TABLE public.collections_integrations_activities
    ADD CONSTRAINT collections_integrations_activities_ibfk_b FOREIGN KEY (state_id) REFERENCES public.objects_states DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ONLY public.documents_collections_integrations
    ADD CONSTRAINT documents_collections_integrations_ibfk_0 FOREIGN KEY (document_id) REFERENCES public.documents(id) on delete cascade;
ALTER TABLE ONLY public.documents_collections_integrations
    ADD CONSTRAINT documents_collections_integrations_ibfk_1 FOREIGN KEY (integration_id) REFERENCES public.collections_integrations(id) on delete cascade;


insert into "collections_versions_changes" 
    ("id", "desc")
    values
    (1, 'General'),
    (2, 'Fields'),
    (3, 'Classes'),
    (4, 'Constants'),
    (5, 'Indexes'),
    (6, 'Flows');

ALTER DATABASE robotikflow OWNER TO botflow_user;

GRANT ALL ON ALL TABLES IN SCHEMA public TO botflow_user;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO botflow_user;


--only in dev mode: GRANT ALL ON SCHEMA public TO botflow_user;

