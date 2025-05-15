-- 지역 테이블
CREATE TABLE location (
                          location_id NUMBER PRIMARY KEY,
                          city VARCHAR2(255),
                          district VARCHAR2(255)
);

-- 지역 테이블 pk 시퀀스
CREATE SEQUENCE location_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;