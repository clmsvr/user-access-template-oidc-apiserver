-- https://docs.spring.io/spring-boot/how-to/data-initialization.html

insert into Role (id, name, description) 
values 
(1, 'Admin', 'Promove novos usuaros e aloca blocos de trabalho');


insert into authority (id, name, description) values 
(1, 'LER', 'Permite consultar recurso.'),
(2, 'ATUALIZAR', 'Permite atualizar recurso.'),
(3, 'CRIAR', 'Permite criar recurso'),
(4, 'REMOVER', 'Permite remover recurso.');


INSERT INTO role_has_authority(role_id,authority_id) values
(1,1),
(1,2),
(1,3),
(1,4);