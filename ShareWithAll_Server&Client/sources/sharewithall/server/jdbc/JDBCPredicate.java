/**
 * 
 */
package sharewithall.server.jdbc;

/**
 * Authors:
 *    Alex Catarineu
 *    Ferran Rigual
 *    Miguel Angel Vico
 *
 * Creation date: Oct 31, 2010
 */
public class JDBCPredicate
{
    
    public String key;
    public Object value;
    public String operator; // EQ, NEQ, GT, LT, GE, LE

    public JDBCPredicate(String key, Object value)
    {
        super();
        this.key = key;
        this.value = value;
        this.operator = "=";
    }
    
    public JDBCPredicate(String key, Object value, String operator)
    {
        super();
        this.key = key;
        this.value = value;
        this.operator = operator;
    }

}
