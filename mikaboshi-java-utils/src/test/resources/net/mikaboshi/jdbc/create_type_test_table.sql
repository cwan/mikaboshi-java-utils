create table type_test (
    id INTEGER primary key,
 /*   c_bit BIT, */
 /*   c_tinyint TINYINT, */
    c_smallint SMALLINT,
    c_integer INTEGER,
 /*   c_bigint BIGINT, */
    c_float FLOAT,
    c_real REAL,
 /*   c_double DOUBLE(5, 2), */
    c_numeric NUMERIC(5, 2),
    c_decimal DECIMAL(5, 2),
    c_char CHAR(10),
    c_varchar VARCHAR(10),
 /*   c_longvarchar LONGVARCHAR(10), */
    c_date DATE,
 /*   c_time TIME, */
    c_timestamp TIMESTAMP WITH TIME ZONE,
    c_blob BLOB
);
