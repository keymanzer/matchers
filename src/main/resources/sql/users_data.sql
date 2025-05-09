-- 관리자 계정
INSERT INTO USERS (
    user_id, email, password, nickname, enabled, created_at, is_deleted
) VALUES (
    USER_SEQ.NEXTVAL, 'admin@test.com', '$2a$12$Q2oRkd4wE8MIxqPRj/JAzOj9EQbAekQDaMZsZxBckCfzS1aTqEdcG', '관리자', 'Y', SYSDATE, 'N'
);

-- 일반 사용자 1
INSERT INTO USERS (
    user_id, email, password, nickname, enabled, created_at, is_deleted
) VALUES (
    USER_SEQ.NEXTVAL, 'user1@test.com', '$2a$12$Q2oRkd4wE8MIxqPRj/JAzOj9EQbAekQDaMZsZxBckCfzS1aTqEdcG', '홍길동', 'Y', SYSDATE, 'N'
);

-- 일반 사용자 2
INSERT INTO USERS (
    user_id, email, password, nickname, enabled, created_at, is_deleted
) VALUES (
    USER_SEQ.NEXTVAL, 'user2@test.com', '$2a$12$Q2oRkd4wE8MIxqPRj/JAzOj9EQbAekQDaMZsZxBckCfzS1aTqEdcG', '황진이', 'Y', SYSDATE, 'N'
);

-- 일반 사용자 3
INSERT INTO USERS (
    user_id, email, password, nickname, enabled, created_at, is_deleted
) VALUES (
    USER_SEQ.NEXTVAL, 'user3@test.com', '$2a$12$Q2oRkd4wE8MIxqPRj/JAzOj9EQbAekQDaMZsZxBckCfzS1aTqEdcG', '김유신', 'Y', SYSDATE, 'N'
);

-- 권한 부여: 관리자
INSERT INTO USER_AUTH (user_id, authority)
VALUES ((SELECT user_id FROM USERS WHERE email = 'admin@test.com'), 'ROLE_ADMIN');

-- 권한 부여: 일반 사용자 1
INSERT INTO USER_AUTH (user_id, authority)
VALUES ((SELECT user_id FROM USERS WHERE email = 'user1@test.com'), 'ROLE_USER');

-- 권한 부여: 일반 사용자 2
INSERT INTO USER_AUTH (user_id, authority)
VALUES ((SELECT user_id FROM USERS WHERE email = 'user2@test.com'), 'ROLE_USER');

-- 권한 부여: 일반 사용자 3
INSERT INTO USER_AUTH (user_id, authority)
VALUES ((SELECT user_id FROM USERS WHERE email = 'user3@test.com'), 'ROLE_USER');

COMMIT;