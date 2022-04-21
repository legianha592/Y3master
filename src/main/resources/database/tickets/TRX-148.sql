create table milkrun_vehicle

(

   id                   bigint not null,

   tenant_id            bigint,

   customer_id          bigint,

   vehicle_id           bigint,

   day_of_week          varchar(64),

   active_ind           tinyint(1),

   created_by           varchar(128),

   created_date         timestamp,

   updated_by           varchar(128),

   updated_date         timestamp,

   primary key (id)

);



create table milkrun_trip

(

   id                   bigint not null,

   milkrun_vehicle_id   bigint,

   trip_sequence        int,

   vist_sequence        int,

   location_id          bigint,

   start_time           timestamp,

   end_time             timestamp,

   active_ind           tinyint(1),

   created_by           varchar(128),

   created_date         timestamp,

   updated_by           varchar(128),

   updated_date         timestamp,

   primary key (id)

);

CREATE SEQUENCE SEQ_milkrun_vehicle
START WITH 1
INCREMENT BY 1
NO MINVALUE
NO MAXVALUE
CACHE 1;
CREATE SEQUENCE SEQ_milkrun_trip
START WITH 1
INCREMENT BY 1
NO MINVALUE
NO MAXVALUE
CACHE 1;