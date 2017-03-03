create table catalog(
  catalog_id number(38,0) not null
 ,catalog_name varchar(150) not null
 ,season varchar(150) not null
 ,magazine varchar(150) not null
 ,article varchar(150) not null
 ,primary key(catalog_id)
);
create unique index idx_c_name on catalog(catalog_name);
grant select on catalog to public;

create sequence catalog_seq;
--grant select on catalog_seq to public;

--insert into catalog(catalog_id,catalog_name) values(1, "Kevin");

