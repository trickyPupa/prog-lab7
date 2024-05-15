package client;

import common.abstractions.AbstractReceiver;
import common.abstractions.IInputManager;
import common.abstractions.IOutputManager;
import common.exceptions.FileException;
import common.exceptions.WrongArgumentException;
import common.model.entities.Movie;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.regex.Pattern;

public class ClientReceiver extends AbstractReceiver {

    private int recur_param = 0;
    private int cur_recur_param = 0;

    public ClientReceiver(IInputManager inp, IOutputManager out) {
        super(inp, out);
    }

    @Override
    public void add(Object[] args) {
//        System.out.println(Arrays.toString(args));
//        System.out.println(args.length);
        modelObjectInput(args);
//        System.out.println(Arrays.toString(((AbstractCommand) args[0]).getArgs()));
    }

    @Override
    public void exit(Object[] args) {
        outputManager.print("Завершение работы.");
        System.exit(0);
    }

    // проверить
    @Override
    public void executeScript(Object[] args) {
        if (args.length < 2) {
            throw new WrongArgumentException("Недостаточно аргументов для команды execute_script");
        }
        String filename = (String) args[2];
        File file = getFile(filename);

        StringBuilder writer = new StringBuilder();

        try {
            int local_recur_param = checkRecursion(Path.of(filename), new ArrayDeque<>(), 0);
            if (recur_param == 0) recur_param = local_recur_param;

            BufferedReader bufReader = new BufferedReader(new FileReader(file));

            String temp;
            while ((temp = bufReader.readLine()) != null){
                if (temp.strip().startsWith("execute_script")){ // && temp.strip().substring(14).strip().startsWith(args[0])
                    cur_recur_param++;
                    if (cur_recur_param == recur_param) {
                        outputManager.print("В файле обнаружена бесконечная рекурсия, " +
                                "будут выполнены все команды до нее.");
                        recur_param = 0;
                        cur_recur_param = 0;
                        break;
                    }

                }
                writer.append(temp).append("\n");
            }

            CharArrayReader car = new CharArrayReader(writer.toString().toCharArray());

            outputManager.print("Начало исполнения файла {" + file.getPath() + "}.");
            inputManager.setTemporaryInput(new BufferedReader(car));

        } catch (FileNotFoundException e) {
            throw new FileException("Нет файла с указанным именем");
        } catch (IOException e){
            outputManager.print("Ошибка при чтении данных в файле.");
            outputManager.print(e);
        }
    }

    private int checkRecursion(Path path, ArrayDeque<Path> stack, int j) throws IOException {
        int i = 0;

        if (stack.contains(path)) return j;
        stack.addLast(path);
        String str = Files.readString(path);

        Pattern pattern = Pattern.compile("execute_script .*");
        var patternMatcher = pattern.matcher(str);
        while (patternMatcher.find())
        {
            i++;
            Path newPath = Path.of(patternMatcher.group().split(" ")[1]);
//            if(checkRecursion(newPath, stack, i) != 0) return i;
            int a = checkRecursion(newPath, stack, i);
            if (a != 0) return a + j;
        }
        stack.removeLast();
        return 0;
    }

    private static File getFile(String filename) {
        if (filename.isBlank()) {
            throw new WrongArgumentException("execute");
        }

        if (!filename.endsWith(".txt")) {
            throw new FileException("Указан файл недопустимого формата.");
        }

        File file = new File(filename);
        if (!file.exists() || !file.isFile()){
            throw new FileException("Нет файла с указанным именем");
        } else if (!file.canRead()){
            throw new FileException("Файл недоступен для чтения.");
        }
        return file;
    }

    @Override
    public void removeLower(Object[] args) {
        modelObjectInput(args);
    }

    @Override
    public void update(Object[] args) {
        modelObjectInput(args);
    }

    private void modelObjectInput(Object[] args){
        addArg(args, Movie.createMovie(inputManager, outputManager));
    }
}
