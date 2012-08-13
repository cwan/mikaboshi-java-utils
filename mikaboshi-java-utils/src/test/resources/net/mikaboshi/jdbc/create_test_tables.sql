create table sample_tab1 (
	id integer primary key,
	name varchar
);

create table sample_tab2 (
	id1 integer,
	id2 integer,
	name varchar,
	primary key (id1, id2)
);

create table EMP (
	EMPNO integer primary key,
	ENAME varchar,
	JOB varchar,
	MGR integer,
	HIREDATE varchar,
	SAL numeric,
	COMM numeric,
	DEPTNO integer
);	
