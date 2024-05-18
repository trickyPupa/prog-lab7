package managers;

import common.abstractions.AbstractReceiver;
import common.abstractions.IOutputManager;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;
import common.model.entities.Movie;
import common.exceptions.WrongArgumentException;
import common.user.User;
import exceptions.FinishConnecton;
import exceptions.WrongUserException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

import static common.utils.Funcs.isInt;

public class ServerCommandReceiver extends AbstractReceiver {
    private ServerCommandHandler.ShellValuables shell;

    public ServerCommandReceiver(ServerCommandHandler.ShellValuables shell){
        super(null, shell.getServerOutputManager());
        this.shell = shell;
    }

    @Override
    public void add(Object[] args) {
        Movie obj = (Movie) args[2];
        if (shell.getCollectionManager().contains(obj)){
            shell.getServerOutputManager().print("Элемент уже существует в коллекции");
        } else {
//            obj.setGeneratedFields();
            try {
                shell.getCollectionManager().add(obj, (User) args[1]);
                shell.getServerOutputManager().print("Добавлен новый элемент, ему присвоен id=" + obj.getId() + ".");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // теперь не нужен так как данные и так сохраняются в базу автоматически
    @Override
    public void save(Object[] args) {
        /*try {
            FileManager fm = shell.getFileManager();
            fm.writeToFile(shell.getCollectionManager().getCollection());
        } catch (JsonProcessingException e) {
            shell.getServerOutputManager().print("Невозможно записать в файл:\n" + e.getMessage() + ".");
        }*/
    }

    @Override
    public void clear(Object[] args) {
        shell.getCollectionManager().clear((User) args[1]);
        shell.getServerOutputManager().print("Удалены все возможные элементы.");
    }

    @Override
    public void show(Object[] args) {
        shell.getServerOutputManager().print(shell.getCollectionManager().presentView());
    }

    // подумать
    @Override
    public void exit(Object[] args) {
        shell.getServerOutputManager().print("Завершение работы.");
        throw new FinishConnecton();
    }

    // сделать красивый вывод
    @Override
    public void info(Object[] args) {
        IOutputManager output = shell.getServerOutputManager();
        Map<String, String> info = shell.getCollectionManager().getInfo();
        output.print("Информация о коллекции:");

        for(String key : info.keySet()){
            output.print("\t" + key + " - " + info.get(key));
        }
    }

    // хорошо подумать
    @Override
    public void executeScript(Object[] args) {
        super.executeScript(args);
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

    @Override
    public void filterByGoldenPalmCount(Object[] args) {
        if (!isInt((String) args[2])){
            throw new WrongArgumentException("filter_by_golden_palm_count");
        }

        Integer gp_count = Integer.parseInt((String) args[2]);

        var collection = shell.getCollectionManager().getCollection();

        if (collection.isEmpty()){
            shell.getServerOutputManager().print("Коллекция пуста.");
            return;
        }
        var a = collection.stream().filter((x) -> Objects.equals(x.getGoldenPalmCount(), gp_count)).toArray();
        if (a.length == 0){
            shell.getServerOutputManager().print("В коллекции нет элементов, соответствующих заданным фильтрам.");
        } else{
            Arrays.stream(a).forEach((x) -> shell.getServerOutputManager().print(x.toString()));
        }
    }

    @Override
    public void help(Object[] args) {
        IOutputManager output = shell.getServerOutputManager();
        var commandsList = shell.commands;

        output.print("Список доступных команд:");
        for (String name : commandsList.keySet()) {
            String temp = name;
            AbstractCommand curCmd = (AbstractCommand) commandsList.get(name).apply(null);

            if (!curCmd.getRequiringArguments().equals("no")) temp += " " + curCmd.getRequiringArguments();

            temp = String.format("%50s", temp);
            output.print(String.format("%s: \t%s", temp, curCmd.getDescription()));
        }
    }

    // подумать
    @Override
    public void history(Object[] args) {
        /*IOutputManager output = shell.getServerOutputManager();

        output.print("[");
        for(Command i : shell.getHistoryManager().getHistory()){
            output.print("\t" + i.getName());
        }
        output.print("No ideas bout history");
        output.print("]");*/

    }

    @Override
    public void minByCoordinates(Object[] args) {
        var collection = shell.getCollectionManager().getCollection();

        if (collection.isEmpty()){
            shell.getServerOutputManager().print("Коллекция пуста.");
            return;
        }

//        var res = collection.stream().min(Comparator.comparing(Movie::getCoordinates)).get();
        shell.getServerOutputManager().print(collection.stream()
                .min(Comparator.comparing(Movie::getCoordinates)).get().toString());
    }

    @Override
    public void removeAllByGoldenPalmCount(Object[] args) {
        if (args.length < 3) {
            throw new WrongArgumentException("недостаточно аргументов - remove_all_by_golden_palm_count");
        }
        if (!isInt((String) args[2])){
            throw new WrongArgumentException("remove_all_by_golden_palm_count");
        }

        Integer gp_count = Integer.parseInt((String) args[2]);

        var collection = shell.getCollectionManager().getCollection();

        collection.stream()
                .filter((x) -> Objects.equals(x.getGoldenPalmCount(), gp_count))
                .forEach((x) -> {
                    try {
                        shell.getCollectionManager().remove(x, (User) args[1]);
                        shell.getServerOutputManager().print("Удален элемент id=" + x.getId());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (WrongUserException ignored) {
                        ;
                    }
                });

        shell.getServerOutputManager().print("Все доступные элементы с количеством золотых пальмовых ветвей = " + gp_count + " удалены.");
    }

    @Override
    public void removeById(Object[] args) {
        if (args.length < 3) {
            throw new WrongArgumentException("недостаточно аргументов - remove_by_id");
        }
        if (!isInt((String) args[2])){
            throw new WrongArgumentException("remove_by_id");
        }

        int id = Integer.parseInt((String) args[2]);

        var collection = shell.getCollectionManager().getCollection();

        var a = collection.stream()
                .filter((x) -> x.getId() == id)
                .toList();

        if (a.isEmpty()){
            shell.getServerOutputManager().print("В коллекции нет элемента с id=" + id + ".");
        } else {
            a.forEach((x) -> {
                try {
                    shell.getCollectionManager().remove(x, (User) args[1]);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (WrongUserException e) {
                    shell.getServerOutputManager().print("У вас недостаточно прав для удаления элемента id=" + id + ".");
                }
                shell.getServerOutputManager().print("Элемент c id=" + id + "удален.");
            });
        }
    }

    @Override
    public void removeFirst(Object[] args) {
//        if (shell.getCollectionManager().getCollection().isEmpty()){
//            shell.getServerOutputManager().print("Коллекция пуста.");
//            return;
//        }
        try {
            var result = shell.getCollectionManager().removeFirst((User) args[1]);
            if (result != null) {
                shell.getServerOutputManager().print("Элемент удален.");
            } else {
                shell.getServerOutputManager().print("В коллекции нет элементов, которые вы можете удалить.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeLower(Object[] args) {
        User user = (User) args[1];

        var cm = shell.getCollectionManager();
        var collection = cm.getCollectionForUser(user);

        if (collection.isEmpty()){
            shell.getServerOutputManager().print("В коллекции нет элементов, которые вы можете удалить.");
            return;
        }

        Movie elem = (Movie) args[2];

        collection.stream()
                .filter((x) -> x.compareTo(elem) < 0)
                .forEach((x) -> {
                    try {
                        cm.remove(x, user);
                    } catch (SQLException | WrongUserException e) {
                        throw new RuntimeException(e);
                    }
                    shell.getServerOutputManager().print(String.format("Удален элемент {%100s}.", x));
                });
    }

    @Override
    public void update(Object[] args) {
        if (args.length < 4) {
            throw new WrongArgumentException("недостаточно аргументов - update");
        }
        if (!isInt((String) args[2])){
//            shell.getOutputManager().print("Некорректные аргументы.");
            throw new WrongArgumentException("update");
        }
        int id = Integer.parseInt((String) args[2]);

        var collection = shell.getCollectionManager().getCollection();

        var a = collection.stream().filter((x) -> x.getId() == id).findFirst();

        if (a.isEmpty())
            shell.getServerOutputManager().print("В коллекции нет элемента с id=" + id + ".");
        else{
            Movie obj = (Movie) args[3];
            if (shell.getCollectionManager().contains(obj)){
                shell.getServerOutputManager().print("Такой элемент уже существует в коллекции");
                shell.getServerOutputManager().print("Элемент c id=\" + id + \" не будет обновлён.");
            } else {
                User user = (User) args[1];

                // obj - сырой объект Movie, данные из которого нужно установить в объект коллекции
                // устанавливаем ему поля id и date нужного объекта, и подменяем им изначальный объект
                obj.setId(id);
                obj.setCreationDate(a.get().getCreationDate());
                obj.setCreator(a.get().getCreator());

                try {
                    shell.getCollectionManager().update(obj, user);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (WrongUserException e) {
                    shell.getServerOutputManager().print("У вас недостаточно прав для изменения элемента id=" + id + ".");
                    return;
                }
                a.get().update(obj);
                shell.getCollectionManager().sort();
                shell.getServerOutputManager().print("Элемент c id=" + id + " обновлён.");
            }
        }
    }
}
