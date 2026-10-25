ALTER TABLE public.wallets
    ADD COLUMN name VARCHAR(255);

UPDATE public.wallets w
SET name = u.name
FROM public.users u
WHERE w.user_id = u.user_id;

ALTER TABLE public.wallets
    ALTER COLUMN name SET NOT NULL;