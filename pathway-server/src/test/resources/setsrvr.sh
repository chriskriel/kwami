RESET SERVER
SET SERVER PROCESSTYPE OSS
SET SERVER ARGLIST -Xms128m,-Xmx512m,-DDMSTEST,-Xabend,-Xnoclassgc,&
netw.kwami.ServerWithDataSource
SET SERVER AUTORESTART 0
SET SERVER CREATEDELAY 10 SECS
SET SERVER CWD /home/java/test/apps/pathway
SET SERVER DEBUG OFF
SET SERVER DELETEDELAY 10 MINS
SET SERVER ENV EMS_COLLECTOR=$DVLOG
SET SERVER ENV CLASSPATH="&
/usr/tandem/javaexth11/lib/tdmext.jar&
:/usr/tandem/jdbcMx/current/lib/jdbcMx.jar&
:$HOME/apps/lib/kwami-pathsend-utils-1.jar&
:$HOME/apps/lib/kwami-base-utils-3.jar&
:$HOME/lib/tomcat-jdbc-7.0.70.jar&
:$HOME/lib/tomcat-juli-7.0.70.jar&
:$HOME/test/apps/pathway&
:$HOME/test/apps/pathway/pathway-server-example-1.jar&
"
SET SERVER ENV USER_COMMANDS="&
-Djava.library.path=/usr/tandem/javaexth11/lib:/usr/tandem/jdbcMx/current/lib
"
SET SERVER HIGHPIN ON
SET SERVER HOMETERM \TANZM.$ZHOME
SET SERVER DEFINE =TCPIP^PROCESS^NAME, CLASS MAP, FILE \TANZM.$ZTC0
SET SERVER LINKDEPTH 20
SET SERVER MAXLINKS 20
SET SERVER MAXSERVERS 10
SET SERVER NUMSTATIC 10
SET SERVER OWNER \TANZM.101,2
SET SERVER PRI 10
SET SERVER PROGRAM /usr/tandem/java/bin/java
SET SERVER SECURITY "N"
SET SERVER STDERR /home/java/test/logs/pathway/stderr
SET SERVER STDOUT /home/java/test/logs/pathway/stdout
SET SERVER TMF ON
SET SERVER VOLUME \TIMD.$APP001.DDCVODA
ADD SERVER SERVER-WITH-DATASOURCE
START SERVER SERVER-WITH-DATASOURCE


