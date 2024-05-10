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

CREATE TABLE IF NOT EXISTS persons_prog (
    id serial PRIMARY KEY,
    name text NOT NULL,
    birthDate date NOT NULL,
    eyeColor eye_color_enum,
    hairColor hair_color_enum NOT NULL,
    nationality country_enum NOT NULL,
    location location_type NOT NULL
);

CREATE TABLE IF NOT EXISTS users_prog (
    id serial PRIMARY KEY,
    login text UNIQUE NOT NULL,
    password text NOT NULL
);

CREATE TABLE IF NOT EXISTS movies_prog (
    id serial PRIMARY KEY,
    name text,
    coordinates_x int NOT NULL CHECK ( coordinates_x > -879 ),
    coordinates_y int NOT NULL CHECK ( coordinates_y <= 155 ),
    creationDate date DEFAULT CURRENT_DATE,
    oscarsCount int NOT NULL CHECK (oscarsCount > 0),
    goldenPalmCount int CHECK (goldenPalmCount > 0),
    length int NOT NULL CHECK (length > 0),
    mpaa mpaa_enum NOT NULL,
    director_id INT NOT NULL REFERENCES persons_prog (id),
    creator_id INT NOT NULL REFERENCES users_prog (id)
);

