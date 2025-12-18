-- MySQL의 TEXT와 JPA의 @Lob+String(CLOB으로 생성)는 다르다.

ALTER TABLE tax_outbox
MODIFY payload LONGTEXT NOT NULL;