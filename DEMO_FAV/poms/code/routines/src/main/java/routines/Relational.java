// ============================================================================
//
// Copyright (c) 2006-2014, Talend Inc.
//
// This source code has been automatically generated by_Talend Cloud Data Fabric
// CodeGenerator version 8.0.1.20240118_0425-patch
// You can find more information about Talend products at www.talend.com.
// You may distribute this code under the terms of the GNU LGPL license
// http://www.gnu.org/licenses/lgpl.html).
//
// ============================================================================
package routines;

public class Relational {

    /**
     * Indicates when a variable is the null value.
     *
     * {talendTypes} boolean | Boolean
     *
     * {Category} Relational
     *
     * {param} Object(null)
     *
     * {example} ISNULL(null)
     *
     *
     */
    public static boolean ISNULL(Object variable) {
        return variable == null;
    }

    /**
     * Returns the complement of the logical value of an expression.
     *
     * {talendTypes} boolean | Boolean
     *
     * {Category} Relational
     *
     * {param} boolean(true)
     *
     * {example} NOT(false)
     */
    public static boolean NOT(boolean expression) {
        return !expression;
    }
    
    /**
     * Indicates when a variable is the null value.
     * 
     * @param value :the object need to judge.
     * 
     * @return 1 if value == null, 0 if not.
     * 
     * {Category} Relational
     * 
     * {param} Object(null)
     * 
     * {example} ISNULL(null)
     */
	public static int isNull(Object value) {
		if (value == null) {
			return 1;
		}
		return 0;

	}
}
