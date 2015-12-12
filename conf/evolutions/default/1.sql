# Messages schema
 
# --- !Ups
 
CREATE TABLE Messages (
    id SERIAL PRIMARY KEY,
    message varchar(255) NOT NULL
);

INSERT INTO Messages (message) Values('Hi!');
INSERT INTO Messages (message) Values('What''s up?');
INSERT INTO Messages (message) Values('Am I alive now?');
 
# --- !Downs
 
DROP TABLE Messages;
