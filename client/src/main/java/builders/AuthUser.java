package builders;

import client.AbstractClientRequestManager;
import common.abstractions.IInputManager;
import common.abstractions.IOutputManager;
import common.exceptions.InterruptException;
import common.user.EncryptionManager;
import common.user.User;
import common.utils.Pair;
import network.LoginCheckRequest;
import network.LoginCheckResponse;

import java.io.IOException;

public class AuthUser {
    protected static String getLogin(IInputManager input, IOutputManager output){
        while(true){
            try {
                output.print("Введите логин:");
                String line = input.nextLine();

                if (line == null || line.strip().equals("exit")){
                    throw new InterruptException();
                }

                if (!line.isBlank()){
                    return line;
                }
                else {
                    output.print("Некорректные данные.");
                    output.print("'" + line + "'");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected static String getPassword(IInputManager input, IOutputManager output){
        while(true){
            try {
                output.print("Введите пароль:");
                String line = input.nextLine();

                if (line == null || line.strip().equals("exit")){
                    throw new InterruptException();
                }

                if (!line.isBlank() && line.length() >= 6) {
                    return line;
                }
                else{
                    output.print("Пароль должен быть длиннее 6 символов.");
                    output.print("'" + line + "'");
                    continue;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Pair<User, String> getUser(IInputManager input, IOutputManager output, AbstractClientRequestManager requestManager){
        while(true){
            String login = getLogin(input, output);
            var request = new LoginCheckRequest(login);
            requestManager.makeRequest(request);

            try {
                var response = (LoginCheckResponse) requestManager.getResponse();

                if (!response.isLoginExists()){
                    output.print("Пользователь с таким логином не существует. Зарегистрируйте нового пользователя или введите существующий логин.");
                    continue;
                }
                String salt = response.getSalt();

                String password = getPassword(input, output);
                String hashedPwd = EncryptionManager.byteArrayToHexString(EncryptionManager.encrypt(password + salt));

                return new Pair<>(new User(login, hashedPwd), salt);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
