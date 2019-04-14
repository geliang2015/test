#!/bin/bash
kill -9 `cat server.pid`
if [ $? -eq 0 ];then
	echo "cash-tenant-server is stopped!"
else
	echo "cash-tenant-server stop failed!"
fi
