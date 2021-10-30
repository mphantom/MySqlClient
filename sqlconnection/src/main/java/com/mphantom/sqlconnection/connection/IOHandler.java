package com.mphantom.sqlconnection.connection;


import com.mphantom.sqlconnection.protocol.MysqlMessage;

public interface IOHandler {
	
	void handler(Connection conn, MysqlMessage msg);

}
