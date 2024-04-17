package client;

import common.abstractions.IInputManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;

/**
 * Конкретный класс реализует {@link IInputManager}, осуществляет доставку входных данных программе из различных источников.
 */
public class InputManager implements IInputManager {
    protected InputStream input;
    protected BufferedReader normalInput;
    protected ArrayDeque<BufferedReader> temporaryInput;

    public InputManager(InputStream input){
        this.input = input;
        normalInput = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        temporaryInput = new ArrayDeque<>();
    }

    public InputManager(Reader input){
        normalInput = new BufferedReader(input);
    }

    /**
     * @return Возвращает следующую строку входных данных их текущего потока.
     * @throws IOException - если происходит ошибка ввода/вывода
     */
    @Override
    public String nextLine() throws IOException {
        String line = "";
//        int c;

//        while ((char)(c = currentInput.read()) != '\n' && (char)c != '\r'){
//            if (c == -1) {
//                currentInput = normalInput;
//                return line;
//            }
//            line = line + (char)c;
//        }
//        return line.strip();

        while (!temporaryInput.isEmpty()){
            if ((line = temporaryInput.getLast().readLine()) != null) {
                if (line.isBlank()) continue;
                return line;
            }
            else {
                temporaryInput.getLast().close();
                temporaryInput.removeLast();

                System.out.println("Конец исполнения файла.");
            }
        }
        line = normalInput.readLine();
        if (line == null) return "exit";
        return line.strip();
    }

    /*public String nextWord() throws IOException {
        String word = "";
        int c;
        while (true){
            if (temporaryInput == null || (c = temporaryInput.read()) == -1) {
//                (char) (c = (normalInput.read())) != ' '
                c = normalInput.read();
            }

            if (c == -1 || (char) c == '\n' || (char) c == ' ') return word.strip();
            word = word + (char)c;
        }
//        return word;
    }*/

    /**
     * Устанавливает поток, из которого требуется читать данные в обход основного. Когда поток исчерпается, произойдет возвращение к основному.
     * @param input новый поток
     */
    @Override
    public void setTemporaryInput(Reader input){
        temporaryInput.add(new BufferedReader(input));
    }

    @Override
    public void setTemporaryInput(InputStream input) {
        temporaryInput.add(new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)));
    }
}
