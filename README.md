# Лабораторная работа #7

### Доработать программу из лабораторной работы №6 следующим образом:

1.    Организовать хранение коллекции в реляционной СУБД (PostgresQL). Убрать хранение коллекции в файле.
2.    Для генерации поля id использовать средства базы данных (sequence).
3.    Обновлять состояние коллекции в памяти только при успешном добавлении объекта в БД
4.    Все команды получения данных должны работать с коллекцией в памяти, а не в БД
5.    Организовать возможность регистрации и авторизации пользователей. У пользователя есть возможность указать пароль.
6.    Пароли при хранении хэшировать алгоритмом SHA-256
7.    Запретить выполнение команд не авторизованным пользователям.
8.    При хранении объектов сохранять информацию о пользователе, который создал этот объект.
9.    Пользователи должны иметь возможность просмотра всех объектов коллекции, но модифицировать могут только принадлежащие им.
10.    Для идентификации пользователя отправлять логин и пароль с каждым запросом.

### Необходимо реализовать многопоточную обработку запросов.

1.    Для многопоточного чтения запросов использовать Cached thread pool
2.    Для многопотчной обработки полученного запроса использовать создание нового потока (java.lang.Thread)
3.    Для многопоточной отправки ответа использовать создание нового потока (java.lang.Thread)
4.    Для синхронизации доступа к коллекции использовать потокобезопасные аналоги коллекции из java.util.concurrent

### Порядок выполнения работы:

1.    В качестве базы данных использовать PostgreSQL.
2.    Для подключения к БД на кафедральном сервере использовать хост pg, имя базы данных - studs, имя пользователя/пароль совпадают с таковыми для подключения к серверу.

### Отчёт по работе должен содержать:

-    Текст задания.
-    Диаграмма классов разработанной программы.
-    Исходный код программы.
-    Выводы по работе.

### Вопросы к защите лабораторной работы:

-    Многопоточность. Класс Thread, интерфейс Runnable. Модификатор synchronized.
-    Методы wait(), notify() класса Object, интерфейсы Lock и Condition.
-    Классы-сихронизаторы из пакета java.util.concurrent.
-    Модификатор volatile. Атомарные типы данных и операции.
-    Коллекции из пакета java.util.concurrent.
-    Интерфейсы Executor, ExecutorService, Callable, Future
-    Пулы потоков
-    JDBC. Порядок взаимодействия с базой данных. Класс DriverManager. Интерфейс Connection
-    Интерфейсы Statement, PreparedStatement, ResultSet, RowSet
-    Шаблоны проектирования.


