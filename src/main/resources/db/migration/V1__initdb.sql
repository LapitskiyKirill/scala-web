create table "user"
(
    id serial,
    first_name varchar not null,
    last_name varchar not null,
    dob varchar,
    email varchar not null,
    password varchar not null
);

alter table "user" owner to postgres;

create unique index user_email_uindex
    on "user" (email);

create unique index user_id_uindex
    on "user" (id);

create table "group"
(
    id serial
        constraint group_pk
            primary key,
    display_name varchar not null
);

alter table "group" owner to postgres;

create unique index group_display_name_uindex
    on "group" (display_name);

create unique index group_id_uindex
    on "group" (id);

create table user_group
(
    id serial
        constraint user_group_pk
            primary key,
    user_id integer not null
        constraint user_group_user_id_fk
            references "user" (id)
            on update cascade on delete cascade,
    group_id integer
        constraint user_group_group_id_fk
            references "group"
            on update cascade on delete cascade
);

alter table user_group owner to postgres;

create unique index user_group_id_uindex
    on user_group (id);

