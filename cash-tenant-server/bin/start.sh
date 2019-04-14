#!/bin/bash
JRAIN_BIN="${BASH_SOURCE-$0}"
JRAIN_BIN="$(dirname "${JRAIN_BIN}")"
JRAIN_BIN_DIR="$(cd "${JRAIN_BIN}"; pwd)"
JRAIN_HOME="${JRAIN_BIN_DIR}/.."

JRAIN_CFG_DIR="$JRAIN_HOME/conf"
JRIAN_PAGE_DIR="$JRAIN_HOME/pages"
CLASSPATH="$JRAIN_CFG_DIR:$JRIAN_PAGE_DIR:$CLASSPATH"
 

for i in "$JRAIN_HOME"/libs/*.jar
do
    CLASSPATH="$i:$CLASSPATH"
done

JRAIN_MAIN=cash.tenant.main.CashTenantServer
#java -DOUE_LOG_TYPE=slf4j -cp  $CLASSPATH $JRAIN_MAIN
nohup java -DOUE_LOG_TYPE=slf4j -cp  $CLASSPATH $JRAIN_MAIN  >/dev/null 2>&1 &
if [ $? -eq 0 ];then
	echo $! > server.pid
	echo "cash-tenant-server is started!"
else
	echo "cash-tenant-server start failed!"
fi
