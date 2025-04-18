----
---- PostgreSQL database dump
----
--
---- Dumped from database version 15.2
---- Dumped by pg_dump version 15.0
--
---- Started on 2024-07-04 21:46:50
--
--SET statement_timeout = 0;
--SET lock_timeout = 0;
--SET idle_in_transaction_session_timeout = 0;
--SET client_encoding = 'UTF8';
--SET standard_conforming_strings = on;
--SELECT pg_catalog.set_config('search_path', '', false);
--SET check_function_bodies = false;
--SET xmloption = content;
--SET client_min_messages = warning;
--SET row_security = off;
--
--DROP DATABASE cofixdb;
----
---- TOC entry 3336 (class 1262 OID 34755)
---- Name: cofixdb; Type: DATABASE; Schema: -; Owner: postgres
----
--
--CREATE DATABASE cofixdb WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'English_United States.1252';
--
--
--ALTER DATABASE cofixdb OWNER TO postgres;
--
--\connect cofixdb

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

--
-- TOC entry 843 (class 1247 OID 34772)
-- Name: Location; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."Location" AS (
	lat double precision,
	lng double precision
);


ALTER TYPE public."Location" OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 217 (class 1259 OID 34804)
-- Name: posts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.posts (
    email text NOT NULL,
    post_id integer NOT NULL,
    benefit_type text,
    scheme_name text,
    description text,
    image text,
    issue_name text,
    activity_description text,
    latitude double precision,
    longitude double precision,
    comment text,
    create_date TIMESTAMP,
    urgency VARCHAR(20),
    status VARCHAR(20) DEFAULT 'PENDING'
);


ALTER TABLE public.posts OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 34803)
-- Name: posts_post_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.posts_post_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.posts_post_id_seq OWNER TO postgres;

--
-- TOC entry 3337 (class 0 OID 0)
-- Dependencies: 216
-- Name: posts_post_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.posts_post_id_seq OWNED BY public.posts.post_id;


--
-- TOC entry 219 (class 1259 OID 34859)
-- Name: reviews; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.reviews (
    review_id integer NOT NULL,
    email text,
    name text,
    message text,
    create_date timestamp with time zone
);


ALTER TABLE public.reviews OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 34858)
-- Name: reviews_review_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.reviews_review_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.reviews_review_id_seq OWNER TO postgres;

--
-- TOC entry 3348 (class 0 OID 0)
-- Dependencies: 218
-- Name: reviews_review_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.reviews_review_id_seq OWNED BY public.reviews.review_id;
--
-- TOC entry 3187 (class 2604 OID 34862)
-- Name: reviews review_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reviews ALTER COLUMN review_id SET DEFAULT nextval('public.reviews_review_id_seq'::regclass);

--
-- TOC entry 214 (class 1259 OID 34756)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    email text NOT NULL,
    name text,
    password text,
    nick_name text,
    phone_number text,
    country text,
    gender text,
    address text,
    create_date TIMESTAMP
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 3181 (class 2604 OID 34807)
-- Name: posts post_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.posts ALTER COLUMN post_id SET DEFAULT nextval('public.posts_post_id_seq'::regclass);


--
-- TOC entry 3330 (class 0 OID 34804)
-- Dependencies: 217
-- Data for Name: posts; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3328 (class 0 OID 34756)
-- Dependencies: 214
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 3338 (class 0 OID 0)
-- Dependencies: 216
-- Name: posts_post_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.posts_post_id_seq', 1, false);


--
-- TOC entry 3185 (class 2606 OID 34811)
-- Name: posts posts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT posts_pkey PRIMARY KEY (email, post_id);


--
-- TOC entry 3183 (class 2606 OID 34762)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (email);

-- Create admin users table if not exists
CREATE TABLE IF NOT EXISTS public.admin_users (
    email VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    admin_level INTEGER NOT NULL CHECK (admin_level BETWEEN 1 AND 4),
    admin_code VARCHAR(6) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    issues_resolved INTEGER DEFAULT 0
);

-- Add new columns to posts table if they don't exist
DO $$ 
BEGIN 
    BEGIN
        ALTER TABLE public.posts 
        ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PENDING',
        ADD COLUMN IF NOT EXISTS urgency VARCHAR(20) DEFAULT 'MEDIUM';
    EXCEPTION
        WHEN duplicate_column THEN 
            RAISE NOTICE 'columns already exist';
    END;
END $$;

-- Completed on 2024-07-04 21:46:51

--
-- PostgreSQL database dump complete
--

-- Update users table create_date column
ALTER TABLE public.users 
ALTER COLUMN create_date TYPE TIMESTAMP;

-- Add at the end of your create_db_script.sql
ALTER TABLE public.posts 
ADD COLUMN resolution_description TEXT,
ADD COLUMN resolution_image TEXT;

-- First ensure post_id is a primary key in posts table
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'posts_post_id_pkey'
    ) THEN
        ALTER TABLE public.posts
        DROP CONSTRAINT IF EXISTS posts_pkey,
        ADD CONSTRAINT posts_post_id_pkey PRIMARY KEY (post_id);
    END IF;
END $$;

-- Forums functionality using existing posts table
ALTER TABLE public.posts 
ADD COLUMN IF NOT EXISTS is_forum BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS upvotes INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS downvotes INTEGER DEFAULT 0;

-- Volunteer opportunities using existing posts table 
ALTER TABLE public.posts
ADD COLUMN IF NOT EXISTS is_volunteer BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS event_date DATE,
ADD COLUMN IF NOT EXISTS start_time TIME,
ADD COLUMN IF NOT EXISTS end_time TIME,
ADD COLUMN IF NOT EXISTS spots_available INTEGER,
ADD COLUMN IF NOT EXISTS max_spots INTEGER,
ADD COLUMN IF NOT EXISTS required_skills TEXT;

-- Volunteer registrations using existing reviews table
ALTER TABLE public.reviews
ADD COLUMN IF NOT EXISTS is_volunteer_registration BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS post_id INTEGER,
ADD CONSTRAINT fk_volunteer_post 
    FOREIGN KEY (post_id) 
    REFERENCES public.posts(post_id)
    ON DELETE CASCADE;

ALTER TABLE public.posts 
ADD COLUMN IF NOT EXISTS resolution_description TEXT,
ADD COLUMN IF NOT EXISTS resolution_image VARCHAR(255); 