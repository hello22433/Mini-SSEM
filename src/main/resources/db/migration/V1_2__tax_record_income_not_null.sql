-- income은 필수이므로 not null 설정

ALTER TABLE tax_record
    MODIFY income DECIMAL(19,2) NOT NULL;

