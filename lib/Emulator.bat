@echo off

cd
cd lib

set HMI="GisEmul.jar"
if not exist %HMI% goto ERR1

start javaw -Xmx512m -Xmn16m -jar %HMI%
exit

:ERR1
echo    Error : Can not find %HMI%
goto END

:END
sleep 5
exit