INSERT INTO board VALUES (board_seq.NEXTVAL, '견적테이블1', 'Content1', TO_DATE('2025-05-09', 'YYYY-MM-DD'), 'nickname1', 1);
INSERT INTO board VALUES (board_seq.NEXTVAL, '견적테이블2', 'Content2', TO_DATE('2025-05-08', 'YYYY-MM-DD'), 'nickname2', 2);
INSERT INTO board VALUES (board_seq.NEXTVAL, '견적테이블3', 'Content3', TO_DATE('2025-05-07', 'YYYY-MM-DD'), 'nickname3', 3);
INSERT INTO board VALUES (board_seq.NEXTVAL, '커뮤니티테이블1', 'Content4', TO_DATE('2025-05-06', 'YYYY-MM-DD'), 'nickname1', 1);
INSERT INTO board VALUES (board_seq.NEXTVAL, '커뮤니티테이블2', 'Content5', TO_DATE('2025-05-05', 'YYYY-MM-DD'), 'nickname2', 2);
INSERT INTO board VALUES (board_seq.NEXTVAL, '커뮤니티테이블3', 'Content6', TO_DATE('2025-05-04', 'YYYY-MM-DD'), 'nickname3', 3);
INSERT INTO board VALUES (board_seq.NEXTVAL, 'qa테이블1', 'Content7', TO_DATE('2025-05-03', 'YYYY-MM-DD'), 'nickname1', 1);
INSERT INTO board VALUES (board_seq.NEXTVAL, 'qa테이블2', 'Content8', TO_DATE('2025-05-02', 'YYYY-MM-DD'), 'nickname2', 2);
INSERT INTO board VALUES (board_seq.NEXTVAL, 'qa테이블3', 'Content9', TO_DATE('2025-05-01', 'YYYY-MM-DD'), 'nickname3', 3);


INSERT INTO quotation_board VALUES (1, 1, 1, '진행중');
INSERT INTO quotation_board VALUES (2, 2, 2, '진행중');
INSERT INTO quotation_board VALUES (3, 3, 3, '진행전');


INSERT INTO quotation_location VALUES (1, 1);
INSERT INTO quotation_location VALUES (2, 2);
INSERT INTO quotation_location VALUES (3, 3);
