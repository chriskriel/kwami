RESET SERVER
SET SERVER PROCESSTYPE OSS
SET SERVER ARGLIST -Xabend,-Xnoclassgc,tz.vodacom.in.generic.client.SimplePath&
wayServer,amt
SET SERVER AUTORESTART 0
SET SERVER CREATEDELAY 10 SECS
SET SERVER CWD /home/rmschk/siemens
SET SERVER DEBUG OFF
SET SERVER DELETEDELAY 10 MINS
SET SERVER ENV EMS_COLLECTOR=$DVLOG
SET SERVER ENV CLASSPATH="/usr/tandem/javaextv20/lib/tdmext.jar:$JAVA_HOME/lib&
/tools.jar:/home/rmschk/siemens:/home/rmschk/siemens/lib/antlr-2.7.2.jar:/home&
/rmschk/siemens/lib/avalon-framework-4.1.5.jar:/home/rmschk/siemens/lib/backpo&
rt-util-concurrent.jar:/home/rmschk/siemens/lib/concurrent-1.3.2.jar&
:/home/rmschk/siemens/lib/jacorb.jar:/home&
/rmschk/siemens/lib/log4j-1.2.15.jar:/home/rmschk/siemens/lib/logkit-1.2.jar:/&
home/rmschk/siemens/lib/picocontainer-1.2.jar:/home/rmschk/siemens/lib/tz.voda&
com.in.generic.ppfeclient-1.jar:/home/rmschk/siemens/lib/tz.vodacom.in.siemens&
.generic142-1.jar"
SET SERVER ENV USER_COMMANDS="-Dlog4jFile=SimpleServer-log4j.xml -DpropertiesF&
ile=SimpleServer.properties"
SET SERVER HIGHPIN ON
SET SERVER HOMETERM \TIMD.$ZHOME
SET SERVER DEFINE =TCPIP^PROCESS^NAME, CLASS MAP, FILE \TIMD.$ZB018
SET SERVER LINKDEPTH 1
SET SERVER MAXLINKS 1
SET SERVER MAXSERVERS 2
SET SERVER NUMSTATIC 1
SET SERVER OWNER \TIMD.101,2
SET SERVER PRI 10
SET SERVER PROGRAM /usr/tandem/java/bin/java
SET SERVER SECURITY "N"
SET SERVER STDERR /home/rmschk/siemens/stderr
SET SERVER STDOUT /home/rmschk/siemens/stdout
SET SERVER TMF ON
SET SERVER VOLUME \TIMD.$APP001.DZAVODA
ADD SERVER IN-CORBA-CLIENT
START SERVER IN-CORBA-CLIENT