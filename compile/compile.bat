@echo off
set "C=%~dp0"
set "C=%C:~0,-1%"
for %%I in ("%C%") do set "P=%%~dpI"
set "P=%P:~0,-1%"
set "G=%P%\graalvm"
set "T=%P%\target"
rd /S /Q %G%
cd %P%
call mvn -f %P%\pom.xml clean package
call java --enable-preview -agentlib:native-image-agent=config-output-dir=%G% -jar %T%\Migration-jar-with-dependencies.jar version
echo F | xcopy %C%\config_backup.xml %P%\config.xml /Y
call java --enable-preview -agentlib:native-image-agent=config-merge-dir=%G% -jar %T%\Migration-jar-with-dependencies.jar graal
echo F | xcopy %C%\config_missing_net_static.xml %P%\config.xml /Y
call java --enable-preview -agentlib:native-image-agent=config-merge-dir=%G% -jar %T%\Migration-jar-with-dependencies.jar graal
echo F | xcopy %C%\config_no_statics.xml %P%\config.xml /Y
call java --enable-preview -agentlib:native-image-agent=config-merge-dir=%G% -jar %T%\Migration-jar-with-dependencies.jar graal
echo F | xcopy %C%\config_no_statics_hard.xml %P%\config.xml /Y
call java --enable-preview -agentlib:native-image-agent=config-merge-dir=%G% -jar %T%\Migration-jar-with-dependencies.jar graal
echo F | xcopy %C%\config_no_subnets.xml %P%\config.xml /Y
call java --enable-preview -agentlib:native-image-agent=config-merge-dir=%G% -jar %T%\Migration-jar-with-dependencies.jar graal
echo F | xcopy %C%\config_no_subnets_hard.xml %P%\config.xml /Y
call java --enable-preview -agentlib:native-image-agent=config-merge-dir=%G% -jar %T%\Migration-jar-with-dependencies.jar graal
del %P%\config.xml
del %P%\new_config.xml
cd %C%
call mvn -f %P%\pom.xml clean -Pnative native:compile
