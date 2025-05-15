CREATE TABLE board (
                       post_id NUMBER,
                       title VARCHAR2(30) NOT NULL,
                       content VARCHAR2(255),
                       created_at DATE DEFAULT SYSDATE,
                       user_nickname VARCHAR2(50) NOT NULL,
                       userid NUMBER NOT NULL,
                       PRIMARY KEY (post_id),
                       FOREIGN KEY (userid) REFERENCES users(user_id)
);
--게시판 테이블 pk 시퀀스
CREATE SEQUENCE board_seq START WITH 1  INCREMENT BY 1 NOCACHE NOCYCLE;


CREATE TABLE quotation_board (
                                 post_id NUMBER,
                                 expert_id NUMBER,
                                 category_id NUMBER NOT NULL,
                                 state VARCHAR(10),
                                 PRIMARY KEY (post_id),
                                 FOREIGN KEY (post_id) REFERENCES board(post_id),
                                 FOREIGN KEY (category_id) REFERENCES category(category_id)
);




CREATE TABLE quotation_location (
                                    board_id NUMBER,
                                    location_id NUMBER,
                                    PRIMARY KEY (board_id, location_id),
                                    FOREIGN KEY (board_id) REFERENCES quotation_board(post_id),
                                    FOREIGN KEY (location_id) REFERENCES location(location_id)
);

