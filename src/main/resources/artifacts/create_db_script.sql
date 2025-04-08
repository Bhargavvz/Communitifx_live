-- Table creation script for CoFix application
-- Modified for use with existing Render PostgreSQL database

-- Create tables if they don't exist

-- Users table
CREATE TABLE IF NOT EXISTS public.users (
    email character varying(255) NOT NULL,
    address character varying(255),
    country character varying(255),
    create_date timestamp(6) without time zone,
    gender character varying(255),
    name character varying(255),
    nick_name character varying(255),
    password character varying(255),
    phone_number character varying(255),
    CONSTRAINT users_pkey PRIMARY KEY (email)
);

-- Admin users table
CREATE TABLE IF NOT EXISTS public.admin_users (
    email character varying(255) NOT NULL,
    admin_level integer,
    create_date timestamp(6) without time zone,
    name character varying(255),
    password character varying(255),
    CONSTRAINT admin_users_pkey PRIMARY KEY (email)
);

-- Posts table
CREATE TABLE IF NOT EXISTS public.posts (
    email character varying(255) NOT NULL,
    post_id bigint NOT NULL,
    benefit_type character varying(255),
    comment character varying(255),
    create_date timestamp(6) without time zone,
    description character varying(2000),
    image character varying(255),
    latitude double precision,
    longitude double precision,
    status character varying(255),
    title character varying(255),
    type character varying(255),
    update_date timestamp(6) without time zone,
    CONSTRAINT posts_pkey PRIMARY KEY (email, post_id)
);

-- Reviews table
CREATE TABLE IF NOT EXISTS public.reviews (
    email character varying(255) NOT NULL,
    post_id bigint NOT NULL,
    create_date timestamp(6) without time zone,
    description character varying(255),
    rating integer,
    CONSTRAINT reviews_pkey PRIMARY KEY (email, post_id)
);

-- Forums table
CREATE TABLE IF NOT EXISTS public.forums (
    id bigint NOT NULL,
    create_date timestamp(6) without time zone,
    description character varying(2000),
    title character varying(255),
    votes integer,
    CONSTRAINT forums_pkey PRIMARY KEY (id)
);

-- Forum votes table
CREATE TABLE IF NOT EXISTS public.forum_votes (
    forum_id bigint NOT NULL,
    user_email character varying(255) NOT NULL,
    vote integer,
    CONSTRAINT forum_votes_pkey PRIMARY KEY (forum_id, user_email)
);

-- Volunteer opportunities table
CREATE TABLE IF NOT EXISTS public.volunteer_opportunities (
    id bigint NOT NULL,
    create_date timestamp(6) without time zone,
    description character varying(2000),
    location character varying(255),
    title character varying(255),
    CONSTRAINT volunteer_opportunities_pkey PRIMARY KEY (id)
);

-- Volunteer registrations table
CREATE TABLE IF NOT EXISTS public.volunteer_registrations (
    opportunity_id bigint NOT NULL,
    volunteer_email character varying(255) NOT NULL,
    registration_date timestamp(6) without time zone,
    CONSTRAINT volunteer_registrations_pkey PRIMARY KEY (opportunity_id, volunteer_email)
);

-- Add foreign key constraints
ALTER TABLE IF NOT EXISTS public.posts
    ADD CONSTRAINT fk_posts_users FOREIGN KEY (email) REFERENCES public.users(email) ON DELETE CASCADE;

ALTER TABLE IF NOT EXISTS public.reviews
    ADD CONSTRAINT fk_reviews_posts FOREIGN KEY (email, post_id) REFERENCES public.posts(email, post_id) ON DELETE CASCADE;

ALTER TABLE IF NOT EXISTS public.forum_votes
    ADD CONSTRAINT fk_forum_votes_forums FOREIGN KEY (forum_id) REFERENCES public.forums(id) ON DELETE CASCADE;

ALTER TABLE IF NOT EXISTS public.forum_votes
    ADD CONSTRAINT fk_forum_votes_users FOREIGN KEY (user_email) REFERENCES public.users(email) ON DELETE CASCADE;

ALTER TABLE IF NOT EXISTS public.volunteer_registrations
    ADD CONSTRAINT fk_volunteer_registrations_opportunities FOREIGN KEY (opportunity_id) REFERENCES public.volunteer_opportunities(id) ON DELETE CASCADE;

ALTER TABLE IF NOT EXISTS public.volunteer_registrations
    ADD CONSTRAINT fk_volunteer_registrations_users FOREIGN KEY (volunteer_email) REFERENCES public.users(email) ON DELETE CASCADE;
