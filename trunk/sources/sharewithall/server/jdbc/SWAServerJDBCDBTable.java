/**
 * 
 */
package sharewithall.server.jdbc;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Authors:
 *    Alex Catarineu
 *    Ferran Rigual
 *    Miguel Angel Vico
 *
 * Creation date: Oct 31, 2010
 */
public abstract class SWAServerJDBCDBTable
{
    
    protected Connection c;
    protected ArrayList<String> fields = new ArrayList<String>();
    protected ArrayList<String> pkeys = new ArrayList<String>();

    protected abstract String get_name();
    protected abstract Object read_obj(ResultSet rs);
    protected abstract SWAServerJDBCPredicate[] write_obj(Object obj);

    public SWAServerJDBCDBTable()
    {
        try {
            FileInputStream in = new FileInputStream("connection.properties");
            Properties p = new Properties();
            p.load(in);
    
            String driver = p.getProperty("driver");
            p.remove("driver");
            String url = p.getProperty("url");
            p.remove("url");
    
            Class.forName(driver);
            c = DriverManager.getConnection(url, p);
            c.setAutoCommit(false);
    
            DatabaseMetaData meta = c.getMetaData();
            ResultSet rs = meta.getPrimaryKeys(null, null, get_name());
            while (rs.next()) pkeys.add(rs.getString("COLUMN_NAME"));
            rs.close();
            
            rs = meta.getColumns(null, null, get_name(), null);
            while (rs.next()) fields.add(rs.getString("COLUMN_NAME"));
            rs.close();
        }
        catch (Exception ex) {
            System.out.println("Server exception: " + ex.getClass() + ":" + ex.getMessage());
        }
    }

    public void close() throws SQLException
    {
        if (c != null) c.close();
    }

    @Override
    protected void finalize() throws Throwable
    {
        try {
            close();
        }
        finally {
            super.finalize();
        }
    }

    public Object get_key(Object... keys) throws Exception
    {
        PreparedStatement ps = prepare_key("SELECT * FROM " + get_name(), keys);
        ResultSet rs = ps.executeQuery();
        ps.close();
        if (!rs.next()) {
            rs.close();
            return null;
        }
        Object ret = read_obj(rs);
        rs.close();
        return ret;
    }

    public ArrayList<Object> select_gen(SWAServerJDBCPredicate... preds) throws Exception
    {
        PreparedStatement ps = prepare_gen("SELECT * FROM " + get_name(), preds);
        fill_prepared(ps, preds);
        ResultSet rs = ps.executeQuery();
        ps.close();
        ArrayList<Object> ret = new ArrayList<Object>();
        while (rs.next()) ret.add(read_obj(rs));
        rs.close();
        return ret;
    }

    public int insert_obj(Object obj) throws SQLException
    {
        SWAServerJDBCPredicate[] preds = write_obj(obj);
        String keys = "";
        String values = "";
        for (int i = 0; i < preds.length; ++i)
        {
            if (i > 0)
            {
                keys += ", ";
                values += ", ";
            }
            keys += preds[i].key;
            values += "?";
        }

        String sql = "INSERT INTO " + get_name() + " (" + keys + ") VALUES (" + values + ");";
        PreparedStatement ps = c.prepareStatement(sql);
        fill_prepared(ps, preds);

        int res = ps.executeUpdate();
        ps.close();
        return res;
    }

    public int update_obj(Object obj) throws Exception
    {
        SWAServerJDBCPredicate[] preds = write_obj(obj);
        SWAServerJDBCPredicate[] keys = new SWAServerJDBCPredicate[pkeys.size()];
        String sql = "UPDATE " + get_name() + " SET ";
        int count = 0;
        for (int i = 0; i < preds.length; ++i)
        {
            if (i > 0) sql += ", ";
            if (preds[i].value == null) sql += preds[i].key + " = NULL";
            else sql += preds[i].key + " = ?";
            if (pkeys.contains(preds[i].key))
            {
                if (preds[i].value == null) throw new Exception("Primary keys cannot be NULL");
                keys[count++] = preds[i];
            }
        }
        if (count != pkeys.size()) throw new Exception("Not all primary keys are set");
        PreparedStatement ps = prepare_gen(sql, keys);
        SWAServerJDBCPredicate[] all = new SWAServerJDBCPredicate[preds.length + keys.length];
        for (int i = 0; i < preds.length; ++i)
            all[i] = preds[i];
        for (int i = preds.length; i < all.length; ++i)
            all[i] = keys[i - preds.length];
        fill_prepared(ps, all);
        
        int res = ps.executeUpdate();
        ps.close();
        return res;
    }

    public int delete_key(Object... keys) throws Exception
    {
        PreparedStatement ps = prepare_key("DELETE FROM " + get_name(), keys);
        int res = ps.executeUpdate();
        ps.close();
        return res;
    }
    
    public int delete_gen(SWAServerJDBCPredicate... preds) throws Exception
    {
        PreparedStatement ps = prepare_gen("DELETE FROM " + get_name(), preds);
        fill_prepared(ps, preds);
        int res = ps.executeUpdate();
        ps.close();
        return res;
    }

    public boolean exists_gen(SWAServerJDBCPredicate... preds) throws Exception
    {
        PreparedStatement ps = prepare_gen("SELECT EXISTS(SELECT * FROM " + get_name(), preds, ") AS exists");
        fill_prepared(ps, preds);
        ResultSet rs = ps.executeQuery();
        ps.close();
        if (!rs.next())
        {
            rs.close();
            return false;
        }
        return rs.getBoolean("exists");
    }
        
    public boolean exists_key(Object... keys) throws Exception
    {
        PreparedStatement ps = prepare_key("SELECT EXISTS(SELECT * FROM " + get_name(), keys, ") AS exists");
        ResultSet rs = ps.executeQuery();
        ps.close();
        if (!rs.next())
        {
            rs.close();
            return false;
        }
        return rs.getBoolean("exists");
    }
    
    public void commit() throws SQLException
    {
        c.commit();
    }

    public void rollback() throws SQLException
    {
        c.rollback();
    }
    
    protected void fill_prepared(PreparedStatement ps, SWAServerJDBCPredicate[] preds) throws SQLException
    {
        int count = 1;
        for (int i = 0; i < preds.length; ++i)
        {
            if (preds[i] == null) continue;
            else ps.setObject(count, preds[i].value);
            ++count;
        }
    }

    protected PreparedStatement prepare_key(String sql, Object[] keys) throws Exception
    {
        return prepare_key(sql, keys, "");
    }
    
    protected PreparedStatement prepare_key(String sql, Object[] keys, String after) throws Exception
    {
        if (keys.length != pkeys.size()) throw new Exception("The number of primary keys passed is not correct");
        String cond = " WHERE ";

        for (int i = 0; i < keys.length; ++i)
        {
            if (i > 0) cond += " AND ";
            cond += pkeys.get(i) + " = ?";
        }

        PreparedStatement ps = c.prepareStatement(sql + cond + " " + after + ";");

        for (int i = 0; i < keys.length; ++i)
        {
            if (keys[i] == null) throw new Exception("Primary keys cannot be NULL");
            else ps.setObject(i, keys[i]);
        }

        return ps;
    }

    protected PreparedStatement prepare_gen(String sql, SWAServerJDBCPredicate[] preds) throws Exception
    {
        return prepare_gen(sql, preds, "");
    }

    protected PreparedStatement prepare_gen(String sql, SWAServerJDBCPredicate[] preds, String after) throws Exception
    {
        String cond = " WHERE ";

        for (int i = 0; i < preds.length; ++i)
        {
            if (i > 0) cond += " AND ";
            cond += preds[i].key;
            if (preds[i].value == null)
            {
                if (preds[i].operator.equals("=")) cond += " IS NULL";
                else if (preds[i].operator.equals("!=") || preds[i].operator.equals("<>")) cond += " IS NOT NULL";
                else throw new Exception("Operator for NULL values must be either '=' or '!='");
            }
            else cond += " " + preds[i].operator + " ?";
        }

        PreparedStatement ps = c.prepareStatement(sql + cond + " " + after + ";");

        return ps;
    }

}
