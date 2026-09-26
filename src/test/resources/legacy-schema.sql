-- the schema the bot created before the migrations (1.1.0 and earlier), as pg_dump printed it
-- plus a table left behind by a removed feature, which the first migration drops

CREATE TABLE public.bannedusers (
    user_id bigint NOT NULL,
    username character varying(32),
    first_name character varying(64) NOT NULL,
    reason character varying(150),
    banned_at timestamp without time zone NOT NULL
);
CREATE TABLE public.chatallowlist (
    chat_id bigint NOT NULL,
    title character varying(70),
    created_at timestamp without time zone NOT NULL
);
CREATE TABLE public.chathistory (
    id integer NOT NULL,
    user_id bigint NOT NULL,
    role character varying(30) NOT NULL,
    content text NOT NULL,
    created_at timestamp without time zone NOT NULL
);
CREATE SEQUENCE public.chathistory_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE public.chathistory_id_seq OWNED BY public.chathistory.id;
CREATE TABLE public.commandsstate (
    command_name character varying(40) NOT NULL,
    is_disabled boolean NOT NULL,
    updated_at timestamp without time zone,
    created_at timestamp without time zone NOT NULL
);
CREATE TABLE public.configurations (
    key character varying(50) NOT NULL,
    value character varying(250) NOT NULL,
    updated_at timestamp without time zone,
    created_at timestamp without time zone NOT NULL
);
CREATE TABLE public.slowmode (
    user_id bigint NOT NULL,
    usage_count integer DEFAULT 1 NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    created_at timestamp without time zone NOT NULL
);
CREATE TABLE public.sudoers (
    user_id bigint NOT NULL,
    username character varying(32),
    created_at timestamp without time zone NOT NULL
);
ALTER TABLE ONLY public.chathistory ALTER COLUMN id SET DEFAULT nextval('public.chathistory_id_seq'::regclass);
ALTER TABLE ONLY public.bannedusers
    ADD CONSTRAINT bannedusers_pkey PRIMARY KEY (user_id);
ALTER TABLE ONLY public.chatallowlist
    ADD CONSTRAINT chatallowlist_pkey PRIMARY KEY (chat_id);
ALTER TABLE ONLY public.chathistory
    ADD CONSTRAINT chathistory_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.commandsstate
    ADD CONSTRAINT commandsstate_pkey PRIMARY KEY (command_name);
ALTER TABLE ONLY public.configurations
    ADD CONSTRAINT configurations_pkey PRIMARY KEY (key);
ALTER TABLE ONLY public.slowmode
    ADD CONSTRAINT slowmode_pkey PRIMARY KEY (user_id);
ALTER TABLE ONLY public.sudoers
    ADD CONSTRAINT sudoers_pkey PRIMARY KEY (user_id);
CREATE INDEX chathistory_role ON public.chathistory USING btree (role);
CREATE INDEX chathistory_user_id ON public.chathistory USING btree (user_id);

CREATE TABLE public.apikeys (
    provider character varying(40) NOT NULL,
    api_key character varying(150) NOT NULL
);
