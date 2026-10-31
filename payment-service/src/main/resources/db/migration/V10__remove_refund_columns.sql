ALTER TABLE public.payments
    DROP COLUMN IF EXISTS refunded_amount;

ALTER TABLE public.payments
    DROP COLUMN IF EXISTS razorpay_refund_id;
ALTER TABLE public.payments
    ADD COLUMN wallet_credited BOOLEAN NOT NULL DEFAULT FALSE;