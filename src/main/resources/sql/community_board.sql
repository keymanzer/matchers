CREATE TABLE community_board (
post_id NUMBER NOT NULL,
last_updated_at DATE,
views NUMBER,
PRIMARY KEY (post_id),
FOREIGN KEY (post_id) REFERENCES board(post_id)
);

---------- 05.12 김현조 >> 커뮤니티게시판 데이터 변경 ----------
INSERT INTO community_board VALUES (4, TO_DATE('2025-05-08', 'YYYY-MM-DD'), 101);								
INSERT INTO community_board VALUES (5, TO_DATE('2025-05-07', 'YYYY-MM-DD'), 102);								
INSERT INTO community_board VALUES (6, TO_DATE('2025-05-06', 'YYYY-MM-DD'), 103);	