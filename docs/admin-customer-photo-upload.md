Admin customer photo upload

The reported PATCH failure is validation of `profileImage: "blob:http://localhost:3000/..."`. This is a temporary browser preview reference, not an uploaded file. The server cannot fetch its contents. The blob rejection introduced during the audit exposed this gap in the admin photo workflow.

The backend now supports `POST /api/v1/admin/customers/{customerId}/image` with an admin Bearer token and a multipart field named `file`. It stores the image and updates the selected customer's `profile_image` directly. It returns the usual API wrapper with `payload.fileName` and `payload.profileImage`. The latter is the persistent public image URL.

Example client request (integration guidance only; frontend files were not changed):

```javascript
const form = new FormData();
form.append("file", selectedFile); // File from the input, not URL.createObjectURL(...)

const response = await fetch(`${apiBaseUrl}/api/v1/admin/customers/${customerId}/image`, {
  method: "POST",
  headers: { Authorization: `Bearer ${adminToken}` },
  body: form,
});
if (!response.ok) {
  throw new Error(`Photo upload failed (${response.status})`);
}
const result = await response.json();
const savedPhotoUrl = result.payload.profileImage;
```

Let the browser set the multipart Content-Type and boundary. Use `savedPhotoUrl` in the displayed customer state. The image endpoint already persists the URL: no additional PATCH is required for the photo. When saving other fields through PATCH, omit `profileImage` or send the returned persistent URL; do not send an old blob preview or stale previous URL. Keep any local blob URL solely for previewing before upload.

Restart/redeploy the backend to load the new endpoint. In Postman, send POST to `http://localhost:8081/api/v1/admin/customers/26/image` (replace 26 with the intended customer ID), use Authorization → Bearer Token for the admin, and Body → form-data → `file` with type File.

JPG/JPEG, PNG, and WEBP are accepted up to 5 MiB using the same content checks, UUID storage, and configured public base URL as self-profile uploads. The endpoint requires ADMIN at controller and service levels and reuses the existing customer lookup, which excludes ADMIN/SELLER targets. It preserves other customer fields. The existing `/api/v1/profile/image` endpoint still targets the logged-in user.

No database migration, existing data update, or frontend edit was performed during implementation. Tests use mocked repositories and remove only their newly created test upload files. Four new tests cover an admin multipart upload for the selected customer, non-admin denial, missing/privileged targets, and the required multipart field name.
