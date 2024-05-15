package common.commands.implementations;

import common.commands.abstractions.AbstractCommand;
import common.abstractions.AbstractReceiver;
import common.commands.abstractions.Command;

import java.util.function.Function;


public class AddCommand extends AbstractCommand{
    private static final String tab = " ".repeat(56);
    private static final String description = "Команда для добавления Movie в коллекцию. Требуется ввести все " +
            "характеристики фильма:\n" + tab + "- Название (строка), количество \"Оскаров\" (целое число), количество " +
            "золотых пальмовых ветвей (целое число или пустая строка), продолжительность фильма " +
            "(целое число)\n" + tab + "- Координаты (2 целых числа: первое больше -879, второе  не больше 155)\n" +
            tab + "- MPAA" +
            " рейтинг фильма (одна из строк: PG, PG_13, NC_17)\n" +
            tab + "- Режиссер фильма: имя, дата " +
            "рождения в формате ДД.ММ.ГГГГ, цвет глаз (одна из строк: BLUE, YELLOW, ORANGE, WHITE, " +
            "BROWN.), на отдельной строке цвет волос (одна " +
            "из строк: GREEN, RED, BLUE, YELLOW, ORANGE), национальность (одна из " +
            "доступных стран: FRANCE, INDIA, VATICAN, THAILAND), местонахождение (3 " +
            "числа-координаты: дробное, целое, целое)";

    public AddCommand(Object[] args) {
        super("add", description, "{element}", args);
    }

    @Override
    public void execute(String[] s, AbstractReceiver r) {
//        Movie element = Movie.createMovie1(shell.getInputManager(), shell.getOutputManager());
//
//        try {
//            shell.getCollectionManager().add(element);
//        } catch (WrongArgumentException e){
//            throw new WrongArgumentException(getName());
//        }

        r.add(s);
    }
    @Override
    public void execute(AbstractReceiver rec) {
        rec.add(getArgs());
    }

    @Override
    public Function<Object[], Command> getConstructor() {
        return AddCommand::new;
    }

}
