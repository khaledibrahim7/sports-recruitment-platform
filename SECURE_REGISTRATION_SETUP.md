# Secure registration deployment

Registration now remains pending until the player or account owner's email is verified. A minor's guardian must separately approve through a one-time email link. Academy and club accounts stay out of public directories and cannot sign in until an administrator approves them.

## Required production configuration

Configure these values in the backend secret manager before accepting registrations:

- `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, and `SMTP_PASSWORD`
- `MAIL_FROM` with a sender address authorized by the mail provider
- `APP_PUBLIC_URL` with the exact public frontend origin and repository path, such as `https://khaledibrahim7.github.io/sports-recruitment-platform`
- `ALLOWED_ORIGINS` with the frontend origin only, such as `https://khaledibrahim7.github.io` (no repository path)
- `JWT_SECRET` with a randomly generated signing secret
- `TRUSTED_PROXY_IPS` with the actual ingress proxy addresses so IP rate limits use the client address safely

The frontend currently calls APIs at `/api/...`. Serve the frontend and backend behind one origin or route `/api` through a reverse proxy; GitHub Pages itself cannot proxy these requests to a separate backend host.

If SMTP is unavailable, registration fails transactionally and does not leave an account that cannot receive its confirmation link. Verification links expire after 24 hours. Resend requests are generic to avoid account enumeration and have a one-minute per-account cooldown.

## Institution review

Create the initial administrator through a controlled database/bootstrap procedure; public registration rejects the `ADMIN` role. An authenticated administrator can inspect `GET /api/v1/admin/organizations/pending` and approve a verified institution with `POST /api/v1/admin/organizations/{userId}/approve`. Approval records the reviewer and timestamp. Public academy and club directories exclude pending accounts.

## Database

Flyway migration `V3__secure_registration_verification.sql` adds account status, one-time verification token, and shared rate-limit tables. Back up production and run the migration through the normal deployment process before enabling the new backend.

Before opening registration to users, publish the organization-approved Terms of Use and Privacy Notice and link them from the registration consent. Those policy texts and their public pages are not present in this repository yet.
