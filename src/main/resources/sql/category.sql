-- 카테고리 테이블
CREATE TABLE category (
                          category_id NUMBER PRIMARY KEY,
                          name VARCHAR2(100) UNIQUE NOT NULL
);

-- 카테고리 테이블 pk 시퀀스
CREATE SEQUENCE category_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;