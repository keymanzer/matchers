CREATE TABLE attached_file (
                               attached_file_id NUMBER,
                               name VARCHAR2(30),
                               path VARCHAR(255) NOT NULL,
                               post_id NUMBER NOT NULL,
                               PRIMARY KEY (attached_file_id),
                               FOREIGN KEY (post_id) REFERENCES board(post_id)
);

--첨부파일 테이블 pk 시퀀스
CREATE SEQUENCE attached_file_seq START WITH 1  INCREMENT BY 1 NOCACHE NOCYCLE;