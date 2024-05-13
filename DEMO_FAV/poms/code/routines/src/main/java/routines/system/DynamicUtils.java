/**
 * 
 */
package routines.system;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.nio.charset.Charset;

/**
 * @author Administrator
 * 
 */

public class DynamicUtils {

    /**
     * @author parham parvizi
     * @description: write all the values in the dynamic schema to a delimited file writer. fields are separated by the
     * fieldSeparator.
     * @param column the dynamic column to write
     * @param out a java.io.Writer
     * @param fieldSeparator field delimiter
     * @throws java.io.IOException
     */
    public static void writeValuesToDelimitedFile(Dynamic column, java.io.Writer out, String fieldSeparator)
            throws java.io.IOException {
        for (int i = 0; i < column.getColumnCount(); i++) {
            if (column.getColumnValue(i) != null) {
                DynamicMetadata metadata = column.getColumnMetadata(i);
                if ("id_Date".equals(metadata.getType())
                        && !(DBMSConstants.MSSQL.getDBmsId().equalsIgnoreCase(column.getDbmsId()) && StringUtils.containsIgnoreCase(metadata.getDbType(), "timestamp"))) {
                    out.write(formatDate(column.getColumnValue(i), metadata.getFormat()));
                } else {
                    out.write(String.valueOf(column.getColumnValue(i)));
                }
            }
            if (i != (column.getColumnCount() - 1)) {
                out.write(fieldSeparator);
            }
        }
        out.flush();
    }
    
    public static void writeValuesToStringBuilder(Dynamic column, Appendable sb, String fieldSeparator,Character thousandsSeparator, Character decimalSeparator) 
            throws java.io.IOException {
        DynamicUtils.writeValuesToStringBuilder(column,  sb, fieldSeparator, thousandsSeparator, decimalSeparator, Charset.defaultCharset());
    }

    public static void writeValuesToStringBuilder(Dynamic column, Appendable sb, String fieldSeparator,Character thousandsSeparator, Character decimalSeparator, Charset charset)
            throws java.io.IOException {
        
        for (int i = 0; i < column.getColumnCount(); i++) {
            final Object colValue = column.getColumnValue(i);
            if (colValue != null) {
                final DynamicMetadata metadata = column.getColumnMetadata(i);
                final String valueToAdd = DynamicUtils.buildStringValue(column, colValue, metadata, thousandsSeparator, decimalSeparator, charset);
                sb.append(valueToAdd);
            }
            if (i != (column.getColumnCount() - 1)) {
                sb.append(fieldSeparator);
            }
        }
    }
    
    private static String buildStringValue(Dynamic column, final Object colValue, final DynamicMetadata metadata, Character thousandsSeparator, Character decimalSeparator, Charset charset) {
        final boolean advancedSeparator = thousandsSeparator != null || decimalSeparator != null;
        final String IdType = metadata.getType();
             
        String value = "";   
        if ("id_Date".equals(IdType) && !(DBMSConstants.MSSQL.getDBmsId().equalsIgnoreCase(column.getDbmsId())
                && StringUtils.containsIgnoreCase(metadata.getDbType(), "timestamp"))) {
            value = formatDate(colValue, metadata.getFormat());
        } else if ("id_BigDecimal".equals(IdType)) {
            
            if (colValue instanceof BigDecimal) {
                value = ((BigDecimal) colValue).toPlainString();
            } else {
                value = String.valueOf(colValue);
            }

            if (advancedSeparator) {
                value = FormatterUtils.format_Number(value, thousandsSeparator, decimalSeparator);
            } 
           
        } else if (advancedSeparator
                && Arrays.asList("id_Integer", "id_Long", "id_Short", "id_Double", "id_Float", "id_Byte")
                        .contains(IdType)) {
            value = FormatterUtils.format_Number(
                    new BigDecimal(String.valueOf(colValue)).toPlainString(),
                    thousandsSeparator, decimalSeparator);
        } else if ("id_byte[]".equals(IdType) && colValue instanceof byte[]) {
            final byte[] contentValue = (byte[]) colValue;
            value = new String(contentValue, charset);
        } else {
            value = String.valueOf(colValue);
        }
        return value;
    }

    public static void writeValuesToStringBuilder(Dynamic column, Appendable sb, String fieldSeparator) throws IOException{
        writeValuesToStringBuilder(column,sb,fieldSeparator,null,null, Charset.defaultCharset());
    }
    
    public static void writeValuesToStringBuilder(Dynamic column, Appendable sb, String fieldSeparator, Charset charset) throws IOException{
        writeValuesToStringBuilder(column,sb,fieldSeparator,null,null, charset);
    }
    
    public static void writeValuesToStringArray(Dynamic column, String[] row, int offset) {
        writeValuesToStringArray(column, row, offset, Charset.defaultCharset());
    }

    public static void writeValuesToStringArray(Dynamic column, String[] row, int offset, Charset charset) {
        for (int i = 0; i < column.getColumnCount(); i++) {
            if (column.getColumnValue(i) != null) {
                DynamicMetadata metadata = column.getColumnMetadata(i);
                final String valueToAdd = DynamicUtils.buildStringValue(column, column.getColumnValue(i), metadata, null, null, charset);
                row[offset + i] = valueToAdd;
            } else {
                row[offset + i] = "";
            }
        }
    }
    
    public static void writeValuesToStringArrayEnhance(Dynamic column, String[] row, int offset,String str4Null) {
        writeValuesToStringArrayEnhance(column, row, offset, str4Null, Charset.defaultCharset());
    }

    public static void writeValuesToStringArrayEnhance(Dynamic column, String[] row, int offset,String str4Null, Charset charset) {
        for (int i = 0; i < column.getColumnCount(); i++) {
            if (column.getColumnValue(i) != null) {
                final DynamicMetadata metadata = column.getColumnMetadata(i);
                final String valueToAdd = DynamicUtils.buildStringValue(column, column.getColumnValue(i), metadata, null, null, charset);
                row[offset + i] = valueToAdd;
            } else {
                if(str4Null == null){
                    row[offset + i] = "";
                }else{
                    row[offset + i] = null;
                }
            }
        }
    }

    public static void writeHeaderToDelimitedFile(Dynamic column, java.io.Writer out, String fieldSeparator)
            throws java.io.IOException {
        for (int i = 0; i < column.getColumnCount(); i++) {
            if(column.getColumnMetadata(i).getName() != null) {
                out.write(column.getColumnMetadata(i).getName());
            }
            if (i != (column.getColumnCount() - 1)) {
                out.write(fieldSeparator);
            }
        }
        out.flush();
    }

    public static void readColumnsFromDelimitedFile(Dynamic column, org.talend.fileprocess.FileInputDelimited fid,
            int fixedColumnCount) throws Exception {
        int fieldCount = fid.getColumnsCountOfCurrentRow();
        for (int i = 0; i < column.getColumnCount(); i++) {
            if ((fixedColumnCount + i) < fieldCount) {
                column.addColumnValue(fid.get((fixedColumnCount + i)));
            } else {
                column.addColumnValue("");
            }
        }
    }

    public static void readColumnsFromDatabase(Dynamic column, java.sql.ResultSet rs, int fixedColumnCount, boolean trim) throws Exception {
        column.clearColumnValues();
        for (int i = 0; i < column.getColumnCount(); i++) {
            DynamicMetadata dcm = column.getColumnMetadata(i);

            if ("id_String".equals(dcm.getType())) {
                String value = rs.getString(fixedColumnCount + i + 1);
                column.addColumnValue((trim && (value!=null)) ? value.trim() : value);
            } else if ("id_Date".equals(dcm.getType())) {
                if (DBMSConstants.MSSQL.getDBmsId().equalsIgnoreCase(column.getDbmsId())
                        && StringUtils.containsIgnoreCase(dcm.getDbType(), "timestamp")) {
                    column.addColumnValue(rs.getString(fixedColumnCount + i + 1));
                } else if (DBMSConstants.NETEZZA.getDBmsId().equalsIgnoreCase(column.getDbmsId())
                        && "time".equalsIgnoreCase(dcm.getDbType())) {
                    column.addColumnValue(rs.getTime(fixedColumnCount + i + 1));
                } else {
                    column.addColumnValue(rs.getTimestamp(fixedColumnCount + i + 1));
                }
            } else if ("id_Integer".equals(dcm.getType()) || "id_Long".equals(dcm.getType()) || "id_Double".equals(dcm.getType())
                    || "id_Byte".equals(dcm.getType()) || "id_byte[]".equals(dcm.getType())) {
                if (rs.getObject(fixedColumnCount + i + 1) == null) {
                    column.addColumnValue(null);
                    continue;
                }
                if ("id_Integer".equals(dcm.getType())) {
                    column.addColumnValue(rs.getInt(fixedColumnCount + i + 1));
                } else if ("id_Long".equals(dcm.getType())) {
                    column.addColumnValue(rs.getLong(fixedColumnCount + i + 1));
                } else if ("id_Double".equals(dcm.getType())) {
                    column.addColumnValue(rs.getDouble(fixedColumnCount + i + 1));
                } else if ("id_Byte".equals(dcm.getType())) {
                    column.addColumnValue(rs.getByte(fixedColumnCount + i + 1));
                } else if ("id_byte[]".equals(dcm.getType())) {
                    column.addColumnValue(rs.getBytes(fixedColumnCount + i + 1));
                }
            }else  if ("id_Boolean".equals(dcm.getType())) {
                if (rs.getObject(fixedColumnCount + i + 1) == null) {
                    column.addColumnValue(null);
                }else{
                    column.addColumnValue(rs.getBoolean(fixedColumnCount + i + 1));
                }
            } else {
                column.addColumnValue(rs.getObject(fixedColumnCount + i + 1));
            }
        }
    }

    public static void readColumnsFromDatabase_Access(Dynamic column, java.sql.ResultSet rs, int fixedColumnCount, boolean trim)
            throws Exception {
        column.clearColumnValues();
        for (int i = 0; i < column.getColumnCount(); i++) {
            DynamicMetadata dcm = column.getColumnMetadata(i);
            if ("id_String".equals(dcm.getType())) {
                String value = rs.getString(fixedColumnCount + i + 1);
                column.addColumnValue((trim && (value!=null)) ? value.trim() : value);
            } else if ("id_Date".equals(dcm.getType())) {
                column.addColumnValue(rs.getTimestamp(fixedColumnCount + i + 1));
            } else if ("id_Byte".equals(dcm.getType()) || "id_Short".equals(dcm.getType()) || "id_Integer".equals(dcm.getType())
                    || "id_Long".equals(dcm.getType()) || "id_Float".equals(dcm.getType()) || "id_Double".equals(dcm.getType())) {
                Object obj = rs.getObject(fixedColumnCount + i + 1);
                if (obj == null) {
                    column.addColumnValue(null);
                    continue;
                }
                if ("id_Byte".equals(dcm.getType())) {
                    column.addColumnValue(Byte.parseByte(obj.toString()));
                } else if ("id_Short".equals(dcm.getType())) {
                    column.addColumnValue(Short.parseShort(obj.toString()));
                } else if ("id_Integer".equals(dcm.getType())) {
                    column.addColumnValue(Integer.parseInt(obj.toString()));
                } else if ("id_Long".equals(dcm.getType())) {
                    column.addColumnValue(Long.parseLong(obj.toString()));
                } else if ("id_Float".equals(dcm.getType())) {
                    column.addColumnValue(Float.parseFloat(obj.toString()));
                } else if ("id_Double".equals(dcm.getType())) {
                    column.addColumnValue(Double.parseDouble(obj.toString()));
                }
            } else {
                column.addColumnValue(rs.getObject(fixedColumnCount + i + 1));
            }
        }
    }

    public static void readColumnsFromDatabase_Mssql(Dynamic column, java.sql.ResultSet rs, int fixedColumnCount,
            java.util.List<String> list, boolean trim) throws Exception {
        column.clearColumnValues();
        for (int i = 0; i < column.getColumnCount(); i++) {
            DynamicMetadata dcm = column.getColumnMetadata(i);

            if ("id_String".equals(dcm.getType())) {
                if (DBMSConstants.MSSQL.getDBmsId().equalsIgnoreCase(column.getDbmsId())
                        && "NTEXT".equalsIgnoreCase(dcm.getDbType())) {
                    String value = list.get(0);
                    column.addColumnValue((trim && (value!=null)) ? value.trim() : value);
                    list.remove(0);
                } else {
                    String value = rs.getString(fixedColumnCount + i + 1);
                    column.addColumnValue((trim && (value!=null)) ? value.trim() : value);
                }
            } else if ("id_Date".equals(dcm.getType())) {
                if (DBMSConstants.MSSQL.getDBmsId().equalsIgnoreCase(column.getDbmsId())
                        && StringUtils.containsIgnoreCase(dcm.getDbType(), "timestamp")) {
                    column.addColumnValue(rs.getString(fixedColumnCount + i + 1));
                } else {
                    column.addColumnValue(rs.getTimestamp(fixedColumnCount + i + 1));
                }
            } else if ("id_Integer".equals(dcm.getType()) || "id_Long".equals(dcm.getType()) || "id_Double".equals(dcm.getType()) 
                || ("id_Byte".equals(dcm.getType()) && "BIT".equalsIgnoreCase(dcm.getDbType()))) {
                if (rs.getObject(fixedColumnCount + i + 1) == null) {
                    column.addColumnValue(null);
                    continue;
                }
                if ("id_Integer".equals(dcm.getType())) {
                    column.addColumnValue(rs.getInt(fixedColumnCount + i + 1));
                } else if ("id_Long".equals(dcm.getType())) {
                    column.addColumnValue(rs.getLong(fixedColumnCount + i + 1));
                } else if ("id_Double".equals(dcm.getType())) {
                    column.addColumnValue(rs.getDouble(fixedColumnCount + i + 1));
                } else {
                    column.addColumnValue(rs.getByte(fixedColumnCount + i + 1));
                }
            } else {
                column.addColumnValue(rs.getObject(fixedColumnCount + i + 1));
            }
        }
    }

    public static int writeColumnsToDatabse(Dynamic column, java.sql.PreparedStatement pstmt, int fixedColumnCount,
            String database) throws Exception {
        return writeColumnsToDatabse(column, pstmt, fixedColumnCount, database, false);
    }

    public static int writeColumnsToDatabse(Dynamic column, java.sql.PreparedStatement pstmt, int fixedColumnCount,
            String database, boolean isUpdate) throws Exception {
        int position = 0;
        for (DynamicMetadata dynamicMetadata : column.metadatas) {
            if (isUpdate && dynamicMetadata.isKey()) {
                continue;
            }
            position++;
            insertValueIntoPreparedStatement(dynamicMetadata.getType(), dynamicMetadata.getDbType(), pstmt, fixedColumnCount + position, database, column.getColumnValue(dynamicMetadata.getName()));
        }
        return position;
    }

    private static void insertValueIntoPreparedStatement(String type, String dbType, PreparedStatement pstmt, int index,
            String database, Object value) throws Exception {

        if (value == null) {
            if (DBMSConstants.SYBASE.getDBmsId().equalsIgnoreCase(database)) {
                pstmt.setNull(index, java.sql.Types.VARCHAR);
            } else if (DBMSConstants.MSSQL.getDBmsId().equalsIgnoreCase(database)
                    && "id_byte[]".equals(type)) {
                pstmt.setNull(index, java.sql.Types.BINARY);
            } else if (DBMSConstants.TERADATA.getDBmsId().equalsIgnoreCase(database)
                    || DBMSConstants.MSSQL.getDBmsId().equalsIgnoreCase(database)
                    || DBMSConstants.REDSHIFT.getDBmsId().equalsIgnoreCase(database)) {
                if("id_String".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.VARCHAR);
                } else if("id_Integer".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.INTEGER);
                } else if("id_Long".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.BIGINT);
                } else if("id_Short".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.SMALLINT);
                } else if("id_Byte".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.TINYINT);
                } else if("id_Date".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.TIMESTAMP);
                } else if ("id_Time".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.TIMESTAMP);
                } else if ("id_Timestamp".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.TIMESTAMP);
                } else if ("id_Float".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.FLOAT);
                } else if ("id_Double".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.DOUBLE);
                } else if ("id_BigDecimal".equals(type)) {
                    pstmt.setBigDecimal(index, null);
                } else if ("id_Boolean".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.BOOLEAN);
                } else if ("id_Character".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.CHAR);
                } else {
                    pstmt.setNull(index, java.sql.Types.NULL);
                }
            } else if (DBMSConstants.DB2.getDBmsId().equalsIgnoreCase(database)) {
                if ("id_String".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.VARCHAR);
                } else if ("id_Long".equals(type)
                        || "id_Integer".equals(type)
                        || "id_Short".equals(type)
                        || "id_Byte".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.INTEGER);
                } else if ("id_Date".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.DATE);
                } else if ("id_Float".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.FLOAT);
                } else if ("id_Double".equals(type)) {
                    pstmt.setNull(index, java.sql.Types.DOUBLE);
                } else if ("id_BigDecimal".equals(type)) {
                    pstmt.setBigDecimal(index, null);
                } else {
                    pstmt.setNull(index, java.sql.Types.NULL);
                }
            } else if (DBMSConstants.VERTICA.getDBmsId().equalsIgnoreCase(database)) {
            	if ("Uuid".equals(dbType)) {
            		pstmt.setObject(index, "00000000-0000-0000-0000-000000000000");
            	} else {
                    pstmt.setNull(index, java.sql.Types.NULL);
                }
        	} else {
                pstmt.setNull(index, java.sql.Types.NULL);
            }
        } else if ("id_Integer".equals(type)) {
            pstmt.setInt(index, Integer.valueOf(value.toString()));
        } else if ("id_String".equals(type)) {
        	if (DBMSConstants.POSTGRESQL.getDBmsId().equalsIgnoreCase(database)) {
        		pstmt.setObject(index, String.valueOf(value),java.sql.Types.OTHER);
        	}else {
        		pstmt.setString(index, String.valueOf(value));
        	}
        } else if ("id_Double".equals(type)) {
            pstmt.setDouble(index, Double.valueOf(value.toString()));
        } else if ("id_Float".equals(type)) {
            pstmt.setFloat(index, Float.valueOf(value.toString()));
        } else if ("id_Long".equals(type)) {
            pstmt.setLong(index, Long.valueOf(value.toString()));
        } else if ("id_BigDecimal".equals(type)) {
            pstmt.setBigDecimal(index, new java.math.BigDecimal(value.toString()));
        } else if ("id_Boolean".equals(type)) {
            pstmt.setBoolean(index, Boolean.valueOf(value.toString()));
        } else if ("id_Byte".equals(type)) {
            pstmt.setByte(index, Byte.valueOf(value.toString()));
        } else if ("id_Short".equals(type)) {
            pstmt.setShort(index, Short.valueOf(value.toString()));
        } else if ("id_Date".equals(type)
                || "id_Time".equals(type)
                || "id_Timestamp".equals(type)) {
            if (value instanceof java.sql.Timestamp) {
                pstmt.setTimestamp(index, (java.sql.Timestamp) value);
            } else {
                String formatValue = formatDate(value, "yyyy-MM-dd HH:mm:ss.SSS");
                pstmt.setTimestamp(index, java.sql.Timestamp.valueOf(formatValue));
            }
        } else if ("id_Blob".equals(type)) {
            pstmt.setBlob(index, (java.sql.Blob) value);
        } else if ("id_Clob".equals(type)) {
            pstmt.setClob(index, (java.sql.Clob) value);
        } else if ("id_Character".equals(type)) {
            if (DBMSConstants.MYSQL.getDBmsId().equalsIgnoreCase(database)) {
                String stringValue = ((Character) value) == '\0' ? "" : String.valueOf(value);
                pstmt.setString(index, stringValue);
            } else {
                try {
                    pstmt.setObject(index, value);
                } catch (java.sql.SQLException e) {
                    pstmt.setString(index, String.valueOf(value));
                }
            }
        } else {
            pstmt.setObject(index, value);
        }
    }

    public static void insertKeyInWhereClause(Dynamic column, java.sql.PreparedStatement pstmt, int index,
            String database) throws Exception {
        insertKeyInWhereClause(column, pstmt, index, database, false);
    }

    public static void insertKeyInWhereClause(Dynamic column, java.sql.PreparedStatement pstmt, int index,
            String database, boolean supportNull) throws Exception {
        for (int i = 0; i < column.getColumnCount(); i++) {
            if (column.getColumnMetadata(i).isKey()) {
                if (supportNull && column.getColumnMetadata(i).isNullable()) {
                    pstmt.setBoolean(index++, column.getColumnValue(i) == null);
                }
                insertValueIntoPreparedStatement(column.getColumnMetadata(i).getType(), column.getColumnMetadata(i).getDbType(), pstmt, index++, database, column.getColumnValue(i));
            }
        }
    }

    public static StringBuffer debugDynamicSql(StringBuffer query, Dynamic column, int fixedColumnCount, String[] sqlsplit)
            throws Exception {
        if (query.length() == 0) {
            query.append(sqlsplit[0]);
        }
        for (int i = 0; i < column.getColumnCount(); i++) {
            Object value = column.getColumnValue(i);
            String talendType = column.getColumnMetadata(i).getType();
            if (value == null) {
                query.append("null");
            } else if ("id_Character".equals(talendType)) {
                query.append("'" + String.valueOf(value) + "'");
            } else if ("id_String".equals(talendType)) {
                query.append("'" + value.toString() + "'");
            } else if ("id_Date".equals(talendType) || "id_Time".equals(talendType) || "id_Timestamp".equals(talendType)) {
                String formatValue = formatDate(value, column.getColumnMetadata(i).getFormat());
                query.append("'" + formatValue + "'");
            } else {
                query.append(String.valueOf(value));
            }
            if (i < column.getColumnCount()) {
                query.append(sqlsplit[fixedColumnCount + i]);
            }

        }
        return query;
    }

    public static String getCreateTableSQL(Dynamic column, String dbmsId) {
        DBManager manager = DBManagerFactory.getDBManager(dbmsId);
        String str = manager.getCreateTableSQL(column);
        return str;
    }

    public static String getInsertIntoStmtColumnsList(Dynamic column, String database) {
        return getInsertIntoStmtColumnsList(column, database, new StringBuilder());
    }

    public static String getInsertIntoStmtColumnsList(Dynamic column, String database, StringBuilder createSQL) {
        DBManager manager = DBManagerFactory.getDBManager(database);
        String str = manager.getInsertTableSQL(column, createSQL);
        return str;
    }

    public static String getInsertIntoStmtValuesList(Dynamic column) {
        return getInsertIntoStmtValuesList(column, null);
    }

    public static String getInsertIntoStmtValuesList(Dynamic column, String database) {
        return getInsertIntoStmtValuesList(column, database, new StringBuilder());
    }

    public static String getInsertIntoStmtValuesList(Dynamic column, String database, StringBuilder list) {
        String sign = "?";
        if (list.length() != 0) {
            list.append(", ");
        }
        for (int i = 0; i < column.getColumnCount(); i++) {
            if(DBMSConstants.TERADATA.getDBmsId().equalsIgnoreCase(database)) {
                if("id_Date".equals(column.getColumnMetadata(i).getType())) {
                    int dbTypeId = column.getColumnMetadata(i).getDbTypeId();//it only work for database source
                    if(dbTypeId == java.sql.Types.DATE) {
                        sign = "cast(cast(? as timestamp) as DATE)";
                    } else if(dbTypeId == java.sql.Types.TIME) {
                        sign = "cast(cast(? as timestamp) as TIME)";
                    }
                }
            }
            if (list.length() != 0 && i != 0) {
                list.append(",");
            }
            list.append(sign);
        }
        return list.toString();
    }

    public static String getUpdateSet(Dynamic column, String dbmsId) {
        return getUpdateSet(column, dbmsId, new StringBuilder());
    }

    public static String getUpdateSet(Dynamic column, String dbmsId, StringBuilder updateSetStmt) {
        DBManager manager = DBManagerFactory.getDBManager(dbmsId);
        String updateSqlSet = manager.getUpdateSetSQL(column, updateSetStmt);
        return updateSqlSet;
    }

    public static String getUpdateWhere(Dynamic column, String dbmsId, StringBuilder updateWhereStmt, boolean supportNull) {
        DBManager manager = DBManagerFactory.getDBManager(dbmsId);
        String updateWhereSql = manager.getUpdateWhereSQL(column, updateWhereStmt, supportNull);
        return updateWhereSql;
    }

    public static String formatDate(Object value, String pattern) {
        if(value instanceof Long){
            return FormatterUtils.format_Date(new java.util.Date((Long)value), pattern);
        }else{
            return FormatterUtils.format_Date((java.util.Date) value, pattern);
        }
    }
}
