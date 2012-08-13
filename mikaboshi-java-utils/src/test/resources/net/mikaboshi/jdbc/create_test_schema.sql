create table sample_tab1 (
	id integer primary key,
	name varchar
);

insert into sample_tab1 values (1, 'AAA');
insert into sample_tab1 values (2, 'あああ');
insert into sample_tab1 values (3, '①～');

create table sample_tab2 (
	id1 integer,
	id2 integer,
	name varchar,
	primary key (id1, id2)
);

insert into sample_tab2 values (1, 1, '12345');
insert into sample_tab2 values (1, 2, '67890');
insert into sample_tab2 values (2, 1, 'あいうえお');
insert into sample_tab2 values (2, 2, '壱弐参');

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

insert into EMP values (7369, 'SMITH', 'CLERK', 7902, '1980/12/17', 800, null, 20);
insert into EMP values (7499, 'ALLEN', 'SALESMAN', 7698, '1981/2/20', 1600,300, 30);
insert into EMP values (7521, 'WARD', 'SALESMAN', 7698, '1981/2/22', 1250,500, 30);
insert into EMP values (7566, 'JONES', 'MANAGER', 7839, '1981/4/2', 2975,null, 20);
insert into EMP values (7654, 'MARTIN', 'SALESMAN', 7698, '1981/9/28', 1250,1400, 30);
insert into EMP values (7698, 'BLAKE', 'MANAGER', 7839, '1981/5/1', 2850, null, 30);
insert into EMP values (7782, 'CLARK', 'MANAGER', 7839, '1981/6/9', 2450, null, 10);
insert into EMP values (7788, 'SCOTT', 'ANALYST', 7566, '1987/4/19', 3000, null, 20);
insert into EMP values (7839, 'KING', 'PRESIDENT',  null, '1981/11/17', 5000, null, 10);
insert into EMP values (7844, 'TURNER', 'SALESMAN', 7698, '1981/9/8', 1500,0, 30);
insert into EMP values (7876, 'ADAMS', 'CLERK', 7788, '1987/5/23', 1100, null, 20);
insert into EMP values (7900, 'JAMES', 'CLERK', 7698, '1981/12/3', 950, null, 30);
insert into EMP values (7902, 'FORD', 'ANALYST', 7566, '1981/12/3', 3000, null, 20);
insert into EMP values (7934, 'MILLER', 'CLERK', 7782, '1982/1/23', 1300, null, 10);
