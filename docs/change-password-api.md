# Change password

`POST /api/v1/auth/change-password`

Changes the password of the authenticated user. Send the JWT returned by login
in the `Authorization: Bearer <token>` header and use
`Content-Type: application/json`.

```json
{
  "currentPassword": "<current-password>",
  "newPassword": "<new-password>",
  "confirmPassword": "<new-password>"
}
```

All three fields are required and must not be blank. The new password must be
at least eight characters and match `confirmPassword` exactly. Current and new
passwords must not exceed BCrypt's 72-byte UTF-8 limit. Password whitespace is
preserved. The account is selected from the JWT; no email or user ID is needed.

A successful change returns HTTP `200`:

```json
{
  "success": true,
  "message": "Password changed successfully",
  "status": 200,
  "payload": null,
  "timestamp": "2026-09-14T15:00:00Z"
}
```

The timestamp is generated when the request completes.

| Status | Meaning |
| --- | --- |
| `400` | Invalid fields, incorrect current password, mismatched confirmation, or a password exceeding 72 UTF-8 bytes. |
| `403` | Missing or invalid authentication, or a blocked account, under the existing security configuration. |
| `404` | The authenticated account cannot be found during the password update. |

Only the encoded password is stored, and rejected requests leave the password
unchanged. Existing JWTs remain valid until their normal expiry.
