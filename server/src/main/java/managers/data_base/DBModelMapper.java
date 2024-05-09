package managers.data_base;

import common.model.enums.*;
import common.model.entities.*;
import org.postgresql.geometric.PGpoint;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBModelMapper {
    public static Movie getMovieFromDB(ResultSet record, ResultSet person) throws SQLException {
        Movie result = new Movie();

        result.setId(record.getInt("id"));
        result.setName(record.getString("name"));
//        result.setCoordinates(record.getObject("coordinates", Coordinates.class));
        PGpoint point = (PGpoint) record.getObject("coordinates");
        result.setCoordinates(new Coordinates((int)point.x, (long)point.y));

        result.setCreationDate(record.getDate("creationDate").toLocalDate());
        result.setMpaaRating(MpaaRating.valueOf(record.getString("mpaa")));
        result.setOscarsCount(record.getInt("oscarsCount"));
        result.setGoldenPalmCount(record.getInt("goldenPalmCount"));
        result.setLength(record.getInt("length"));
        result.setDirector(getPersonFromDB(person));

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
        statement.setString(2, movie.getCoordinates().toString());
        statement.setDate(3, Date.valueOf(movie.getCreationDate()));
        statement.setInt(4, movie.getOscarsCount());
        statement.setInt(5, movie.getGoldenPalmCount());
        statement.setLong(6, movie.getLength());
        statement.setString(7, movie.getMpaaRating().toString());
        statement.setInt(8, movie.getDirector().getId());
    }

    public static void setPersonData(Person person, PreparedStatement statement) throws SQLException {
        statement.setString(1, person.getName());
        statement.setDate(2, (Date) person.getBirthday());
        statement.setString(3, person.getEyeColor().toString());
        statement.setString(4, person.getHairColor().toString());
        statement.setString(5, person.getNationality().toString());
        statement.setString(6, person.getLocation().toString());
    }
}
