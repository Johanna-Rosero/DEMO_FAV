// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package routines.system;

import java.util.HashMap;
import java.util.Map;

public class DBManagerFactory {

    private static Map<String, DBManager> managerMap = new HashMap<String, DBManager>();

    public static DBManager getDBManager(String dbmsId) {
        DBManager manager = managerMap.get(dbmsId);

        if (manager == null) {
            DBMSConstants curDBMS = null;
            for (DBMSConstants dbms : DBMSConstants.values()) {
                if (dbmsId.equalsIgnoreCase(dbms.getDBmsId())) {
                    curDBMS = dbms;
                    break;
                }
            }
            if (curDBMS == DBMSConstants.AS400) {
                manager = new AS400Manager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.ACCESS) {
                manager = new AccessManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.DB2) {
                manager = new DB2Manager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.FIREBIRD) {
                manager = new FirebirdManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.HSQLDB) {
                manager = new HSQLDBManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.INFORMIX) {
                manager = new InformixManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.INGRES) {
                manager = new IngresManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.VECTORWISE) {
                manager = new VectorWiseManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.JAVADB) {
                manager = new JavaDBManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.MAXDB) {
                manager = new MaxDBManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.MSSQL) {
                manager = new MSSQLManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.MYSQL) {
                manager = new MysqlManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.NETEZZA) {
                manager = new NetezzaManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.ORACLE) {
                manager = new OracleManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.POSTGREPLUS) {
                manager = new PostgrePlusManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.POSTGRESQL) {
                manager = new PostgreManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.SQLITE) {
                manager = new SQLiteManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.SYBASE) {
                manager = new SybaseManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.TERADATA) {
                manager = new TeradataManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.VERTICA) {
                manager = new VerticaManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.SAPHANA) {
                manager = new SAPHanaManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.ODBC) {
                manager = new ODBCManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.REDSHIFT) {
                manager = new RedshiftManager(curDBMS.getDBmsId());
            } else if (curDBMS == DBMSConstants.EXASOL) {
                manager = new ExasolManager(curDBMS.getDBmsId());
            }
        }
        managerMap.put(dbmsId, manager);
        return manager;
    }
}

abstract class DBManager {

    protected abstract String getLProtectedChar();

    protected abstract String getRProtectedChar();

    protected abstract String getDBMSId();

    protected String getLProtectedChar(String columName) {
        return getLProtectedChar();
    }

    protected String getRProtectedChar(String columName) {
        return getRProtectedChar();
    }

    private String getSqlStmt(DynamicMetadata column) {
        if (column != null) {
            if (column.getType().equals("id_Geometry"))
                return "GeomFromText(?, ?)"; // For PostGIS
            else
                return "?";
        } else {
            return "?";
        }
    }

    public String getUpdateSetSQL(Dynamic dynamic, StringBuilder updateSetStmt) {
        String ending = ",";
        if (updateSetStmt.length() != 0) {
            updateSetStmt.append(ending).append(" ");
        }
        for (int i = 0; i < dynamic.getColumnCount(); i++) {
            DynamicMetadata column = dynamic.getColumnMetadata(i);
            if (column.isKey()) {
                continue;
            }
            if (updateSetStmt.length() != 0 && i != 0) {
                updateSetStmt.append(ending);
            }
            updateSetStmt.append(getLProtectedChar(column.getName()) + column.getName() + getRProtectedChar(column.getName())
                    + " = " + getSqlStmt(column));
        }

        return updateSetStmt.toString();
    }

    public String getUpdateWhereSQL(Dynamic dynamic, StringBuilder updateWhereStmt, boolean supportNull) {
        for (int i = 0; i < dynamic.getColumnCount(); i++) {
            DynamicMetadata column = dynamic.getColumnMetadata(i);
            if (column.isKey()) {
                if (updateWhereStmt.length() != 0) {
                    updateWhereStmt.append(" AND ");
                }
                if (supportNull && column.isNullable()) {
                    updateWhereStmt.append("((").append(getLProtectedChar(column.getName())).append(column.getName())
                            .append(getRProtectedChar(column.getName())).append(" IS NULL AND true = ")
                            .append(getSqlStmt(column)).append(") OR ");
                }
                updateWhereStmt.append(getLProtectedChar(column.getName())).append(column.getName()).append(getRProtectedChar(column.getName())).append(" = ").append(getSqlStmt(column));
                if (supportNull && column.isNullable()) {
                    updateWhereStmt.append(")");
                }
            }
        }
        if (updateWhereStmt.length() == 0) {
            throw new RuntimeException("No key specified");
        }
        return updateWhereStmt.toString();

    }

    public String getInsertTableSQL(Dynamic dynamic, StringBuilder createSQL) {
        if (createSQL.length() != 0) {
            createSQL.append(", ");
        }

        for (int i = 0; i < dynamic.getColumnCount(); i++) {
            DynamicMetadata column = dynamic.getColumnMetadata(i);
            if (i != 0) {
                createSQL.append(",");
            }
            createSQL.append(getLProtectedChar(column.getName())).append(column.getName()).append(getRProtectedChar(column.getName())).append(" ");
        }
        return createSQL.toString();
    }

    public void appendDBType(StringBuilder createSQL,String dataType) {
        createSQL.append(dataType);
    }

    public void appendDBTypeTail(StringBuilder createSQL,String dataType) {
        //do nothing
    }

    public boolean checkPrecision(Integer precision) {
        return precision.intValue() < 0;
    }

    public boolean ignoreLengthAndPrecision(String dataType,Integer length,Integer precision) {
        return false;
    }

    public boolean ignoreLength(Integer length) {
        return false;
    }

    protected String correctDBType(DynamicMetadata column) {
    	return column.getDbType();
    }

    private String getDBTypeFromDynamic(DynamicMetadata column) {
        if (column.isDbTypeDefinitive()){       //This mean right DB type was set before and it's final type
            return column.getDbType();          //We can immediatly return DBtype, and not to correct it
        }
		String dataType = getDefaultSelectedDbType(column.getType());
		String orignalDBType = correctDBType(column);
		java.util.List<String> outputDBTypelist = MetadataTalendType.getTalendToDBList(this.getDBMSId(), column.getType());
		if (outputDBTypelist != null && outputDBTypelist.size() > 0) {
			for (String dbType : outputDBTypelist) {
				if (orignalDBType.equalsIgnoreCase(dbType)) {
					dataType = dbType;
					break;
				}
			}
		}
		if (dataType == null || ("").equals(dataType)) {
			dataType = orignalDBType;
		}
		return dataType;
	}

	private String getDefaultSelectedDbType(String talendType) {
	    return MetadataTalendType.getDefaultSelectedDbType(this.getDBMSId(), talendType, -1, -1);
	}

    public String getCreateTableSQL(Dynamic dynamic) {

        StringBuilder createSQL = new StringBuilder();
        String ending = ",";

        for (int i = 0; i < dynamic.getColumnCount(); i++) {
            DynamicMetadata column = dynamic.getColumnMetadata(i);
            createSQL.append(getLProtectedChar(column.getName()) + column.getName() + getRProtectedChar(column.getName()) + " ");
            String dataType = this.getDBTypeFromDynamic(column);

            appendDBType(createSQL,dataType);

            boolean defaultLengthIgnored = ("true").equals(MetadataTalendType.getDefaultDBTypes(getDBMSId(), dataType,
                    MetadataTalendType.IGNORE_LEN));
            Integer length = column.getLength();
            if (length.intValue() <= 0) {
                String defaultLen = MetadataTalendType.getDefaultDBTypes(getDBMSId(), dataType,
                        MetadataTalendType.DEFAULT_LENGTH);
                if (defaultLen != null && !("").equals(defaultLen)) {
                    length = Integer.parseInt(defaultLen);
                }
            }
            boolean defaultPrecisionIgnored = ("true").equals(MetadataTalendType.getDefaultDBTypes(getDBMSId(), dataType,
                    MetadataTalendType.IGNORE_PRE));
            Integer precision = column.getPrecision();
            if (checkPrecision(precision)) {
                String strPrecision = MetadataTalendType.getDefaultDBTypes(getDBMSId(), dataType,
                        MetadataTalendType.DEFAULT_PRECISION);
                if (strPrecision != null && !("").equals(strPrecision)) {
                    precision = Integer.parseInt(strPrecision);
                }
            }
            String prefix = "";
            String suffix = "";
            String comma = "";
            if (!ignoreLengthAndPrecision(dataType,length,precision)) {

                if (!defaultLengthIgnored) {
                    if (!ignoreLength(length)) {
                        prefix = "(";
                        suffix = ") ";
                        createSQL.append(prefix + getLengthStringRepresentation(dataType, length));
                    }
                }
                if (!defaultPrecisionIgnored) {
                    prefix = (prefix.equals("") ? "(" : prefix);
                    suffix = (suffix.equals("") ? ") " : suffix);
                    if (defaultLengthIgnored) {
                        createSQL.append(prefix);
                        comma = "";
                    } else {
                        comma = ",";
                    }
                    createSQL.append(comma + precision);
                }
                if (!ignoreLength(length)) {
                    createSQL.append(suffix);
                }
                appendDBTypeTail(createSQL,dataType);
            }
            createSQL.append(setNullable(column.isNullable()));
            if (i >= dynamic.getColumnCount() - 1) {
                ending = "";
            }
            createSQL.append(ending);
        }

        return createSQL.toString();
    }

    protected String getLengthStringRepresentation(String dataType, Integer length) {
        return String.valueOf(length);
    }

    protected String setNullable(boolean nullable) {
        if (!nullable) {
            return " not null ";
        } else {
            return "";
        }
    }

}

class AccessManager extends DBManager {

    private String dbmsId;

    public AccessManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {

        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "[";
    }

    protected String getRProtectedChar() {
        return "]";
    }

}

class AS400Manager extends DBManager {

    private String dbmsId;

    public AS400Manager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "";
    }

    protected String getRProtectedChar() {
        return "";
    }

}

class DB2Manager extends DBManager {

    private String dbmsId;

    public DB2Manager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "\"";
    }

    protected String getRProtectedChar() {
        return "\"";
    }
}

class FirebirdManager extends DBManager {

    private String dbmsId;

    public FirebirdManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return " ";
    }

    protected String getRProtectedChar() {
        return " ";
    }

}

class HSQLDBManager extends DBManager {

    private String dbmsId;

    String[] hsqldbKeyWords = {};

    public HSQLDBManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "";
    }

    protected String getRProtectedChar() {
        return "";
    }

    protected boolean isHSQLDBKeyword(String keyword) {
        for (int i = 0; i < hsqldbKeyWords.length; i++) {
            if (hsqldbKeyWords[i].equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }

    protected String getLProtectedChar(String keyword) {
        if (isHSQLDBKeyword(keyword)) {
            return "\"";
        }
        return getLProtectedChar();
    }

    protected String getRProtectedChar(String keyword) {
        if (isHSQLDBKeyword(keyword)) {
            return "\"";
        }
        return getRProtectedChar();
    }

}

class InformixManager extends DBManager {

    private String dbmsId;

    public InformixManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    /*
     * (non-Javadoc)
     *
     * @see routines.system.dbmanager.DBManager#getDBMSId()
     */
    @Override
    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "";
    }

    protected String getRProtectedChar() {
        return "";
    }

}

class IngresManager extends DBManager {

    private String dbmsId;

    public IngresManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "\"";
    }

    protected String getRProtectedChar() {
        return "\"";
    }

}

class VectorWiseManager extends IngresManager {

    private String dbmsId;

    public VectorWiseManager(String dbmsId) {
        super(dbmsId);
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return dbmsId;
    }

}

class JavaDBManager extends DBManager {

    private String dbmsId;

    public JavaDBManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "\"";
    }

    protected String getRProtectedChar() {
        return "\"";
    }

}

class JDBCManager extends DBManager {

    private String dbmsId;

    public JDBCManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "";
    }

    protected String getRProtectedChar() {
        return "";
    }

}

class MaxDBManager extends DBManager {

    private String dbmsId;

    public MaxDBManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "";
    }

    protected String getRProtectedChar() {
        return "";
    }

}

class MSSQLManager extends DBManager {

    private static final String MAX_STRING = "MAX";

    private String dbmsId;

    public MSSQLManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "[";
    }

    protected String getRProtectedChar() {
        return "]";
    }

    protected String getLengthStringRepresentation(String dataType, Integer length) {
        if (("VARCHAR".equalsIgnoreCase(dataType) && length > 8000) || ("NVARCHAR".equalsIgnoreCase(dataType) && length > 4000)
                || ("VARBINARY".equalsIgnoreCase(dataType) && length > 8000)) {
            return MAX_STRING;
        }
        return String.valueOf(length);
    }

}

class MysqlManager extends DBManager {

    private String dbmsId;

    public MysqlManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "`";
    }

    protected String getRProtectedChar() {
        return "`";
    }

    public void appendDBType(StringBuilder createSQL,String dataType) {
        if (dataType.endsWith("UNSIGNED")) {
            createSQL.append(dataType.substring(0, dataType.indexOf("UNSIGNED")));
        } else {
            createSQL.append(dataType);
        }
    }

    public void appendDBTypeTail(StringBuilder createSQL,String dataType) {
        if (dataType.endsWith("UNSIGNED")) {
            createSQL.append("UNSIGNED");
        }
    }

    public boolean ignoreLengthAndPrecision(String dataType,Integer length,Integer precision) {
        return (("DECIMAL".equalsIgnoreCase(dataType)) || ("NUMERIC".equalsIgnoreCase(dataType)))
        && (length == null || length <= 0) && (precision == null || precision <= 0);
    }

    public String correctDBType(DynamicMetadata column) {
    	if((java.sql.Types.LONGVARCHAR == column.getDbTypeId()) && "VARCHAR".equals(column.getDbType())) {
    		int length = column.getLength();
    		if(length<256) {
    			return "TINYTEXT";
    		} else if(length<65536) {
    			return "TEXT";
    		} else if(length<16777216) {
    			return "MEDIUMTEXT";
    		} else {
    			return "LONGTEXT";
    		}
    	}
    	return column.getDbType();
    }

    public boolean ignoreLength(Integer length) {
        return length < 0;
    }

}

class NetezzaManager extends DBManager {

    private String dbmsId;

    public NetezzaManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "";
    }

    protected String getRProtectedChar() {
        return "";
    }

}

class ODBCManager extends DBManager {

    private String dbmsId;

    public ODBCManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "";
    }

    protected String getRProtectedChar() {
        return "";
    }

}

class OracleManager extends DBManager {

    private String[] oracleKeyWords = { "ACCESS", "AUDIT", "COMPRESS", "DESC", "ADD", "CONNECT", "DISTINCT", "ALL", "BY",
            "CREATE", "DROP", "ALTER", "CHAR", "CURRENT", "ELSE", "AND", "CHECK", "DATE", "EXCLUSIVE", "ANY", "CLUSTER",
            "DECIMAL", " EXISTS", "AS", "COLUMN", "DEFAULT", "FILE", "ASC", "COMMENT", "DELETE", "FLOAT", "FOR", "LONG",
            "PCTFREE", "SUCCESSFUL", "FROM", "MAXEXTENTS", "PRIOR", "SYNONYM", "GRANT", "MINUS", "PRIVILEGES", "SYSDATE",
            "GROUP", "MODE", "PUBLIC", "TABLE", "HAVING", "MODIFY", "RAW", "THEN", "IDENTIFIED", "NETWORK", "RENAME", "TO",
            "IMMEDIATE", "NOAUDIT", "RESOURCE", "TRIGGER", "IN", "NOCOMPRESS", "REVOKE", "UID", "INCREMENT", "NOT", "ROW",
            "UNION", "INDEX", "NOWAIT", "ROWID", "UNIQUE", "INITIAL", "NULL", "ROWNUM", "UPDATE", "INSERT", "NUMBER", "ROWS",
            "USER", "INTEGER", "OF", "SELECT", "VALIDATE", "INTERSECT", "OFFLINE", "SESSION", "VALUES", "INTO", "ON", "SET",
            "VARCHAR", "IS", "ONLINE", "SHARE", "VARCHAR2", "LEVEL", "OPTION", "SIZE", "VIEW", "LIKE", "OR", "SMALLINT",
            "WHENEVER", "LOCK", "ORDER", "START", "WHERE", "WITH" };

    private String dbmsId;

    public OracleManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getLProtectedChar() {
        return "";
    }

    protected String getRProtectedChar() {
        return "";
    }

    protected String getDBMSId() {
        return dbmsId;
    }

    protected boolean isOracleKeyword(String keyword) {
        for (int i = 0; i < oracleKeyWords.length; i++) {
            if (oracleKeyWords[i].equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }

    protected boolean contaionsSpaces(String columnName) {
        if (columnName != null) {
            // bug0016837 when use Additional column the coulmn name like: " + "columnNmae" + "
            if (columnName.startsWith("\" + ") && columnName.endsWith(" + \"")) {
                return false;
            }

            if (columnName.contains(" ")) {
                return true;
            }
        }
        return false;
    }

    protected String getLProtectedChar(String keyword) {
        if (isOracleKeyword(keyword) || contaionsSpaces(keyword)) {
            return "\"";
        }
        return getLProtectedChar();
    }

    protected String getRProtectedChar(String keyword) {
        if (isOracleKeyword(keyword) || contaionsSpaces(keyword)) {
            return "\"";
        }
        return getRProtectedChar();
    }

    public boolean ignoreLengthAndPrecision(String dataType,Integer length,Integer precision) {
        if (("NUMBER".equalsIgnoreCase(dataType))
                && (length == null || length <= 0) && (precision == null || precision < 0)) {
            return true;
        } else if((("CHAR".equalsIgnoreCase(dataType)) || ("NCHAR".equalsIgnoreCase(dataType)))
                && (length == null || length <= 0) && (precision == null || precision <= 0)) {
            return true;
        }

        return false;
    }

}

class PostgreManager extends DBManager {

    private String dbmsId;

    public PostgreManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return dbmsId;
    }

    protected String getLProtectedChar() {
        return "\"";
    }

    protected String getRProtectedChar() {
        return "\"";
    }

    public boolean ignoreLength(Integer length) {
        return length < 0;
    }

}

class PostgrePlusManager extends DBManager {

    private String dbmsId;

    public PostgrePlusManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "\"";
    }

    protected String getRProtectedChar() {
        return "\"";
    }

    public boolean ignoreLength(Integer length) {
        return length < 0;
    }

}

class RedshiftManager extends DBManager {

    private String dbmsId;

    public RedshiftManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "\"";
    }

    protected String getRProtectedChar() {
        return "\"";
    }

    public boolean ignoreLength(Integer length) {
        return length < 0;
    }

}

class SQLiteManager extends DBManager {

    private String dbmsId;

    public SQLiteManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "\"";
    }

    protected String getRProtectedChar() {
        return "\"";
    }

}

class SybaseManager extends DBManager {

    private String dbmsId;

    private String[] sybaseKeyWords = { "ACCESS", "AUDIT", "COMPRESS", "DESC", "ADD", "CONNECT", "COUNT", "DISTINCT", "ALL",
            "BY", "CREATE", "DROP", "ALTER", "CHAR", "CURRENT", "ELSE", "AND", "CHECK", "DATE", "EXCLUSIVE", "ANY", "CLUSTER",
            "DECIMAL", " EXISTS", "AS", "COLUMN", "DEFAULT", "FILE", "ASC", "COMMENT", "DELETE", "FLOAT", "FOR", "LONG",
            "PCTFREE", "SUCCESSFUL", "FROM", "FALSE", "MAXEXTENTS", "PRIOR", "SYNONYM", "GRANT", "MINUS", "PRIVILEGES",
            "SYSDATE", "GROUP", "MODE", "PUBLIC", "TABLE", "HAVING", "MODIFY", "RAW", "THEN", "IDENTIFIED", "NETWORK", "RENAME",
            "TO", "IMMEDIATE", "NOAUDIT", "RESOURCE", "TRIGGER", "IN", "NOCOMPRESS", "REVOKE", "UID", "INCREMENT", "NOT", "ROW",
            "UNION", "INDEX", "NOWAIT", "ROWID", "UNIQUE", "INITIAL", "NULL", "ROWNUM", "UPDATE", "INSERT", "NUMBER", "ROWS",
            "USER", "INTEGER", "OF", "SELECT", "VALIDATE", "INTERSECT", "OFFLINE", "SESSION", "VALUES", "INTO", "ON", "SET",
            "VARCHAR", "IS", "ONLINE", "SHARE", "LEVEL", "OPTION", "SIZE", "VIEW", "LIKE", "OR", "SMALLINT", "WHENEVER", "LOCK",
            "ORDER", "START", "WHERE", "WITH"

    };

    public SybaseManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "";
    }

    protected String getRProtectedChar() {
        return "";
    }

    protected boolean isSybaseKeyword(String keyword) {
        for (int i = 0; i < sybaseKeyWords.length; i++) {
            if (sybaseKeyWords[i].equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }

    protected String getLProtectedChar(String keyword) {
        if (isSybaseKeyword(keyword)) {
            return "\"";
        }
        return getLProtectedChar();
    }

    protected String getRProtectedChar(String keyword) {
        if (isSybaseKeyword(keyword)) {
            return "\"";
        }
        return getRProtectedChar();
    }

}

class TeradataManager extends DBManager {

    private String dbmsId;

    public TeradataManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return dbmsId;
    }

    protected String getLProtectedChar() {
        return "\"";
    }

    protected String getRProtectedChar() {
        return "\"";
    }

}

class VerticaManager extends DBManager {

    private String dbmsId;

    public VerticaManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "";
    }

    protected String getRProtectedChar() {
        return "";
    }

}
class SAPHanaManager extends DBManager {

    private String dbmsId;
    private String[] sapHanaReservedWords = { "ALL", "ALTER", "AS", "BEFORE", "BEGIN", "BOTH", "CASE", "CHAR", "CONDITION",
            "CONNECT", "CROSS", "CUBE", "CURRENT_CONNECTION", "CURRENT_DATE", "CURRENT_SCHEMA", "CURRENT_TIME",
            "CURRENT_TIMESTAMP", "CURRENT_USER", "CURRENT_UTCDATE", "CURRENT_UTCTIME", "CURRENT_UTCTIMESTAMP", "CURRVAL",
            "CURSOR", "DECLARE", "DISTINCT", "ELSE", "ELSEIF", "ELSIF", "END", "EXCEPT", "EXCEPTION", "EXEC", "FOR", "FROM",
            "FULL", "GROUP", "HAVING", "IF", "IN", "INNER", "INOUT", "INTERSECT", "INTO", "IS", "JOIN", "LEADING", "LEFT",
            "LIMIT", "LOOP", "MINUS", "NATURAL", "NEXTVAL", "NULL", "ON", "ORDER", "OUT", "PRIOR", "RETURN", "RETURNS",
            "REVERSE", "RIGHT", "ROLLUP", "ROWID", "SELECT", "SET", "SQL", "START", "SYSDATE", "SYSTIME", "SYSTIMESTAMP",
            "SYSUUID", "TOP", "TRAILING", "UNION", "USING", "UTCDATE", "UTCTIME", "UTCTIMESTAMP", "VALUES", "WHEN", "WHERE",
            "WHILE", "WITH" };



    public SAPHanaManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        // TODO Auto-generated method stub
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "";
    }

    protected String getRProtectedChar() {
        return "";
    }

    protected boolean isSAPHanaKeyword(String keyword) {
        for (int i = 0; i < sapHanaReservedWords.length; i++) {
            if (sapHanaReservedWords[i].equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }

    protected String getLProtectedChar(String keyword) {
        if (isSAPHanaKeyword(keyword)) {
            return "\"";
        }
        return getLProtectedChar();
    }

    protected String getRProtectedChar(String keyword) {
        if (isSAPHanaKeyword(keyword)) {
            return "\"";
        }
        return getRProtectedChar();
    }

}

class ExasolManager extends DBManager {

    private String dbmsId;
    private String[] exasolReservedWords = {"ABSOLUTE","ACTION","ADD","AFTER","ALL","ALLOCATE","ALTER","AND","ANY","APPEND",
    		"ARE","ARRAY","AS","ASC","ASENSITIVE","ASSERTION","AT","ATTRIBUTE","AUTHID","AUTHORIZATION","BEFORE","BEGIN",
    		"BETWEEN","BIGINT","BINARY","BIT","BLOB","BLOCKED","BOOL","BOOLEAN","BOTH","BY","BYTE","CALL","CALLED","CARDINALITY",
    		"CASCADE","CASCADED","CASE","CASESPECIFIC","CAST","CATALOG","CHAIN","CHAR","CHARACTER","CHARACTERISTICS",
    		"CHARACTER_SET_CATALOG","CHARACTER_SET_NAME","CHARACTER_SET_SCHEMA","CHECK","CHECKED","CLOB","CLOSE","COALESCE",
    		"COLLATE","COLLATION","COLLATION_CATALOG","COLLATION_NAME","COLLATION_SCHEMA","COLUMN","COMMIT","CONDITION",
    		"CONNECTION","CONNECT_BY_ISCYCLE","CONNECT_BY_ISLEAF","CONNECT_BY_ROOT","CONSTANT","CONSTRAINT","CONSTRAINTS",
    		"CONSTRAINT_STATE_DEFAULT","CONSTRUCTOR","CONTAINS","CONTINUE","CONTROL","CONVERT","CORRESPONDING","CREATE","CS",
    		"CSV","CUBE","CURRENT","CURRENT_DATE","CURRENT_PATH","CURRENT_ROLE","CURRENT_SCHEMA","CURRENT_SESSION",
    		"CURRENT_STATEMENT","CURRENT_TIME","CURRENT_TIMESTAMP","CURRENT_USER","CURSOR","CYCLE","DATA","DATALINK","DATE",
    		"DATETIME_INTERVAL_CODE","DATETIME_INTERVAL_PRECISION","DAY","DBTIMEZONE","DEALLOCATE","DEC","DECIMAL","DECLARE",
    		"DEFAULT","DEFAULT_LIKE_ESCAPE_CHARACTER","DEFERRABLE","DEFERRED","DEFINED","DEFINER","DELETE","DEREF","DERIVED",
    		"DESC","DESCRIBE","DESCRIPTOR","DETERMINISTIC","DISABLE","DISABLED","DISCONNECT","DISPATCH","DISTINCT","DLURLCOMPLETE",
    		"DLURLPATH","DLURLPATHONLY","DLURLSCHEME","DLURLSERVER","DLVALUE","DO","DOMAIN","DOUBLE","DROP","DYNAMIC",
    		"DYNAMIC_FUNCTION","DYNAMIC_FUNCTION_CODE","EACH","ELSE","ELSEIF","ELSIF","ENABLE","ENABLED","END","END-EXEC",
    		"ENFORCE","EQUALS","ERRORS","ESCAPE","EXCEPT","EXCEPTION","EXEC","EXECUTE","EXISTS","EXIT","EXPORT","EXTERNAL",
    		"EXTRACT","FALSE","FBV","FETCH","FILE","FINAL","FIRST","FLOAT","FOLLOWING","FOR","FORALL","FORCE","FORMAT","FOUND",
    		"FREE","FROM","FS","FULL","FUNCTION","GENERAL","GENERATED","GEOMETRY","GET","GLOBAL","GO","GOTO","GRANT","GRANTED",
    		"GROUP","GROUPING","GROUP_CONCAT","HAVING","HIGH","HOLD","HOUR","IDENTITY","IF","IFNULL","IMMEDIATE","IMPLEMENTATION",
    		"IMPORT","IN","INDEX","INDICATOR","INNER","INOUT","INPUT","INSENSITIVE","INSERT","INSTANCE","INSTANTIABLE","INT",
    		"INTEGER","INTEGRITY","INTERSECT","INTERVAL","INTO","INVERSE","INVOKER","IS","ITERATE","JOIN","KEY_MEMBER","KEY_TYPE",
    		"LARGE","LAST","LATERAL","LDAP","LEADING","LEAVE","LEFT","LEVEL","LIKE","LIMIT","LOCAL","LOCALTIME","LOCALTIMESTAMP",
    		"LOCATOR","LOG","LONGVARCHAR","LOOP","LOW","MAP","MATCH","MATCHED","MERGE","METHOD","MINUS","MINUTE","MOD","MODIFIES",
    		"MODIFY","MODULE","MONTH","NAMES","NATIONAL","NATURAL","NCHAR","NCLOB","NEW","NEXT","NLS_DATE_FORMAT","NLS_DATE_LANGUAGE",
    		"NLS_FIRST_DAY_OF_WEEK","NLS_NUMERIC_CHARACTERS","NLS_TIMESTAMP_FORMAT","NO","NOCYCLE","NOLOGGING","NONE","NOT",
    		"NULL","NULLIF","NUMBER","NUMERIC","NVARCHAR","NVARCHAR2","OBJECT","OF","OFF","OLD","ON","ONLY","OPEN","OPTION",
    		"OPTIONS","OR","ORDER","ORDERING","ORDINALITY","OTHERS","OUT","OUTER","OUTPUT","OVER","OVERLAPS","OVERLAY",
    		"OVERRIDING","PAD","PARALLEL_ENABLE","PARAMETER","PARAMETER_SPECIFIC_CATALOG","PARAMETER_SPECIFIC_NAME",
    		"PARAMETER_SPECIFIC_SCHEMA","PARTIAL","PATH","PERMISSION","PLACING","PLUS","POSITION","PRECEDING","PREFERRING",
    		"PREPARE","PRESERVE","PRIOR","PRIVILEGES","PROCEDURE","PROFILE","RANDOM","RANGE","READ","READS","REAL","RECOVERY",
    		"RECURSIVE","REF","REFERENCES","REFERENCING","REGEXP_LIKE","RELATIVE","RELEASE","RENAME","REPEAT","REPLACE",
    		"RESTORE","RESTRICT","RESULT","RETURN","RETURNED_LENGTH","RETURNED_OCTET_LENGTH","RETURNS","REVOKE","RIGHT",
    		"ROLLBACK","ROLLUP","ROUTINE","ROW","ROWS","ROWTYPE","SAVEPOINT","SCHEMA","SCOPE","SCRIPT","SCROLL","SEARCH",
    		"SECOND","SECTION","SECURITY","SELECT","SELECTIVE","SELF","SENSITIVE","SEPARATOR","SEQUENCE","SESSION","SESSIONTIMEZONE",
    		"SESSION_USER","SET","SETS","SHORTINT","SIMILAR","SMALLINT","SOME","SOURCE","SPACE","SPECIFIC","SPECIFICTYPE","SQL",
    		"SQLEXCEPTION","SQLSTATE","SQLWARNING","SQL_BIGINT","SQL_BIT","SQL_CHAR","SQL_DATE","SQL_DECIMAL","SQL_DOUBLE",
    		"SQL_FLOAT","SQL_INTEGER","SQL_LONGVARCHAR","SQL_NUMERIC","SQL_PREPROCESSOR_SCRIPT","SQL_REAL","SQL_SMALLINT",
    		"SQL_TIMESTAMP","SQL_TINYINT","SQL_TYPE_DATE","SQL_TYPE_TIMESTAMP","SQL_VARCHAR","START","STATE","STATEMENT",
    		"STATIC","STRUCTURE","STYLE","SUBSTRING","SUBTYPE","SYSDATE","SYSTEM","SYSTEM_USER","SYSTIMESTAMP","TABLE",
    		"TEMPORARY","TEXT","THEN","TIME","TIMESTAMP","TIMEZONE_HOUR","TIMEZONE_MINUTE","TINYINT","TO","TRAILING",
    		"TRANSACTION","TRANSFORM","TRANSFORMS","TRANSLATION","TREAT","TRIGGER","TRIM","TRUE","TRUNCATE","UNDER","UNION",
    		"UNIQUE","UNKNOWN","UNLINK","UNNEST","UNTIL","UPDATE","USAGE","USER","USING","VALUE","VALUES","VARCHAR","VARCHAR2",
    		"VARRAY","VERIFY","VIEW","WHEN","WHENEVER","WHERE","WHILE","WINDOW","WITH","WITHIN","WITHOUT","WORK","YEAR","YES","ZONE"};

    public ExasolManager(String dbmsId) {
        super();
        this.dbmsId = dbmsId;
    }

    protected String getDBMSId() {
        return this.dbmsId;
    }

    protected String getLProtectedChar() {
        return "";
    }

    protected String getRProtectedChar() {
        return "";
    }

    protected boolean isexasolKeyword(String keyword) {
        for (int i = 0; i < exasolReservedWords.length; i++) {
            if (exasolReservedWords[i].equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }

    protected String getLProtectedChar(String keyword) {
        if (isexasolKeyword(keyword)) {
            return "\"";
        }
        return getLProtectedChar();
    }

    protected String getRProtectedChar(String keyword) {
        if (isexasolKeyword(keyword)) {
            return "\"";
        }
        return getRProtectedChar();
    }

}
