-- https://docs.spring.io/spring-boot/how-to/data-initialization.html

set foreign_key_checks = 0;

drop table IF EXISTS user_has_role;
drop table IF EXISTS role_has_authority;
drop table IF EXISTS user;
drop table IF EXISTS role;
drop table IF EXISTS authority;

set foreign_key_checks = 1;

-- -----------------------------------------------------
-- Table User
-- -----------------------------------------------------
CREATE TABLE user (
  id BIGINT NOT NULL AUTO_INCREMENT,
  oidc_id VARCHAR(200) NOT NULL,
  provider_name VARCHAR(200) NOT NULL,
  email VARCHAR(100) NOT NULL,
  name VARCHAR(100) NOT NULL,
  city VARCHAR(100) NULL,
  state CHAR(2) NULL,
  num_blocks_subtitled INT NULL DEFAULT 0,
  num_blocks_translated INT NULL DEFAULT 0,
  comment TEXT NULL COMMENT 'descricao do proprio usuario.',
  creation_date DATETIME not null,
  update_date DATETIME not null,
  PRIMARY KEY (id),
  UNIQUE INDEX oidc_id_UNIQUE (oidc_id ASC) ,
  INDEX email_INDEX (email ASC) )
ENGINE = InnoDB default character set = utf8mb4;


-- -----------------------------------------------------
-- Table role
-- -----------------------------------------------------
CREATE TABLE role (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(20) NOT NULL,
  description varchar(255),
  PRIMARY KEY (id))
ENGINE = InnoDB default character set = utf8mb4;

-- -----------------------------------------------------
-- Table User_has_role
-- -----------------------------------------------------
CREATE TABLE user_has_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  INDEX fk_user_has_role_role1_idx (role_id ASC) ,
  INDEX fk_user_has_role_user1_idx (user_id ASC) ,
  CONSTRAINT fk_user_has_role_user1
    FOREIGN KEY (user_id)
    REFERENCES user (id)
  ,
  CONSTRAINT fk_user_has_role_role1
    FOREIGN KEY (role_id)
    REFERENCES role (id)
  )
ENGINE = InnoDB default character set = utf8mb4;


-- -----------------------------------------------------
-- Table Authority
-- -----------------------------------------------------
CREATE TABLE authority (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(20) NOT NULL,
  description varchar(255),
  PRIMARY KEY (id))
ENGINE = InnoDB default character set = utf8mb4;


-- -----------------------------------------------------
-- Table role_has_Authority
-- -----------------------------------------------------
CREATE TABLE role_has_authority (
  role_id BIGINT NOT NULL,
  authority_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, authority_id),
  INDEX fk_role_has_authority_authority_idx (authority_id ASC) ,
  INDEX fk_role_has_authority_role_idx (role_id ASC) ,
  CONSTRAINT fk_role_has_authority_role1
    FOREIGN KEY (role_id)
    REFERENCES role (id)
  ,
  CONSTRAINT fk_role_has_authority_authority1
    FOREIGN KEY (authority_id)
    REFERENCES authority (id)
  )
ENGINE = InnoDB default character set = utf8mb4;

