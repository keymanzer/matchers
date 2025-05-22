-- 채팅 관련 시퀀스 생성
CREATE SEQUENCE chat_room_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE chat_participant_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE chat_message_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE message_read_status_seq START WITH 1 INCREMENT BY 1;

-- 채팅방 테이블
CREATE TABLE chat_room (
                           chat_room_id NUMBER NOT NULL,
                           name VARCHAR2(50) NOT NULL,
                           board_id NUMBER NOT NULL,
                           PRIMARY KEY (chat_room_id),
                           FOREIGN KEY (board_id) REFERENCES quotation_board(post_id)
);

-- 채팅 참여자 테이블
CREATE TABLE chat_participant (
                                  participant_id NUMBER NOT NULL,
                                  chat_room_id NUMBER NOT NULL,
                                  user_id NUMBER NOT NULL,
                                  PRIMARY KEY (participant_id),
                                  UNIQUE (chat_room_id, user_id),
                                  FOREIGN KEY (chat_room_id) REFERENCES chat_room(chat_room_id) ON DELETE CASCADE,
                                  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 메시지 테이블
CREATE TABLE chat_message (
                              message_id NUMBER NOT NULL,
                              chat_room_id NUMBER NOT NULL,
                              user_id NUMBER NOT NULL,
                              content VARCHAR2(200) NOT NULL,
                              PRIMARY KEY (message_id),
                              UNIQUE (chat_room_id, user_id, message_id),
                              FOREIGN KEY (chat_room_id) REFERENCES chat_room(chat_room_id) ON DELETE CASCADE,
                              FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 메세지 테이블에 메세지 생성일자 생성
ALTER TABLE chat_message ADD created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

------- 05.15 백승호 >> 메시지 읽음 상태 테이블 수정 -------
CREATE TABLE message_read_status (
                                     message_read_id NUMBER NOT NULL,
                                     message_id NUMBER NOT NULL,
                                     chat_room_id NUMBER NOT NULL,
                                     user_id NUMBER NOT NULL,
                                     is_read VARCHAR2(1) DEFAULT 'N' NOT NULL CHECK (is_read IN ('Y', 'N')),
                                     PRIMARY KEY (message_read_id),
                                     UNIQUE (message_id, user_id),
                                     FOREIGN KEY (message_id) REFERENCES chat_message(message_id),
                                     FOREIGN KEY (chat_room_id) REFERENCES chat_room(chat_room_id),
                                     FOREIGN KEY (user_id) REFERENCES users(user_id)
);
ALTER TABLE message_read_status ADD update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;


------- 05.21 백승호 >> 알림 테이블 및 시퀀스 추가-------
CREATE TABLE notification (
                              notification_id NUMBER PRIMARY KEY,
                              receiver_user_id NUMBER NOT NULL,              -- 알림 받는 사용자
                              is_read VARCHAR2(1) DEFAULT 'N' CHECK (is_read IN ('Y', 'N')),
                              sender_user_id NUMBER NOT NULL,                -- 보낸 사용자 (시스템 알림 없으니 NOT NULL 적절)
                              chat_room_id NUMBER,                           -- 채팅 알림에만 사용 (nullable)
                              content VARCHAR2(255) NOT NULL,
                              notification_type VARCHAR2(50) CHECK (notification_type IN ('CHAT', 'ADMIN')),
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE notification_seq START WITH 1 INCREMENT BY 1 NOCACHE;