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

/**
 * when the external components and new developed components needs to support Dynamic Schema<br/>
 * make sure that register the dbmsId and the mapping file path here.<br/>
 * And if it is a db input component, make sure it has the mapping field.
 *
 * @author talend
 */
public enum DBMSConstants {

    AS400 {

        String getDBmsId() {
            return "as400_id";
        }

        String getDBmsMappingFile() {
            return "mapping_as400.xml";
        }
    },

    ACCESS {

        String getDBmsId() {
            return "access_id";
        }

        String getDBmsMappingFile() {
            return "mapping_access.xml";
        }
    },

    DB2 {

        String getDBmsId() {
            return "ibmdb2_id";
        }

        String getDBmsMappingFile() {
            return "mapping_ibmdb2.xml";
        }
    },

    FIREBIRD {

        String getDBmsId() {
            return "firebird_id";
        }

        String getDBmsMappingFile() {
            return "mapping_firebird.xml";
        }
    },

    HSQLDB {

        String getDBmsId() {
            return "hsqldb_id";
        }

        String getDBmsMappingFile() {
            return "mapping_hsqldb.xml";
        }
    },

    INFORMIX {

        String getDBmsId() {
            return "informix_id";
        }

        String getDBmsMappingFile() {
            return "mapping_informix.xml";
        }
    },

    INGRES {

        String getDBmsId() {
            return "ingres_id";
        }

        String getDBmsMappingFile() {
            return "mapping_ingres.xml";
        }
    },

    VECTORWISE {

        String getDBmsId() {
            return "vectorwise_id";
        }

        String getDBmsMappingFile() {
            return "mapping_vectorwise.xml";
        }
    },
    
    JAVADB {

        String getDBmsId() {
            return "javadb_id";
        }

        String getDBmsMappingFile() {
            return "mapping_javadb.xml";
        }
    },

    MAXDB {

        String getDBmsId() {
            return "maxdb_id";
        }

        String getDBmsMappingFile() {
            return "mapping_maxdb.xml";
        }
    },

    MSSQL {

        String getDBmsId() {
            return "id_MSSQL";
        }

        String getDBmsMappingFile() {
            return "mapping_mssql.xml";
        }
    },

    MYSQL {

        String getDBmsId() {
            return "mysql_id";
        }

        String getDBmsMappingFile() {
            return "mapping_mysql.xml";
        }
    },

    NETEZZA {

        String getDBmsId() {
            return "netezza_id";
        }

        String getDBmsMappingFile() {
            return "mapping_netezza.xml";
        }
    },

    ORACLE {

        String getDBmsId() {
            return "oracle_id";
        }

        String getDBmsMappingFile() {
            return "mapping_oracle.xml";
        }
    },

    POSTGRESQL {

        String getDBmsId() {
            return "postgres_id";
        }

        String getDBmsMappingFile() {
            return "mapping_postgres.xml";
        }
    },

    POSTGREPLUS {

        String getDBmsId() {
            return "postgresplus_id";
        }

        String getDBmsMappingFile() {
            return "mapping_postgresplus.xml";
        }
    },

    SQLITE {

        String getDBmsId() {
            return "sqlite_id";
        }

        String getDBmsMappingFile() {
            return "mapping_sqlite.xml";
        }
    },

    SYBASE {

        String getDBmsId() {
            return "sybase_id";
        }

        String getDBmsMappingFile() {
            return "mapping_sybase.xml";
        }
    },


    SAPHANA {

        String getDBmsId() {
            return "saphana_id";
        }

        String getDBmsMappingFile() {
            return "mapping_saphana.xml";
        }
    },

    SAP {

        String getDBmsId() {
            return "sap_id";
        }

        String getDBmsMappingFile() {
            return "mapping_sap.xml";
        }
    },

    TERADATA {

        String getDBmsId() {
            return "teradata_id";
        }

        String getDBmsMappingFile() {
            return "mapping_teradata.xml";
        }
    },

    VERTICA {

        String getDBmsId() {
            return "vertica_id";
        }

        String getDBmsMappingFile() {
            return "mapping_vertica.xml";
        }
    },

    H2 {

        String getDBmsId() {
            return "h2_id";
        }

        String getDBmsMappingFile() {
            return "mapping_h2.xml";
        }
    },

    REDSHIFT {

        String getDBmsId() {
            return "redshift_id";
        }

        String getDBmsMappingFile() {
            return "mapping_redshift.xml";
        }
    },

    ODBC {

        String getDBmsId() {
            return "MSODBC";
        }

        String getDBmsMappingFile() {
            return "mapping_msodbc.xml";
        }
    },

    EXASOL {

        String getDBmsId() {
            return "exasol_id";
        }

        String getDBmsMappingFile() {
            return "mapping_exasol.xml";
        }
    },
    
    BIG_QUERY {
        
        String getDBmsId() {
            return "bigquery_id";
        }

        String getDBmsMappingFile() {
            return "mapping_bigquery.xml";
        }
        
    };

    abstract String getDBmsId();

    abstract String getDBmsMappingFile();
}
