title cash tenant server
setlocal 
REM add the config dir to classpath
set CLASSPATH=%~dp0..\conf;%CLASSPATH%
set CLASSPATH=%~dp0..\pages;%CLASSPATH%
set CLASSPATH=%~dp0..\libs\*;%CLASSPATH%
set JRAIN_MAIN=cash.tenant.main.CashTenantServer
set JRAIN_HOME=%~dp0%..
cls 
java -DOUE_LOG_TYPE=slf4j  -cp "%CLASSPATH%" %JRAIN_MAIN%  
endlocal