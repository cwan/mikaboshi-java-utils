drop table if exists hoge;

create table hoge (
	id integer primary key,
	name varchar
);

insert into hoge values (1, 'aaa');
insert into hoge values (2, 'bbb');


drop table if exists piyo;

create table piyo (
	id integer primary key,
	name varchar
);

drop table if exists fuga;

create table fuga (
	id integer primary key,
	name varchar not null
);
