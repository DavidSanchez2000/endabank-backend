ALTER TABLE users
    ADD is_email_verified BOOLEAN default false ;

ALTER TABLE users
    ADD token_email_verification VARCHAR(255);
