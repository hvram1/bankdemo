@echo off
set API="."
set JAVA=java
cd %API%
for %%i in (".\lib\*.jar") do call ".\lcp.bat" %%i
for %%i in (".\lib\ext\*.jar") do call ".\lcp.bat" %%i


%JAVA%   -Xmx128m -Xms32m  -cp "%LOCALCLASSPATH%;./classes;./components" com.alipay.test.TestMessageBinder
pause