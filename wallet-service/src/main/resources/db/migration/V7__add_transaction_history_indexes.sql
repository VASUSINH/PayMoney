CREATE INDEX IF NOT EXISTS idx_transactions_sender_created
    ON public.transactions (sender_wallet_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_transactions_receiver_created
    ON public.transactions (receiver_wallet_id, created_at DESC);