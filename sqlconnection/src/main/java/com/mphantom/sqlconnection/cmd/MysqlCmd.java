package com.mphantom.sqlconnection.cmd;


import com.mphantom.sqlconnection.protocol.MysqlMessage;

public interface MysqlCmd {
	
	MysqlMessage mysqlCmd();
	
	int cmdType();

}
