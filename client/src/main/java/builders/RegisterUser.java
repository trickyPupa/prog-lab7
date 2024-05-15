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

public class RegisterUser extends AuthUser{
    protected static String getPassword(IInputManager input, IOutputManager output){
        while(true){
            try {
                output.print("Введите пароль:");
                String line = input.nextLine();

                if (line == null || line.strip().equals("exit")){
                    throw new InterruptException();
                }

                if (line.isBlank() || line.length() < 6){
                    output.print("Пароль должен быть длиннее 6 символов.");
                    output.print("'" + line + "'");
                    continue;
                }

                // подтверждение пароля
                output.print("Подтверждение пароля:");
                String line2 = input.nextLine();

                if (line2 == null || line2.strip().equals("exit")){
                    throw new InterruptException();
                }

                if (line2.equals(line)){
                    return line;
                } else {
                    output.print("Пароли не совпадают. Пожалуйста введите пароль заново.");
                    output.print("'" + line + "'");
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

                if (response.isLoginExists()){
                    output.print("Пользователь с таким логином уже существует. Придумайте другой или войдите в аккаунт.");
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
