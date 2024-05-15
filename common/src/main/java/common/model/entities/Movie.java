package common.model.entities;

import common.model.enums.MpaaRating;
import common.exceptions.InterruptException;
import common.abstractions.IInputManager;
import common.abstractions.IOutputManager;
import common.user.User;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static common.utils.Funcs.*;
import static java.lang.Math.max;

/**
 * Класс, хранящий описание фильма.
 */

public class Movie implements Comparable<Movie>, Checkable {
//    private static int id_counter = 0;
    private static int maxNameLen = 10;
    private Integer id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически

//    @JsonSerialize(using = FileManager.CustomLocalDateSerializer.class)
//    @JsonDeserialize(using = FileManager.CustomLocalDateDeserializer.class)
    private LocalDate creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически

    private String name; //Поле не может быть null, Строка не может быть пустой
    private int oscarsCount; //Значение поля должно быть больше 0
    private Integer goldenPalmCount; //Значение поля должно быть больше 0, Поле может быть null
    private long length; //Значение поля должно быть больше 0

    private Coordinates coordinates; //Поле не может быть null
    private MpaaRating mpaaRating; //Поле не может быть null
    private Person director; //Поле не может быть null

    private String creator;

    public Movie(){
        ;
    }

    /**
     * Обновляет значения фильма, не меняя его Id
     * @param newValue объект, значения которого требуется присвоить.
     */
    public void update(Movie newValue){
        name = newValue.name;
        oscarsCount = newValue.oscarsCount;
        goldenPalmCount = newValue.goldenPalmCount;
        length = newValue.length;
        coordinates = newValue.coordinates;
        mpaaRating = newValue.mpaaRating;
        director = newValue.director;
    }

    public int getId(){
        return id;
    };

    public Integer getGoldenPalmCount(){
        return goldenPalmCount;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public String getName() {
        return name;
    }

    public int getOscarsCount() {
        return oscarsCount;
    }

    public long getLength() {
        return length;
    }

    public MpaaRating getMpaaRating() {
        return mpaaRating;
    }

    public Person getDirector() {
        return director;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public String getCreator() {
        return creator;
    }

    public void setName(String name) {
        this.name = name;
        maxNameLen = max(maxNameLen, name.length());
    }

    public void setOscarsCount(int oscarsCount) {
        this.oscarsCount = oscarsCount;
    }

    public void setGoldenPalmCount(Integer goldenPalmCount) {
        this.goldenPalmCount = goldenPalmCount;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setDirector(Person director) {
        this.director = director;
    }

    public void setMpaaRating(MpaaRating mpaaRating) {
        this.mpaaRating = mpaaRating;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    //    public static void setId_counter(int id){
//        id_counter = id;
//    }

    public static void setMaxNameLen(int maxNameLen) {
        Movie.maxNameLen = maxNameLen;
    }

    /**
     * Создает и возвращает объект {@see Movie} используя переданный поток ввода
     * @param input экземпляр класса, реализующего {@see IInputManager} для считывания аргументов
     * @param output экземпляр класса, реализующего {@see IOutputManager} для общения с пользователем
     * @return {@see Movie} - объект, созданный с помощью данного входного потока
     */

    public static Movie createMovie(IInputManager input, IOutputManager output){
        Movie elem = new Movie();

        Map<String, Predicate<String>> args_checkers = new LinkedHashMap<>();
        args_checkers.put("название", x -> {
            if (!x.isBlank()){
                elem.setName(x);
                return true;
            }
            return false;
        });
        args_checkers.put("количество премий Оскар (целое число <2*10^9 и >-2*10^9)", x -> {
            if (isInt(x) && !x.equals("0")){
                elem.setOscarsCount(Integer.parseInt(x));
                return true;
            }
            return false;
        });
        args_checkers.put("количество золотых пальмовых ветвей (целое число <2*10^9 и >-2*10^9 или пустая строка)", x -> {
            if (isInt(x) && !x.equals("0")){
                elem.setGoldenPalmCount(Integer.parseInt(x));
                return true;
            } else if (x.isBlank()){
                elem.setGoldenPalmCount(null);
                return true;
            }
            return false;
        });
        args_checkers.put("продолжительность фильма (целое число <9*10^18 и >-9*10^18)", x -> {
            if (isLong(x) && !x.equals("0")){
                elem.setLength(Long.parseLong(x));
                return true;
            }
            return false;
        });
        args_checkers.put("MPAA рейтинг фильма (PG, PG_13, NC_17)", x -> {
            if (MpaaRating.contains(x.toUpperCase())){
                elem.setMpaaRating(MpaaRating.valueOf(x.toUpperCase()));
                return true;
            }
            return false;
        });


        // "Имя режиссёра", "Дата рождения режиссёра (ДД.ММ.ГГГГ)", "Цвет глаз режиссёра (BLUE, YELLOW, ORANGE, WHITE, BROWN)"

        try {
            for (String a : args_checkers.keySet()){
                Predicate<String> check = args_checkers.get(a);
                output.print("Введите " + a + ":");
                String line = input.nextLine();
                if (line == null || line.strip().equals("exit")){
                    throw new InterruptException();
                }

                while (!check.test(line)){
                    output.print("Некорректные данные.");

                    output.print("'" + line + "'");

                    output.print("Введите " + a + ":");
                    line = input.nextLine();
                }
            }

            elem.setCoordinates(Coordinates.createCoords(input, output));
            elem.setDirector(Person.createPerson(input, output));

        } catch (IOException e){
            output.print(e.getMessage());
        }

        if (elem.name.length() > maxNameLen)
            maxNameLen = elem.name.length();

        return elem;
    }

//    public void setGeneratedFields(){
//        id_counter++;
//        id = id_counter;
//        creationDate = LocalDate.now();
//    }


    @Override
    public boolean checkItself(){
        return !name.isBlank() && oscarsCount > 0 && (goldenPalmCount == null || goldenPalmCount > 0)
                && length > 0 && coordinates.checkItself() && director.checkItself();
    }

    @Override
    public String toString() {
        return String.format("%3d: %s (%s; rating - %5s; coordinates - %10s) with %3d Oscars and %3d Golden Palms by %10s. [creator = %s]",
                id, " ".repeat(max(maxNameLen - name.length(), 0)) + name, creationDate, mpaaRating.name(), coordinates.toString(), oscarsCount, goldenPalmCount,
                director.toString(), creator);
    }

    @Override
    public int compareTo(Movie o) {
//        return this.creationDate != o.creationDate ? this.creationDate.compareTo(o.creationDate) : this.name.compareTo(o.name);
        return !Objects.equals(this.name.toLowerCase(), o.name.toLowerCase()) ? this.name.toLowerCase().compareTo(o.name.toLowerCase()) : this.director.compareTo(o.director);
    }
}
