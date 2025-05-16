CREATE TABLE comments (
    comment_id NUMBER NOT NULL,
    content VARCHAR2(255),
    created_at DATE,
    user_nickname VARCHAR2(50),
    last_updated_at DATE,
    PRIMARY KEY (comment_id)
);

INSERT INTO comments VALUES (comments_seq.NEXTVAL, '커뮤니티테이블1댓글1', TO_DATE('2025-05-08', 'YYYY-MM-DD'), 'nickname1', TO_DATE('2025-05-08', 'YYYY-MM-DD'));								
INSERT INTO comments VALUES (comments_seq.NEXTVAL, '커뮤니티테이블2댓글1', TO_DATE('2025-05-07', 'YYYY-MM-DD'), 'nickname2', TO_DATE('2025-05-07', 'YYYY-MM-DD'));
INSERT INTO comments VALUES (comments_seq.NEXTVAL, '커뮤니티테이블3댓글1', TO_DATE('2025-05-07', 'YYYY-MM-DD'), 'nickname1', TO_DATE('2025-05-07', 'YYYY-MM-DD'));
INSERT INTO comments VALUES (comments_seq.NEXTVAL, 'qa테이블1댓글1', TO_DATE('2025-05-08', 'YYYY-MM-DD'), 'nickname1', TO_DATE('2025-05-08', 'YYYY-MM-DD'));								
INSERT INTO comments VALUES (comments_seq.NEXTVAL, 'qa테이블2댓글1', TO_DATE('2025-05-07', 'YYYY-MM-DD'), 'nickname2', TO_DATE('2025-05-07', 'YYYY-MM-DD'));
INSERT INTO comments VALUES (comments_seq.NEXTVAL, 'qa테이블3댓글1', TO_DATE('2025-05-07', 'YYYY-MM-DD'), 'nickname1', TO_DATE('2025-05-07', 'YYYY-MM-DD'));