package common.abstractions;

/**
 * Базовый интерфейс для обработчиков выходных потоков для программы.
 */
public interface IOutputManager {
    /**
     * Печатает переданные данные в доступный поток вывода.
     * @param s данные для печати
     */
    void print(String s);
    void print(Object s);
}
