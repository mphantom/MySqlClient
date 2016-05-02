package com.mphantom.mysqlclient.mysql;


import com.mphantom.mysqlclient.model.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wushaorong on 11/6/15.
 */
public class MySqlConnection {
    private final String SQL_DRIVER = "com.mysql.jdbc.Driver";
    private ConnectionInfo info;
    private Connection connection;

    public MySqlConnection(ConnectionInfo info) {
        this.info = info;
        try {
            Class.forName(SQL_DRIVER);
            connection=getConnection();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    private Connection getConnection() {
        if(connection==null){
            try {
                connection=DriverManager.getConnection("jdbc:mysql://"+info.getHost()+":"
                        +info.getPort()+"/"+info.getDatabase()+"?connectTimeout=3000&socketTimeout=20000",
                        info.getUserName(),info.getPassword());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    private Statement getStatement() {
        if (connection == null) {
            connection = getConnection();
        }
        try {
            return connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    public int queryItemCount(String sql) {
        Statement stmt = getStatement();
        try {
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
//                stmt.execute()
                debug(sql);
                return resultSet.getInt("COUNT(*)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Database> showDatabase(){
        String sql="show databases";
        List<Database> list= new ArrayList<>();
        Statement st=getStatement();
        try {
            debug(sql);
            ResultSet result=st.executeQuery(sql);
            while(result.next()){
                Database database=new Database();
                database.setName(result.getString(1));
                list.add(database);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    private void debug(String sql) {
        System.out.println(sql);
    }
}
