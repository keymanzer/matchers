DROP SEQUENCE USER_SEQ;
DROP TABLE "USERS" CASCADE CONSTRAINTS;
DROP TABLE "USER_AUTH" CASCADE CONSTRAINTS;

-- USERS 테이블
CREATE TABLE users (
    user_id NUMBER PRIMARY KEY,
    email VARCHAR2(255) UNIQUE NOT NULL,
    password VARCHAR2(255),
    nickname VARCHAR2(255) NOT NULL,
    profile_img VARCHAR2(255),
    provider VARCHAR2(30),
    provider_id VARCHAR2(100),
    enabled CHAR(1) DEFAULT 'Y',
    created_at DATE DEFAULT SYSDATE,
    updated_at DATE,
    is_deleted CHAR(1) DEFAULT 'N'
);

CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- USER_AUTHORITIES 테이블
CREATE TABLE user_auth (
    user_id NUMBER REFERENCES users(user_id),
    authority VARCHAR2(20) NOT NULL,
    PRIMARY KEY (user_id, authority)
);
