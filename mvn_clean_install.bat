@echo off
set Path=%Path%;R:\maven\bin
set MAVEN_OPTS=%MAVEN_OPTS% -Xmx1424m -Xms1424m -XX:PermSize=200M -XX:MaxPermSize=200M

call mvn -e clean install -Dmaven.test.skip=false -DdownloadSources=true -DdownloadSource=true

pause