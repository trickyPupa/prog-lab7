package managers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import common.model.entities.Movie;
import common.exceptions.FileException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

/**
 * Класс, управляющий файлом, который хранит данные о коллекции в формате .json
 */
public class FileManager {

    public static class CustomLocalDateSerializer extends StdSerializer<LocalDate> {
        private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        public CustomLocalDateSerializer() {
            this(null);
        }
        public CustomLocalDateSerializer(Class<LocalDate> t) {
            super(t);
        }

        @Override
        public void serialize(
                LocalDate value,
                JsonGenerator gen,
                SerializerProvider arg2)
                throws IOException, JsonProcessingException {
            gen.writeString(formatter.format(value));
        }
    }

    public static class CustomLocalDateDeserializer extends StdDeserializer<LocalDate> {

        public CustomLocalDateDeserializer() {
            this(null);
        }

        public CustomLocalDateDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public LocalDate deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
            String value = jsonParser.getText();
            if (!"".equals(value)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                return LocalDate.parse(value, formatter);
            }
            return null;
        }
    }

    private final ObjectMapper mapper;
    private final File file;

    public FileManager(String filename){
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
//        mapper.setDateFormat(new SimpleDateFormat("dd.MM.yyyy"));
        mapper.setDateFormat(DateFormat.getDateInstance(DateFormat.SHORT));

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDate.class, new CustomLocalDateSerializer());
        simpleModule.addDeserializer(LocalDate.class, new CustomLocalDateDeserializer());

        mapper.registerModule(simpleModule);

        if (!filename.endsWith(".json")) {
            throw new FileException("Указан файл недопустимого формата.");
        }
        file = new File(filename);

        if (!file.exists() || !file.isFile()){
            throw new FileException("Нет файла с указанным именем");
        } else if (!file.canRead() || !file.canWrite()){
            throw new FileException("Файл недоступен для чтения и/или записи.");
        }
    }

    public Movie elemFromFile() throws IOException {
        return mapper.readValue(file, Movie.class);
    }

    /**
     * Читает коллекцию из файла.
     * @return коллекция элементов, записанная в файле данных программы.
     */
    public Vector<Movie> collectionFromFile() {
        try {
            TypeReference<Vector<Movie>> type = new TypeReference<>() {
            };
            Vector<Movie> given_vec = mapper.readValue(file, type);
            Vector<Movie> ok_vec = new Vector<>(given_vec);
            for (Movie i : given_vec) {
                if (!i.checkItself()) {
                    ok_vec.remove(i);
                }
            }

            if (ok_vec.isEmpty()){
                throw new FileException("Файл не содержит корректных данных. Коллекция не была изменена.");
            }
//            else if(ok_vec.size() != given_vec.size()) {
//                throw new PartlyCorrectDataFileException("Некоторые данные не были загружены.");
//            }
            return ok_vec;
        } catch (StreamReadException | DatabindException e) {
            throw new RuntimeException("Файл содержит некорректные данные.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Записывает данные в файл данных
     * @param o объект, который требуется записать
     * @throws JsonProcessingException при ошибке распознавания данных из .json файла
     */
    public void writeToFile(Object o) throws JsonProcessingException {
        try(FileWriter fw = new FileWriter(file)) {
            fw.write(mapper.writeValueAsString(o));
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}
