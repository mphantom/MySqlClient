package com.mphantom.mysqlclient.core;

import android.util.Log;

import com.mphantom.mysqlclient.model.Database;
import com.mphantom.mysqlclient.model.Function;
import com.mphantom.mysqlclient.model.Table;
import com.mphantom.mysqlclient.model.TableProperty;
import com.mphantom.mysqlclient.model.ConnectionInfo;
import com.mphantom.mysqlclient.model.Trigger;
import com.mphantom.mysqlclient.utils.Constant;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Created by wushaorong on 11/6/15.
 */
public class SqlConnection {
    private JdbcTemplate jdbcTemplate;
    private ConnectionInfo info;

    private BasicDataSource ds;

    public SqlConnection(ConnectionInfo info) {
        this.info = info;
        ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl("jdbc:mysql://" + info.getHost() + ":" + info.getPort() + "/" + info.getDatabase() + "?connectTimeout=3000&socketTimeout=20000");
        ds.setUsername(info.getUserName());
        ds.setPassword(info.getPassword());
        ds.setMaxWait(5000);
        ds.setMinIdle(2);

        jdbcTemplate = new JdbcTemplate(ds);
        jdbcTemplate.setQueryTimeout(5000);
    }

    public void close() {
        try {
            ds.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JdbcTemplate getJdbcTemplate() {
        if (ds == null || ds.isClosed()) {
            ds = new BasicDataSource();
            ds.setDriverClassName("com.mysql.jdbc.Driver");
            ds.setUrl("jdbc:mysql://" + info.getHost() + ":" + info.getPort() + "/" + info.getDatabase());
            ds.setUsername(info.getUserName());
            ds.setPassword(info.getPassword());
            jdbcTemplate = new JdbcTemplate(ds);
        }
        return jdbcTemplate;
    }

    public ConnectionInfo getInfo() {
        return info;
    }

    public void setInfo(ConnectionInfo info) {
        this.info = info;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof ConnectionInfo && (this == o || this.info.equals(((SqlConnection) o).info))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public List<Database> showDbs() {
        String sql = "show databases";
        List<Map<String, Object>> list = getJdbcTemplate().queryForList(sql);
        debug(sql, list);
        List<Database> dblist = new ArrayList<>();
        Iterator<Map<String, Object>> it = list.iterator();
        while (it.hasNext()) {
            Map<String, Object> map = it.next();
            Database db = new Database();
            db.setName(map.get(Constant.DATABASE).toString());
            dblist.add(db);
        }
        return dblist;
    }

    public void useDb(String dbname) {
        String sql = "use " + dbname;
        jdbcTemplate.execute(sql);
        debug(sql);
    }

    public List<Table> showTables() {
        String sql = "show full tables";
        List<Map<String, Object>> list = getJdbcTemplate().queryForList(sql);
        debug(sql, list);
        List<Table> tblist = new ArrayList<>();
        Iterator<Map<String, Object>> it = list.iterator();
        while (it.hasNext()) {
            Map<String, Object> map = it.next();
            Table tb = new Table();
            tb.setName(map.values().toArray()[0].toString());
            tb.setContent(map.values().toArray()[1].toString());
            tblist.add(tb);
        }
        return tblist;
    }

    public List<Function> showFunctions() {
        String sql = "SHOW FUNCTION STATUS";
        List<Map<String, Object>> list = getJdbcTemplate().queryForList(sql);
        debug(sql, list);
        List<Function> functions = new ArrayList<>();
        Iterator<Map<String, Object>> it = list.iterator();
        while (it.hasNext()) {
            Map<String, Object> map = it.next();
            Function function = new Function();
            function.setDb(getValue(map, "Db"));
            function.setName(getValue(map, "Name"));
            function.setType(getValue(map, "Type"));
            function.setDefiner(getValue(map, "Definer"));
            function.setModified(getValue(map, "Modified"));
            function.setCreated(getValue(map, "Created"));
            function.setSecurity_type(getValue(map, "Security_type"));
            function.setComment(getValue(map, "Comment"));
            function.setCharacter_set_client(getValue(map, "character_set_client"));
            function.setCollation_connection(getValue(map, "collation_connection"));
            function.setDatabase_collation(getValue(map, "Database Collation"));
            functions.add(function);
        }
        return functions;
    }

    public List<Trigger> showTriggers() {
        String sql = "show triggers";
        List<Map<String, Object>> list = getJdbcTemplate().queryForList(sql);
        debug(sql, list);
        List<Trigger> triggers = new ArrayList<>();
        Iterator<Map<String, Object>> it = list.iterator();
        while (it.hasNext()) {
            Map<String, Object> map = it.next();
            Trigger trigger = new Trigger();
            trigger.setTrigger(getValue(map, "Trigger"));
            trigger.setEvent(getValue(map, "Event"));
            trigger.setTable(getValue(map, "Table"));
            trigger.setStatement(getValue(map, "Statement"));
            trigger.setTiming(getValue(map, "Timing"));
            trigger.setCreated(getValue(map, "Created"));
            trigger.setSql_mode(getValue(map, "sql_mode"));
            trigger.setDefiner(getValue(map, "Definer"));
            trigger.setCharacter_set_client(getValue(map, "character_set_client"));
            trigger.setCollation_connection(getValue(map, "collation_connection"));
            trigger.setDatabaseCollation(getValue(map, "Database Collation"));
            triggers.add(trigger);
        }
        return triggers;
    }

    public void createTrigger(Trigger trigger) {
        deleteTrigger(trigger.getTrigger());
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TRIGGER ").append(trigger.getTrigger()).append(" ")
                .append(trigger.getTiming()).append(" ")
                .append(trigger.getEvent()).append(" ON ")
                .append(trigger.getTable()).append(" ")
                .append(" FOR EACH ROW ")
                .append(trigger.getStatement());
        String sql = sb.toString();
        getJdbcTemplate().execute(sql);
    }

    public void deleteTrigger(String trigger) {
        String sql = "DROP TRIGGER IF EXISTS " + trigger;
        getJdbcTemplate().execute(sql);
    }

    public void createFunction(Function info) {
        StringBuilder sb = new StringBuilder();
        sb.append(" CREATE FUNCTION ").append(info.getName())
                .append(" ").append(info.getComment());
        String sql = sb.toString();
        getJdbcTemplate().execute(sql);

    }

    public void deleteFunction(String name) {
        String sql = "DROP FUNCTION IF EXISTS " + name;
        getJdbcTemplate().execute(sql);
    }

    public void createView(String name, String viewInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE VIEW  ").append("name").append(" AS ")
                .append(viewInfo);
        String sql = sb.toString();
        getJdbcTemplate().execute(sql);
    }

    public void deleteView(String name) {
        String sql = "DROP VIEW IF EXISTS " + name;
        getJdbcTemplate().execute(sql);
    }

    public void createTable(String tablename, List<TableProperty> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE").append(" ");
        sb.append(tablename).append(" (");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).ConvertSql());
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        String sql = sb.toString();
        getJdbcTemplate().execute(sql);
    }

    public void deleteTable(String tableName) {
        String sql = "DROP TABLE " + tableName;
        getJdbcTemplate().execute(sql);
    }

    public void alterTable(List<String> sqls) {
        for (int i = 0; i < sqls.size(); i++) {
            Log.i("testforthealertTable", sqls.get(i));
            getJdbcTemplate().execute(sqls.get(i));
        }
    }

    public void insertInto(String sql) {
        getJdbcTemplate().execute(sql);
    }

    public void update(String sql) {
        getJdbcTemplate().execute(sql);
    }

    public void deleteFrom(String sql) {
        getJdbcTemplate().execute(sql);
    }

    public List<TableProperty> schema(String tablename) {
        String sql = "desc " + tablename;
        List<Map<String, Object>> list = getJdbcTemplate().queryForList(sql);
        debug(sql, list);

        List<TableProperty> tbproplist = new ArrayList<>();
        Iterator<Map<String, Object>> it = list.iterator();
        while (it.hasNext()) {
            Map<String, Object> map = it.next();
            TableProperty tbprop = new TableProperty();
            tbprop.setField(getValue(map, "Field"));
            tbprop.setKey(getValue(map, "Key"));
            tbprop.setType(getValue(map, "Type"));
            tbprop.setExtra(getValue(map, "Extra"));
            tbprop.set_null(getValue(map, "Null"));
            tbprop.set_default(getValue(map, "Default"));
            tbproplist.add(tbprop);
        }
        return tbproplist;
    }

    public List<Map<String, Object>> queryAll(String tablename, int page, int pagesize) {
        int offset = (page - 1) * pagesize;
        String sql = "select * from " + tablename + " limit " + pagesize + " offset " + offset;
        List<Map<String, Object>> list = getJdbcTemplate().queryForList(sql);
        debug(sql, list);
        return list;
    }

    public List<Map<String, Object>> queryWithCondition(String tablename, String condition) {
        String sql = "select * from " + tablename + " Where " + condition;
        List<Map<String, Object>> list = getJdbcTemplate().queryForList(sql);
        debug(sql, list);
        return list;
    }

    public int queryItemCount(String tablename) {
        String sql = "select count(*) from " + tablename;
        debug(sql);
        return getJdbcTemplate().queryForObject(sql, int.class);
    }

    public HashMap<String, Object> dosql(String sql) {
        sql = sql.trim();
        HashMap<String, Object> result = new HashMap<>();
        if (sql.endsWith("\\G") || sql.endsWith("\\g")) {
            sql = sql.substring(0, sql.length() - 2);
        }
        long begin = new Date().getTime();
        if (sql.toLowerCase().equals("help")) {
            List<Map<String, Object>> list = SqlHelper.getHelpList();
            result.put(Constant.QUERY_TYPE, 0);
            result.put(Constant.QUERY_RESULT, list);
        } else if (sql.toLowerCase().matches("^\\s*(select|show|desc)\\s+.*$")) {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
            result.put(Constant.QUERY_TYPE, 0);
            if (list == null) {
                list = new ArrayList<>();
            }
            result.put(Constant.QUERY_RESULT, list);
        } else {
            int count = jdbcTemplate.update(sql);
            result.put(Constant.QUERY_TYPE, 1);
            result.put(Constant.QUERY_RESULT, count);
        }
        float time = (new Date().getTime() - begin) / 1000.0f;
        result.put(Constant.QUERY_TIME, time);
        return result;
    }

    private String getValue(Map<String, Object> map, String key) {
        return map.get(key) == null ? "" : map.get(key).toString();
    }

    private void debug(String sql, List<Map<String, Object>> list) {
        for (Map<String, Object> map : list) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Log.d("clientDebug", entry.getKey() + "-->" + entry.getValue());
            }
        }
    }

    private void debug(String sql) {
        Log.d("clientDebug", "sql" + "-->" + sql);
    }


}
