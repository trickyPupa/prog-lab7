package managers.data_base;

import common.model.enums.*;
import common.model.entities.*;
import org.postgresql.util.PGobject;

import java.sql.*;
import java.time.ZoneId;

public class DBModelMapper {
    public static Movie getMovieFromDB(ResultSet record, ResultSet person, String userLogin) throws SQLException {
        Movie result = new Movie();

        result.setId(record.getInt("id"));

        result.setName(record.getString("name"));

//        result.setCoordinates(new Coordinates(record.getInt("coordinates_x"), record.getLong("coordinates_y")));
        var coords = record.getString("coords").substring(1, record.getString("coords").length() - 1).split(",");
        result.setCoordinates(new Coordinates(Integer.parseInt(coords[0]), Long.parseLong(coords[1])));

        result.setCreationDate(record.getDate("creationDate").toLocalDate());
        result.setMpaaRating(MpaaRating.valueOf(record.getString("mpaa")));
        result.setOscarsCount(record.getInt("oscarsCount"));
        result.setGoldenPalmCount(record.getInt("goldenPalmCount"));
        result.setLength(record.getInt("length"));
        result.setDirector(getPersonFromDB(person));

        result.setCreator(userLogin);

        return result;
    }

    public static Person getPersonFromDB(ResultSet person) throws SQLException {
        Person result = new Person();

        result.setName(person.getString("name"));
        result.setBirthday(person.getDate("birthDate"));
        result.setEyeColor(EyeColor.valueOf(person.getString("eyeColor")));
        result.setHairColor(HairColor.valueOf(person.getString("hairColor")));
        result.setNationality(Country.valueOf(person.getString("nationality")));

        var loc = person.getObject("location").toString()
                .replace(")", "")
                .replace("(", "")
                .replace(",", " ")
                .split(" ");
        result.setLocation(new Location(Float.parseFloat(loc[0]), Long.parseLong(loc[1]), Integer.parseInt(loc[2])));

        return result;
    }

    public static void setMovieData(Movie movie, PreparedStatement statement) throws SQLException {
        statement.setString(1, movie.getName());
        statement.setInt(2, movie.getCoordinates().getX());
        statement.setLong(3, movie.getCoordinates().getY());
//        statement.setDate(4, Date.valueOf(movie.getCreationDate()));
        statement.setInt(4, movie.getOscarsCount());
        statement.setObject(5, movie.getGoldenPalmCount(), Types.INTEGER);
        statement.setLong(6, movie.getLength());
        statement.setObject(7, movie.getMpaaRating(), Types.OTHER);

        statement.setInt(8, movie.getDirector().getId());
    }

    public static void setPersonData(Person person, PreparedStatement statement) throws SQLException {
        statement.setString(1, person.getName());
        statement.setDate(2, Date.valueOf(person.getBirthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
        statement.setObject(3, person.getEyeColor(), Types.OTHER);
        statement.setObject(4, person.getHairColor(), Types.OTHER);
        statement.setObject(5, person.getNationality(), Types.OTHER);
//        statement.setObject(6, person.getLocation().toString(), Types.OTHER);

        var location = new PGobject();
        location.setType("location_type");
        location.setValue(person.getLocation().toString());
        statement.setObject(6, location);
    }
}
