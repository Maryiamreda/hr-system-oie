alter table employee add column team_id BIGINT;
alter table employee add constraint fk_team foreign key (team_id ) REFERENCES team(id) ;
