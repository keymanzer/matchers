---------- 05.13 김현조 >> 커뮤니티댓글 컬럼명 수정 (community_board(post_id)에 ON DELETE CASCADE 추가)
---------- 05.12 김현조 >> 커뮤니티댓글 컬럼명 수정 (Field -> parent_comment_id)
CREATE TABLE community_board_comment (
    comment_id NUMBER NOT NULL,
    post_id NUMBER NOT NULL,
    parent_comment_id NUMBER,
    PRIMARY KEY (comment_id, post_id),
    FOREIGN KEY (comment_id) REFERENCES comments(comment_id),
    FOREIGN KEY (post_id) REFERENCES community_board(post_id) ON DELETE CASCADE
);

---------- 05.13 김현조 >> 커뮤니티댓글 데이터 변경 ----------
INSERT INTO community_board_comment VALUES (1, 4, NULL);								
INSERT INTO community_board_comment VALUES (2, 5, NULL);								
INSERT INTO community_board_comment VALUES (3, 6, NULL);	