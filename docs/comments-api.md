# Create a comment

`POST /api/v1/comments` requires the normal `Authorization: Bearer <access-token>` header and `Content-Type: application/json`.

```json
{
  "productId": 85,
  "content": "Is this available in blue?"
}
```

Use an existing, non-deleted product ID. `postId` is accepted as an alias for `productId` to support the supplied comment example; it identifies a product, not a separate post. Send only one of these fields. Content must be nonblank and no longer than 2000 characters.

The author is the signed-in user. The server finds the product's seller and creates a `COMMENT_ADDED` notification for that seller, with the saved comment ID as `referenceId` and `COMMENT` as `referenceType`. The request cannot choose a different author or notification recipient.

A successful request returns HTTP **201 Created**:

```json
{
  "success": true,
  "message": "Comment created successfully",
  "status": 201,
  "payload": {
    "id": 123,
    "productId": 85,
    "authorId": 42,
    "content": "Is this available in blue?",
    "createdAt": "2026-09-14T12:00:00"
  },
  "timestamp": "2026-09-14T05:00:00Z"
}
```

Invalid content or product IDs return 400; missing/deleted products return 404. Unauthenticated requests are rejected by the existing security configuration.

The comment and notification are saved in the same transaction. A database failure rolls both back. WebSocket publication waits for commit; a messaging failure after commit is logged, and the stored notification remains available through `GET /api/v1/notifications` to its recipient.

Flyway migration `V16__create_comments.sql` creates the comments table on application startup. Apply it before using this endpoint. Deleting a customer through the existing admin flow also deletes their authored comments; comments do not add a new restriction to that flow. Browser WebSocket authentication and delivery must be configured separately; these endpoint changes do not add a browser connection flow.
