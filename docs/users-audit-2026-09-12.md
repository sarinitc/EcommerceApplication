Users audit — 12 September 2026

Audited the existing local `ecommerce_postgres` / `ecommerce_db` database on port 15435. These findings do not establish the state of any separate deployed/Neon database. Database queries used read-only transactions. The opt-in application integration test also enforced read-only database connections.

No database data or schema was modified. No migration was added, edited, renamed, or executed. No customer, order, address, role, or upload belonging to an existing user was deleted. Upload tests created UUID files and removed only those test files. Existing uncommitted authentication changes were preserved.

**Database audit**

| Check | Result |
| --- | ---: |
| Total users | 15 |
| Supported BCrypt prefix | 6 |
| Structurally valid BCrypt hashes | 6 |
| Non-BCrypt passwords | 9 |
| Verified / unverified | 4 / 11 |
| ACTIVE / INVITED / BLOCKED | 15 / 0 / 0 |
| With / without profile image | 5 / 10 |
| `http://localhost:8081/uploads/profiles/...` | 3 |
| Browser `blob:http://localhost:3000/...` | 2 |
| Relative profile upload paths | 0 |
| Other absolute HTTP(S) image URLs | 0 |
| NULL phone number | 13 |
| NULL first name | 12 |
| NULL last name | 12 |
| Populated creation timestamp | 15 |
| NULL tax/marketing/verification/status fields | 0 |
| Conflicting `lower(trim(email))` groups | 0 |

The following accounts have non-BCrypt passwords and must be treated as insecure legacy accounts. Their IDs/emails alone do not establish that they are disposable development accounts. No password values or hashes were exported.

| User ID | Email |
| --- | --- |
| 2 | dara@gmail.com |
| 3 | sokha@gmail.com |
| 4 | vanna@gmail.com |
| 5 | piseth@gmail.com |
| 6 | sreypov@gmail.com |
| 7 | ratha@gmail.com |
| 8 | sophy@gmail.com |
| 9 | nita@gmail.com |
| 10 | makara@gmail.com |

Confirm ownership and development/test status before any cleanup. Prefer the existing email verification and password-reset workflow: unverified owners verify their email first, then request and complete a purpose-specific reset OTP; verified owners can use forgot-password directly. The reset service encodes the replacement password with the application's BCrypt encoder. A reset must preserve BLOCKED status. If accounts are confirmed disposable, recreation through registration/admin creation is an explicit later decision after checking associated data. No automatic Java rehash, SQL hashing, account deletion, bulk verification, or password-reset email sending was performed.

**Password and customer behavior**

Registration, admin customer creation, and password reset already call `passwordEncoder.encode(...)`. The inspected auth/user/profile/customer/config paths contain no separate change-password, invitation-acceptance, or development-seed password writer. No stored hash is fed back through the encoder by these flows. Request passwords remain raw inputs to the encoder; a prefix-based bypass for client-provided passwords was deliberately not added. `SecurityConfig` already supplies `BCryptPasswordEncoder`.

Regression tests use the real BCrypt encoder to verify registration, admin creation, reset, and compatibility with `$2a$`, `$2b$`, and `$2y$`. Password and OTP fields are now excluded from Lombok-generated request `toString()` output. Profile and customer response DTOs contain no password, hash, OTP, token, or Redis-key fields. The authentication response's intended login token contract is unchanged.

Auth email normalization now uses `Locale.ROOT` consistently, including forgot/reset, matching admin creation. Existing case-insensitive lookups and duplicate checks remain. Historical emails were not rewritten. Database uniqueness remains case-sensitive, so concurrent conflicting case variants or direct database writes are not fully protected by the application's preflight check; a case-insensitive unique index would be a separate, reviewed database change.

Normal registration retains ACTIVE, unverified, CUSTOMER role, and false tax/marketing defaults. Admin creation retains its required temporary password, automatic CUSTOMER role, optional phone/image, and intentional request verification/status rules. There is no required admin-only profile metadata for normal registration. Username remains the existing login/display identifier; first/last names remain separate optional profile fields.

Admin PATCH remains an explicit non-null field whitelist. Omitted or explicit-null fields retain their values, including false/true booleans when omitted. It does not modify email, ID, password, roles, orders, or addresses. Length validation now matches the current database, and rejects new browser-only blob URLs. Optional fields remain optional. The inspected admin controller has list, create, PATCH, delete, and address routes, but no standalone `GET /api/v1/admin/customers/{customerId}` detail route. No API was invented; detail DTO mapping from create/PATCH was verified.

**Profile images**

New uploads retain the absolute URL response/storage contract, defaulting to `http://localhost:8081/uploads/profiles/<uuid>.<extension>`. Set `UPLOAD_PUBLIC_BASE_URL` to the externally reachable backend origin in other environments. Trailing slashes are removed before appending the public path. Files remain under `uploads/profiles/`, matching the existing public resource mapping.

The service accepts JPEG/JPG, PNG, and WEBP; rejects null, empty, oversized (>5 MiB), unsupported, and mismatched-signature files; derives the extension from the allowed MIME type; ignores the original filename; uses UUID filenames; closes input streams; and avoids overwriting a pre-existing filename. Signature checks are a basic content check, not full image decoding or re-encoding. Product image code was not changed.

Users 20 and 26 have browser `blob:` URLs. These point to temporary browser objects, not recoverable backend files. Leave existing values intact and have the owner upload the original image through the profile upload endpoint. New admin create/PATCH requests reject blob URLs while accepting existing relative paths and HTTP(S) external URLs. No historical URL was normalized. The frontend was not inspected or changed, so switching stored/returned absolute URLs to relative paths was deferred until client host resolution can be verified.

**Schema and Flyway**

Live metadata confirms identity user/role IDs, unique NOT NULL email and username, NOT NULL password, and the existing `user_role` primary key and foreign keys. User/Role JPA length declarations were aligned to live metadata: username 20, email 50, password 120, profile image 500, phone 30, names 100, account status 20, customer type/gender 30, language 50, currency 10, and role name 30. Date of birth is DATE/LocalDate; creation timestamp is TIMESTAMP WITHOUT TIME ZONE/LocalDateTime.

The database already has NOT NULL DEFAULT FALSE for tax exemption, marketing consent, and verification; NOT NULL DEFAULT ACTIVE for account status; and NOT NULL DEFAULT CURRENT_TIMESTAMP for creation. The entity also keeps `@CreationTimestamp` and makes creation time non-updatable. No constraint/default migration is needed. Existing creation dates were not overwritten. Dates assigned when V12 introduced the column cannot reliably establish the true historical registration time; no earlier dates were invented.

Optional first/last name, image, phone, customer type, gender, language, currency, date of birth, and internal notes remain nullable. No fake data was inserted. There was no profile-table split or redesign.

Flyway history has baseline 7 and successful versions 8–14. The next available version is 15, but it was not consumed. Checksum validation succeeded and there are no pending migrations. Flyway reports two existing invalid filenames: the empty V2 and V5 files have leading spaces. These baseline-era files were left unchanged. V1 and several other baseline-era migrations are also empty, so the current migration directory is not a full fresh-database bootstrap. V13's `ADD COLUMN IF NOT EXISTS ... VARCHAR(255)` differs from the live pre-existing image column length 500; the successful migration was not edited and the live column was not narrowed. Other databases should be audited before assuming the same pre-baseline schema.

**Verification**

22 focused tests passed, with zero failures/errors/skips, using JDK 25 and the existing Maven 3.9.16 installation. The wrapper failed locally before Maven started; running the already-installed Maven directly worked without changing the build configuration.

- Spring Boot 4.1.0 and embedded Tomcat started on a random test port against the existing PostgreSQL 17 database.
- Hibernate `ddl-auto=validate` passed.
- Flyway validated 13 recognized migrations; no pending migration exists. The integration test uses a test-only validate strategy rather than executing migrations.
- Existing users, including NULL optional fields/images, loaded through JPA/profile service; customer list queries loaded against the real database.
- Registration/admin creation/reset use real BCrypt in service tests; all three supported prefixes match correctly.
- PATCH preserves omitted fields, blocked/verified state, password, roles, creation timestamp, and address collection. Existing blocked-token regression also passed. There are no BLOCKED rows in the audited database, so blocked-state preservation was tested with fixtures.
- Upload tests cover supported types, UUID storage, hostile original filename, configured absolute URL, empty/oversized/spoofed/unsupported rejection.
- Validation tests cover optional NULLs, length limits, browser blob rejection, and secret-free request `toString()` output.

Limitations: no existing user's known password was provided, so existing-account end-to-end login was not performed. Real email/OTP delivery and live registration/reset/admin writes were not exercised; those paths were tested with mocked persistence/email/OTP collaborators to avoid customer changes or messages. Profile/list verification used live services, not HTTP requests. No standalone customer detail GET exists in the inspected controller. No new migration was applied because none is needed. Orders and addresses were never written.

To repeat focused tests, use Maven with `-Dtest=UserDataSafetyTest,UploadServiceTest,CustomerValidationTest,AuthServicePasswordResetTest,PasswordResetOtpServiceTest,BlockedAccountTest test`. The opt-in `UsersDatabaseAuditIT` additionally requires `RUN_USERS_DB_AUDIT=true` and appropriate `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`. Credentials should be supplied through the environment, never committed. Its datasource connections are read-only and its Flyway strategy only validates.

**Files changed in this task**

Paths below are relative to the repository root.

- `src/main/java/org/example/ecommerceapplication/user/entity/User.java`
- `src/main/java/org/example/ecommerceapplication/user/entity/Role.java`
- `src/main/java/org/example/ecommerceapplication/auth/service/AuthService.java` — only locale-independent normalization; preserved pre-existing changes.
- `src/main/java/org/example/ecommerceapplication/auth/dto/request/LoginRequest.java`
- `src/main/java/org/example/ecommerceapplication/auth/dto/request/RegisterRequest.java`
- `src/main/java/org/example/ecommerceapplication/auth/dto/request/ResetPasswordRequest.java`
- `src/main/java/org/example/ecommerceapplication/auth/dto/request/VerifyOtpRequest.java`
- `src/main/java/org/example/ecommerceapplication/customer/dto/request/AdminCustomerUpdateRequest.java`
- `src/main/java/org/example/ecommerceapplication/customer/dto/response/request/AdminCreateCustomerRequest.java`
- `src/main/java/org/example/ecommerceapplication/profile/service/UploadService.java`
- `src/main/resources/application.properties`
- Added `src/test/java/org/example/ecommerceapplication/customer/service/UserDataSafetyTest.java`
- Added `src/test/java/org/example/ecommerceapplication/customer/service/CustomerValidationTest.java`
- Added `src/test/java/org/example/ecommerceapplication/customer/service/UsersDatabaseAuditIT.java`
- Added `src/test/java/org/example/ecommerceapplication/profile/service/UploadServiceTest.java`
- Added this report.

Pre-existing changes to `CurrentUserResponse`, `CustomUserDetailsService`, `JwtAuthenticationFilter`, and the existing security test/upload were not authored or reverted in this task.
