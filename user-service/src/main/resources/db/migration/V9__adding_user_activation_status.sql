
ALTER TABLE public.users
    ADD COLUMN IF NOT EXISTS account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE public.users
    ADD CONSTRAINT chk_users_account_status
        CHECK (account_status IN ('ACTIVE', 'DEACTIVATED'));
