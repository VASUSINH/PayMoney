# Run & test PayMoney

## 1. Run
Requirements: Docker Desktop (running). Nothing else (Maven/Java run inside Docker).

```bash
# in the project folder (where docker-compose.yml is)
docker compose --env-file environmentFile.env up --build
```
First build takes several minutes. Wait until you see all services registered at http://localhost:8761
(USER-SERVICE, WALLET-SERVICE, PAYMENT-SERVICE, AUDIT-SERVICE, API-GATEWAY).

Stop: `Ctrl+C`, then `docker compose down` (add `-v` to also delete the databases).
Logs of one service: `docker compose logs -f wallet-service`

## 2. Test (use Postman, or curl in Git Bash). Base URL = gateway: http://localhost:8080

### Register two users
POST /api/v1/register-user
```json
{ "name": "Alice", "email": "alice@test.com", "password": "alice1234" }
```
Do the same for `bob@test.com`. A wallet is created asynchronously through Kafka (1-2 seconds).

### Login (save the token)
POST /api/v1/auth/login
```json
{ "email": "alice@test.com", "password": "alice1234" }
```
Response: `{ "token": "..." }`. For every next request add header `Authorization: Bearer <token>`.

### Wallet
GET /api/v1/wallet  -> shows walletId, balance 0. Log in as Bob and note Bob's walletId too.

### Add money (Razorpay test mode)
1. POST /api/v1/payment-links  body `{ "amount": 500 }` -> returns `paymentLinkUrl`.
2. Open the URL in a browser and pay with a Razorpay TEST card/UPI.
3. Razorpay redirects to `APP_PUBLIC_BASE_URL/api/v1/payment-links/callback` (needs a public URL such as a cloudflared tunnel to localhost:8080) and the wallet is credited.
4. GET /api/v1/wallet -> balance 500.

(No Razorpay? Skip to the DB shortcut below.)

### Transfer (needs Idempotency-Key header)
POST /api/v1/transfers   headers: Authorization + `Idempotency-Key: t-001`
```json
{ "senderWalletId": 1, "receiverWalletId": 2, "amount": 100 }
```
* Send the SAME request again with the same key -> same transaction returned, money moves only once.
* Same key, different amount -> 400 "already used for a different request".
* Sender wallet that is not yours -> 400.
* 6th transfer within a minute -> "flagged for fraud review".

### Withdraw
POST /api/v1/withdrawals  headers: Authorization + `Idempotency-Key: w-001`
```json
{ "amount": 50 }
```

### History
GET /api/v1/wallet/transactions?page=0&size=10
GET /api/v1/transactions/{transactionId}

### Account status
PATCH /api/v1/users/me/deactivate -> then any API call returns 403; login fails.
POST /api/v1/auth/reactivate `{ "email": "...", "password": "..." }`

### Admin
Login with PAYMONEY_ADMIN_EMAIL / PAYMONEY_ADMIN_PASSWORD from environmentFile.env, then:
GET /api/v1/all-users?page=0&size=10
GET /api/v1/users/1
PUT /api/v1/users/1 `{ "name": "New Name", "email": "alice@test.com" }` (wallet name updates through Kafka)
A normal user calling these gets 403.

## 3. Check the fixes
| What | How | Expected |
|---|---|---|
| Internal endpoint protected | `curl "http://localhost:8081/api/v1/internal/users/by-email?email=alice@test.com"` | 403 Invalid internal key |
| Internal wallet deposit protected | `curl -X POST http://localhost:8082/api/v1/internal/wallets/deposits -H "Content-Type: application/json" -d '{"userId":1,"paymentId":99,"amount":100}'` | 403 |
| Admin endpoint as normal user | GET /api/v1/all-users with Alice's token | 403 (was 500) |
| Bad page size | GET /api/v1/all-users?size=500 as admin | 400 (was 500) |
| Wrong HTTP method | DELETE /api/v1/wallet | 405 |
| Email case | register `ALICE@test.com` again | 409 already registered |
| Per-user idempotency | Alice and Bob both use `Idempotency-Key: 1` | both work |
| Rate limit | login with wrong password 6 times in a minute | 429 |
| Audit logs in DB | see below | rows for TRANSFER / WITHDRAW / DEPOSIT |
| Redis down | `docker compose stop redis`, then do a transfer | transfer still succeeds, only a warning in the wallet-service log |
| Kafka retry / DLT | `docker compose exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list` | topics user-registered, user-updated, audit-log (and *.DLT only if something failed) |

Look at the database (Postgres is published on port 5433):
```bash
docker compose exec postgres psql -U postgres -d paymoney_audit -c "select * from audit_logs order by audit_id desc limit 10;"
docker compose exec postgres psql -U postgres -d paymoney_wallet -c "select * from wallets;"
```
DB shortcut to give test money without Razorpay (dev only):
```bash
docker compose exec postgres psql -U postgres -d paymoney_wallet -c "update wallets set balance = 1000 where user_id = 1;"
```
(then call GET /api/v1/wallet again; the cache lasts 2 minutes, or do any transfer which clears it)

Run unit tests (needs Maven + Java 21 on your PC): `mvnw -pl wallet-service test`
