-- удаление

DROP TABLE IF EXISTS persons_prog CASCADE;
DROP TABLE IF EXISTS movies_prog CASCADE;
DROP TABLE IF EXISTS users_prog CASCADE;
DROP TYPE IF EXISTS eye_color_enum CASCADE;
DROP TYPE IF EXISTS hair_color_enum CASCADE;
DROP TYPE IF EXISTS country_enum CASCADE;
DROP TYPE IF EXISTS mpaa_enum CASCADE;
DROP TYPE IF EXISTS location_type CASCADE;


-- создание

CREATE TYPE eye_color_enum AS ENUM (
    'BLUE',
    'YELLOW',
    'ORANGE',
    'WHITE',
    'BROWN'
);

CREATE TYPE hair_color_enum AS ENUM (
    'BLUE',
    'YELLOW',
    'ORANGE',
    'GREEN',
    'RED'
);

CREATE TYPE country_enum AS ENUM (
    'FRANCE',
    'INDIA',
    'VATICAN',
    'THAILAND'
);

CREATE TYPE mpaa_enum AS ENUM (
    'PG',
    'PG_13',
    'NC_17'
);

CREATE TYPE location_type AS (
    x real,
    y int,
    z bigint
);

CREATE TYPE coordinates AS(
    x int,
    y bigint
);

CREATE TABLE IF NOT EXISTS persons_prog (
    id serial PRIMARY KEY,
    name varchar NOT NULL,
    birthDate date NOT NULL,
    eyeColor eye_color_enum,
    hairColor hair_color_enum NOT NULL,
    nationality country_enum NOT NULL,
    location location_type NOT NULL
);

CREATE TABLE IF NOT EXISTS users_prog (
    id serial PRIMARY KEY,
    login varchar UNIQUE NOT NULL,
    password_hash varchar(64) NOT NULL,
    salt varchar(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS movies_prog (
    id serial PRIMARY KEY,
    name varchar,
    coords coordinates NOT NULL CHECK((coords).x > -879 AND (coords).y <= 155),
    creationDate date DEFAULT CURRENT_DATE,
    oscarsCount int NOT NULL CHECK (oscarsCount > 0),
    goldenPalmCount int CHECK (goldenPalmCount > 0),
    length int NOT NULL CHECK (length > 0),
    mpaa mpaa_enum NOT NULL,
    director_id INT NOT NULL REFERENCES persons_prog (id),
    creator_id INT NOT NULL REFERENCES users_prog (id)
);

CREATE OR REPLACE FUNCTION delete_directors_without_films()
    RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM movies_prog WHERE director_id = OLD.director_id) THEN
        DELETE FROM persons_prog WHERE id = OLD.director_id;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_reference_trigger
    AFTER DELETE ON movies_prog
    FOR EACH ROW
EXECUTE FUNCTION delete_directors_without_films();
