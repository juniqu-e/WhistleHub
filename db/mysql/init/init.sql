-- init.sql
-- 기존 데이터베이스 제거 (초기화 용도)
DROP DATABASE IF EXISTS whistlehub;

-- 원하는 문자셋(utf8mb4) 및 Collation 설정으로 데이터베이스 생성
CREATE DATABASE whistlehub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 기존 사용자 제거 후, 새 사용자 생성
DROP USER IF EXISTS 'ssafy'@'%';
CREATE USER 'ssafy'@'%' IDENTIFIED BY 'ssafy';

-- whistlehub 데이터베이스에 대한 모든 권한 부여
GRANT ALL PRIVILEGES ON whistlehub.* TO 'ssafy'@'%';
FLUSH PRIVILEGES;
