ALTER TABLE public.posts 
ADD COLUMN IF NOT EXISTS resolution_description TEXT,
ADD COLUMN IF NOT EXISTS resolution_image VARCHAR(255); 