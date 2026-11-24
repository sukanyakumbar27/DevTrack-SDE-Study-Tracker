-- DevTrack MySQL schema (reference).
-- Hibernate creates these tables automatically (spring.jpa.hibernate.ddl-auto=update),
-- so running this file is OPTIONAL. Use it to understand the design, or run it manually
-- and then set spring.jpa.hibernate.ddl-auto=validate.

CREATE DATABASE IF NOT EXISTS devtrack CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE devtrack;

CREATE TABLE users (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  email         VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at    DATETIME(6)  NOT NULL
);

CREATE TABLE study_tasks (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id           BIGINT       NOT NULL,
  title             VARCHAR(200) NOT NULL,
  description       TEXT,
  subject           VARCHAR(80),
  priority          VARCHAR(20)  NOT NULL,
  status            VARCHAR(20)  NOT NULL,
  estimated_minutes INT          NOT NULL DEFAULT 0,
  actual_minutes    INT          NOT NULL DEFAULT 0,
  scheduled_date    DATE         NOT NULL,
  completed_at      DATETIME(6),
  created_at        DATETIME(6)  NOT NULL,
  CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_tasks_user_date (user_id, scheduled_date)
);

CREATE TABLE study_sessions (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id      BIGINT      NOT NULL,
  task_id      BIGINT      NULL,
  subject      VARCHAR(80) NOT NULL,
  minutes      INT         NOT NULL,
  session_date DATE        NOT NULL,
  created_at   DATETIME(6) NOT NULL,
  CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_sessions_task FOREIGN KEY (task_id) REFERENCES study_tasks(id),
  INDEX idx_sessions_user_date (user_id, session_date)
);

CREATE TABLE dsa_problems (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT       NOT NULL,
  name        VARCHAR(200) NOT NULL,
  platform    VARCHAR(80),
  topic       VARCHAR(30)  NOT NULL,
  difficulty  VARCHAR(10)  NOT NULL,
  status      VARCHAR(15)  NOT NULL,
  solved_date DATE,
  notes       TEXT,
  created_at  DATETIME(6)  NOT NULL,
  CONSTRAINT fk_dsa_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE java_topics (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id      BIGINT       NOT NULL,
  name         VARCHAR(100) NOT NULL,
  completed    BIT          NOT NULL DEFAULT 0,
  notes        TEXT,
  completed_at DATETIME(6),
  sort_order   INT          NOT NULL DEFAULT 0,
  created_at   DATETIME(6)  NOT NULL,
  CONSTRAINT fk_java_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE projects (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT       NOT NULL,
  name        VARCHAR(150) NOT NULL,
  description TEXT,
  status      VARCHAR(20)  NOT NULL,
  github_url  VARCHAR(300),
  created_at  DATETIME(6)  NOT NULL,
  CONSTRAINT fk_projects_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE project_tasks (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT       NOT NULL,
  project_id BIGINT       NOT NULL,
  title      VARCHAR(200) NOT NULL,
  milestone  BIT          NOT NULL DEFAULT 0,
  done       BIT          NOT NULL DEFAULT 0,
  due_date   DATE,
  created_at DATETIME(6)  NOT NULL,
  CONSTRAINT fk_ptasks_user    FOREIGN KEY (user_id)    REFERENCES users(id),
  CONSTRAINT fk_ptasks_project FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE TABLE notes (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT       NOT NULL,
  title      VARCHAR(200) NOT NULL,
  content    TEXT,
  category   VARCHAR(20)  NOT NULL,
  updated_at DATETIME(6)  NOT NULL,
  created_at DATETIME(6)  NOT NULL,
  CONSTRAINT fk_notes_user FOREIGN KEY (user_id) REFERENCES users(id)
);
