drop table if exists DML_EXECUTOR_SAMPLE;

create table DML_EXECUTOR_SAMPLE (
	id int primary key,
	name varchar
);

insert into DML_EXECUTOR_SAMPLE values (1, 'aaa');

insert into DML_EXECUTOR_SAMPLE values (1, 'aaa');

insert into DML_EXECUTOR_SAMPLE values (
	2,
	'bbb'
);

update DML_EXECUTOR_SAMPLE set name = 'cccc' where id < 2;

delete from DML_EXECUTOR_SAMPLE;

drop table DML_EXECUTOR_SAMPLE;