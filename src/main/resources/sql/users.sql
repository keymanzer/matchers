DROP SEQUENCE USER_SEQ;
DROP SEQUENCE EXPERT_LICENSE_SEQ;
DROP SEQUENCE LOCATION_SEQ;
DROP SEQUENCE CATEGORY_SEQ;
DROP TABLE "USERS" CASCADE CONSTRAINTS;
DROP TABLE "USER_AUTH" CASCADE CONSTRAINTS;
DROP TABLE "EXPERT" CASCADE CONSTRAINTS;
DROP TABLE "EXPERT_LICENSE" CASCADE CONSTRAINTS;
DROP TABLE "LOCATION" CASCADE CONSTRAINTS;
DROP TABLE "CATEGORY" CASCADE CONSTRAINTS;
DROP TABLE "EXPERT_CATEGORY" CASCADE CONSTRAINTS;
DROP TABLE "EXPERT_LOCATION" CASCADE CONSTRAINTS;

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

-- 유저 테이블 pk 시퀀스
CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- USER_AUTHORITIES 테이블
CREATE TABLE user_auth (
    user_id NUMBER REFERENCES users(user_id),
    authority VARCHAR2(20) NOT NULL,
    PRIMARY KEY (user_id, authority)
);

-- 전문가 테이블
CREATE TABLE expert (
    user_id NUMBER PRIMARY KEY REFERENCES users(user_id),  
    career CLOB,
    status VARCHAR2(20) DEFAULT 'PENDING',
    reviewed_at DATE
);

-- 전문가 자격증 테이블
CREATE TABLE expert_license (
    license_id NUMBER PRIMARY KEY,
    name VARCHAR2(255) NOT NULL,
    path VARCHAR(255) NOT NULL,
    user_id NUMBER NOT NULL REFERENCES expert(user_id)
);

-- 전문가 자격증 테이블 pk 시퀀스
CREATE SEQUENCE expert_license_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- 지역 테이블
CREATE TABLE location (
    location_id NUMBER PRIMARY KEY,
    city VARCHAR2(255),
    district VARCHAR2(255)
);

-- 지역 테이블 pk 시퀀스
CREATE SEQUENCE location_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;


-- 카테고리 테이블
CREATE TABLE category (
	category_id NUMBER PRIMARY KEY,
    name VARCHAR2(100) UNIQUE NOT NULL
);

-- 카테고리 테이블 pk 시퀀스
CREATE SEQUENCE category_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- 전문가 카테고리 테이블
CREATE TABLE expert_category (
    user_id NUMBER NOT NULL REFERENCES expert(user_id), 
    category_id NUMBER NOT NULL REFERENCES category(category_id),
    PRIMARY KEY (user_id, category_id)
);

-- 전문가 활동 지역 테이블
CREATE TABLE expert_location (
    user_id NUMBER NOT NULL REFERENCES expert(user_id),
    location_id NUMBER NOT NULL REFERENCES location(location_id),
    PRIMARY KEY (user_id, location_id)
);
