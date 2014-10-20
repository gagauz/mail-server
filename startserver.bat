set cls=%cd%\bin
set JAVA_HOME=r:\jdk1.8
set Path=%JAVA_HOME%\bin;%Path%

cd %cls%
rem -Djava.util.logging.config.file=logging.properties 
call java -cp %cls% ru.gagauz.mail.server.test.TestServers

pause