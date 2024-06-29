# flight_booking_backend

flight booking backend



### install db

#### for local running environment,

1. Please install MySQL Community Server 8.0 or 8.4 from https://dev.mysql.com/downloads/mysql/ . Please choose MSI Installer instead of ZIP Archive.
2. Please install MySQL Workbench from https://dev.mysql.com/downloads/workbench/
3. After the installation of mysql, please open MySQL Workbench. If the workbench cannot auto-detect a mysql server running, please run it manually: find your mysql server installation path, and run /bin/mysqld.exe.
4. In MySQL Workbench, run sql statement from **sqls** file on your mysql instance. Two tables (**sys_user** and **user_search_log**) should be created after executing the statement from sqls.
5. After successful creation of two tables, you can turn to the Java backend for following development.
