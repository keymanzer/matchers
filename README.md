## 1. Oracle 계정 생성

SQL Developer에서 아래 스크립트를 실행하세요.

> ✅ 계정 정보  
> - 사용자명: `FOURMEN`  
> - 비밀번호: `1004`

```sql
-- 계정 생성
CREATE USER FOURMEN IDENTIFIED BY 1004
DEFAULT TABLESPACE USERS
TEMPORARY TABLESPACE TEMP
QUOTA UNLIMITED ON USERS;

-- 기본 권한 부여
GRANT CONNECT, RESOURCE TO FOURMEN;

-- 객체 생성 권한 부여
GRANT CREATE TABLE, CREATE SEQUENCE, CREATE TRIGGER, CREATE VIEW, CREATE PROCEDURE TO FOURMEN;
```

## 2. User 도메인 관련 테이블 생성 구문 실행

SQL Developer에서 src/main/resources/sql/users.sql 스크립트를 실행하세요.

> - 총 7개 테이블 생성
> - 단 처음 쿼리를 실행한다면 DROP 관련 쿼리들만 제외하고 실행하세요 (CREATE 쿼리만 묶어서 실행)

## 3. User 도메인 관련 MOCK 데이터 생성 구문 실행

SQL Developer에서 src/main/resources/sql/users_data.sql 스크립트를 실행하세요.

> 👑 관리자 계정
> - 이메일 : `admin@test.com` 
> - 비밀번호: `1004`
> - 닉네임: `관리자`

> 🙋‍♂️ 일반 회원 계정 1
> - 이메일 : `user1@test.com` 
> - 비밀번호: `1004`
> - 닉네임: `홍길동`

> 🙋‍♂️ 일반 회원 계정 2
> - 이메일 : `user2@test.com` 
> - 비밀번호: `1004`
> - 닉네임: `이몽룡`
