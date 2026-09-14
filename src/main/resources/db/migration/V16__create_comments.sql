CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    content VARCHAR(2000) NOT NULL,
    product_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comment_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_comments_product_id ON comments(product_id);
CREATE INDEX idx_comments_author_id ON comments(author_id);
