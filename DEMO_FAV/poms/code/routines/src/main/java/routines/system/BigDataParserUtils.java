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

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Reimplementation of ParserUtils. Trying to use org.apache.commons.lang.StringUtils as much a possible and trying to
 * map any numric type to any numeric type, without a "toString" conversion
 *
 */
public class BigDataParserUtils extends ParserUtils {

    public static final boolean defaultValueBoolean = false;

    public static final int defaultValueInt = 0;

    public static final byte defaultValueByte = 0;

    public static final char defaultValueChar = ' ';

    public static final double defaultValueDouble = 0d;

    public static final float defaultValueFloat = 0f;

    public static final long defaultValueLong = 0l;

    public static final short defaultValueShort = 0;

    /**
     * StringUtils from apache commons-lang3 3.3.2
     *
     * We use this implementation in order to avoid using the common lang package and be compatible with any hadoop
     * distribution.
     *
     * Checks if a CharSequence is whitespace, empty ("") or null.
     *
     * @param cs the CharSequence to check, may be null
     * @returntrue if the CharSequence is null, empty or whitespace
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {

            return true;
        }

        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;

            }
        }
        return true;
    }

    /**
     * the source should be a string wrapped in chars[ ] which stands for it is a collection
     *
     * @return List<String>
     */

    public static List<String> parseTo_List(final String strSrc, String fieldSep) {
        if (isBlank(strSrc)) {
            return null;
        }
        List<String> list = new ArrayList<String>();

        // the source string is wrap in [] which means it is a collection
        if ((fieldSep == null || "".equals(fieldSep)) || !(strSrc.startsWith("[") && strSrc.endsWith("]"))) {
            list.add(strSrc);
            return list;
        }
        String strTemp = strSrc.substring(1, strSrc.length() - 1); // remove the [ ]
        for (String str : strTemp.split(fieldSep, -1)) {
            list.add(str);
        }
        return list;
    }

    /**
     * This method returns its List argument.
     *
     * @param list the list to parse into itself.
     * @return the list itself
     */

    public static <T> List<T> parseTo_List(List<T> list) {
        return list;
    }

    /*======Parse anything to Character or char======*/

    public static Character parseTo_Character(String s) {
        return isBlank(s) ? null : s.charAt(0);
    }

    public static char parseTo_char(String s) {
        return isBlank(s) ? defaultValueChar : s.charAt(0);
    }

    public static Character parseTo_Character(Character input) {
        return input;
    }

    public static char parseTo_char(Character input) {
        return input == null ? defaultValueChar : input;
    }

    /*======Parse anything to Boolean or boolean======*/

    public static Boolean parseTo_Boolean(String s) {
        return isBlank(s) ? null : "1".equals(s) ? true : Boolean.parseBoolean(s);
    }

    public static boolean parseTo_boolean(String s) {
        return isBlank(s) ? defaultValueBoolean : parseTo_Boolean(s);
    }

    public static boolean parseTo_boolean(Object input) {
        return input == null ? defaultValueBoolean : parseTo_Boolean(input.toString());
    }
    public static Boolean parseTo_Boolean(Boolean b) {
        return b;
    }

    public static boolean parseTo_boolean(Boolean b) {
        return b == null ? defaultValueBoolean : b;
    }



    public static Object parseTo_Object(Object input) {
        return input;
    }

    /*======Parse anything to String======*/

    public static String parseTo_String(java.nio.ByteBuffer input) {
        return input == null ? null : new String(input.array());
    }

    public static String parseTo_String(Object input) {
        return input == null ? null : String.valueOf(input);
    }

    public static String parseTo_String(java.util.Date input, String pattern) {
        return FormatterUtils.format_DateInUTC(input, pattern);
    }

    public static String parseTo_String(byte input) {
        return Byte.toString(input);
    }

    public static String parseTo_String(char input) {
        return String.valueOf(input);
    }

    public static String parseTo_String(double input) {
        return Double.isNaN(input) ? null : String.valueOf(input);
    }

    public static String parseTo_String(float input) {
        return Float.isNaN(input) ? null : String.valueOf(input);
    }

    public static String parseTo_String(int input) {
        return String.valueOf(input);
    }

    public static String parseTo_String(long input) {
        return String.valueOf(input);
    }

    public static String parseTo_String(short input) {
        return Short.toString(input);
    }

    /*======Parse anything to routines.system.Document======*/

    public static routines.system.Document parseTo_Document(String s, boolean ignoreDTD, String encoding)
            throws org.dom4j.DocumentException {
        if (isBlank(s)) {
            return null;
        }
        routines.system.Document theDoc = new routines.system.Document();
        org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();

        if (ignoreDTD) {
            reader.setEntityResolver(new EntityResolver() {

                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    return new org.xml.sax.InputSource(new java.io.ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>"
                            .getBytes()));
                }
            });
        }

        org.dom4j.Document document = reader.read(new java.io.StringReader(s));
        if (encoding != null && !("".equals(encoding))) {
            document.setXMLEncoding(encoding);
        }
        theDoc.setDocument(document);
        return theDoc;
    }

    /*======Parse anything to Date======*/

    public static java.util.Date parseTo_Date(String s, String pattern) {
        if (isBlank(s)) {
            return null;
        }
        String s2 = s.trim();
        String pattern2 = pattern;
        if (isBlank(pattern2)) {
            pattern2 = Constant.dateDefaultPattern;
        }
        java.util.Date date = null;
        if (pattern2.equals("yyyy-MM-dd'T'HH:mm:ss'000Z'")) {
            if (!s2.endsWith("000Z")) {
                throw new RuntimeException("Unparseable date: \"" + s2 + "\""); //$NON-NLS-1$ //$NON-NLS-2$
            }
            pattern2 = "yyyy-MM-dd'T'HH:mm:ss";
            s2 = s.substring(0, s.lastIndexOf("000Z"));
        }
        DateFormat format = FastDateParser.getInstance(pattern2);
        ParsePosition pp = new ParsePosition(0);
        pp.setIndex(0);

        date = format.parse(s2, pp);
        if (pp.getIndex() != s2.length() || date == null) {
            throw new RuntimeException("Unparseable date: \"" + s2 + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return date;
    }

    public static java.util.Date parseTo_Date(Object o, String pattern) {
        return parseTo_Date(parseTo_String(o), pattern);
    }

    public static java.util.Date parseTo_Date(String s, String pattern, boolean lenient) {
        if (isBlank(s)) {
            return null;
        }
        String s2 = s.trim();
        String pattern2 = pattern;
        if (isBlank(pattern2)) {
            pattern2 = Constant.dateDefaultPattern;
        }
        java.util.Date date = null;
        if (pattern2.equals("yyyy-MM-dd'T'HH:mm:ss'000Z'")) {
            if (!s2.endsWith("000Z")) {
                throw new RuntimeException("Unparseable date: \"" + s2 + "\""); //$NON-NLS-1$ //$NON-NLS-2$
            }
            pattern2 = "yyyy-MM-dd'T'HH:mm:ss";
            s2 = s2.substring(0, s.lastIndexOf("000Z"));
        }
        DateFormat format = FastDateParser.getInstance(pattern2, lenient);
        ParsePosition pp = new ParsePosition(0);
        pp.setIndex(0);

        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        date = format.parse(s2, pp);
        if (pp.getIndex() != s2.length() || date == null) {
            throw new RuntimeException("Unparseable date: \"" + s2 + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return date;
    }

    public static java.util.Date parseTo_Date(java.util.Date input) {
        return input;
    }

    /*
     * in order to transform the string "1.234.567,89" to number 1234567.89
     */
    public static String parseTo_Number(String s, Character thousandsSeparator, Character decimalSeparator) {
        if (isBlank(s)) {
            return null;
        }
        String result = s;
        if (thousandsSeparator != null) {
            result = routines.system.StringUtils.deleteChar(s, thousandsSeparator);
        }
        if (decimalSeparator != null) {
            result = result.replace(decimalSeparator, '.');
        }
        return result;
    }

    /*======Parse anything to Integer======*/

    public static Integer parseTo_Integer(String s) {
        return isBlank(s) ? null : Integer.valueOf(s);
    }
    public static Integer parseTo_Integer(Object input) {
        return input == null ? null : parseTo_Integer(input.toString());
    }

    public static Integer parseTo_Integer(String s, boolean isDecode) {
        if (isBlank(s)) {
            return null;
        }
        return isDecode ? parseTo_Integer(s) : Integer.valueOf(s);
    }
    public static Integer parseTo_Integer(Number input) {
        return input == null ? null : input.intValue();
    }

    public static Integer parseTo_Integer(Double input) {
        return input == null || Double.isNaN(input) ? null : input.intValue();
    }

    public static Integer parseTo_Integer(Float input) {
        return input == null || Float.isNaN(input) ? null : input.intValue();
    }

    /*======Parse anything to int======*/

    public static int parseTo_int(String s) {
        return isBlank(s) ? defaultValueInt : Integer.parseInt(s);
    }

    public static int parseTo_int(Number input) {
        return input == null ? defaultValueInt : input.intValue();
    }

    public static int parseTo_int(Double input) {
        return input == null || Double.isNaN(input) ? defaultValueInt : input.intValue();
    }

    public static int parseTo_int(Float input) {
        return input == null || Float.isNaN(input) ? defaultValueInt : input.intValue();
    }

    /*======Parse anything to Byte======*/

    public static Byte parseTo_Byte(String s) {
        return isBlank(s) ? null : Byte.decode(s);
    }

    public static Byte parseTo_Byte(String s, boolean isDecode) {
        if (isBlank(s)) {
            return null;
        }
        return isDecode ? parseTo_Byte(s) : Byte.parseByte(s);
    }

    public static Byte parseTo_Byte(Number input) {
        return input == null ? null : input.byteValue();
    }

    public static Byte parseTo_Byte(Double input) {
        return input == null || Double.isNaN(input) ? null : input.byteValue();
    }

    public static Byte parseTo_Byte(Float input) {
        return input == null || (Float.isNaN(input)) ? null : input.byteValue();
    }

    public static Byte parseTo_Byte(Boolean input) {
        return input == null ? null : (byte)(input ? 1 : 0);
    }

    /*======Parse anything to byte======*/

    public static byte parseTo_byte(String s) {
        return isBlank(s) ? defaultValueByte : Byte.decode(s).byteValue();
    }

    public static byte parseTo_byte(Number input) {
        return input == null ? defaultValueByte : input.byteValue();
    }

    public static byte parseTo_byte(Double input) {
        return input == null || Double.isNaN(input) ? defaultValueByte : input.byteValue();
    }

    public static byte parseTo_byte(Float input) {
        return input == null || Float.isNaN(input) ? defaultValueByte : input.byteValue();
    }

    public static byte parseTo_byte(Boolean input) {
        return input == null ? defaultValueByte : (byte)(input ? 1 : 0);
    }

    public static byte[] parseTo_bytes(ByteBuffer b) {
        return b.array();
    }

    /*======Parse anything to Double======*/

    public static Double parseTo_Double(String s) {
        return isBlank(s) ? null : Double.parseDouble(s);
    }

    public static Double parseTo_Double(Object input) {
        return input == null ? null : parseTo_Double(input.toString());
    }

    public static Double parseTo_Double(Number input) {
        return input == null ? null : input.doubleValue();
    }

    public static Double parseTo_Double(Float input) {
        return input == null || Float.isNaN(input) ? null : input.doubleValue();
    }

    /*======Parse anything to double======*/

    public static double parseTo_double(String s) {
        return isBlank(s) ? defaultValueDouble : parseTo_Double(s);
    }

    public static double parseTo_double(Number input) {
        return  input == null ? defaultValueDouble : input.doubleValue();
    }

    public static double parseTo_double(Float input) {
        return input == null || Float.isNaN(input) ? defaultValueDouble : input.doubleValue();
    }

    /*======Parse anything to Float======*/

    public static Float parseTo_Float(String s) {
        return isBlank(s) ? null : Float.parseFloat(s);
    }

    public static Float parseTo_Float(Object input) {
        return input == null ? null : parseTo_Float(input.toString());
    }

    public static Float parseTo_Float(Number input) {
        return input == null ? null : input.floatValue();
    }

    public static Float parseTo_Float(Double input) {
        return input == null || Double.isNaN(input) ? null : input.floatValue();
    }

    public static Float parseTo_Float(Float input) {
        return input == null || Float.isNaN(input) ? null : input.floatValue();
    }

    /*======Parse anything to float======*/

    public static float parseTo_float(String s) {
        return isBlank(s) ? defaultValueFloat : Float.parseFloat(s);
    }
    public static float parseTo_float(Number input) {
        return input == null ? defaultValueFloat : input.floatValue();
    }

    public static float parseTo_float(Double input) {
        return input == null || Double.isNaN(input) ? defaultValueFloat : input.floatValue();
    }

    /*======Parse anything to BigDecimal======*/

    public static BigDecimal parseTo_BigDecimal(String s) {
        if (isBlank(s)) {
            return null;
        }
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException nfe) {
            if (nfe.getMessage() == null) {
                throw new NumberFormatException("Incorrect input \"" + s + "\" for BigDecimal.");
            } else {
                throw nfe;
            }
        }
    }

    public static BigDecimal parseTo_BigDecimal(Number input) {
        return  input == null ? null : new BigDecimal(input.toString());
    }

    public static BigDecimal parseTo_BigDecimal(Double input) {
        return  input == null || Double.isNaN(input) ? null : new BigDecimal(Double.toString(input));
    }

    public static BigDecimal parseTo_BigDecimal(Float input) {
        return input == null || Float.isNaN(input) ? null : new BigDecimal(Float.toString(input));
    }

    /*======Parse anything to Long======*/


    public static Long parseTo_Long(String s) {
        return isBlank(s) ? null : Long.decode(s);
    }

    public static Long parseTo_Long(Object input) {
        return input == null ? null : parseTo_Long(input.toString());
    }

    public static Long parseTo_Long(String s, boolean isDecode) {
        if (isBlank(s)) {
            return null;
        }
        return isDecode ? parseTo_Long(s) : Long.parseLong(s);
    }
    public static Long parseTo_Long(Number input) {
        return input == null ? null : input.longValue();
    }

    public static Long parseTo_Long(Double input) {
        return input == null || Double.isNaN(input) ? null : input.longValue();
    }

    public static Long parseTo_Long(Float input) {
        return input == null || Float.isNaN(input) ? null : input.longValue();
    }

    /*======Parse anything to long======*/

    public static long parseTo_long(String s) {
        return isBlank(s) ? defaultValueLong : Long.decode(s);
    }

    public static long parseTo_long(Object input) {
        return input == null ? defaultValueLong : parseTo_long(input.toString());
    }
    public static long parseTo_long(Number input) {
        return input == null ? defaultValueLong : input.longValue();
    }

    public static long parseTo_long(Double input) {
        return input == null || Double.isNaN(input) ? defaultValueLong : input.longValue();
    }

    public static long parseTo_long(Float input) {
        return input == null || Float.isNaN(input) ? defaultValueLong : input.longValue();
    }

    /*======Parse anything to Short======*/


    public static Short parseTo_Short(String s) {
        return isBlank(s) ? null : Short.decode(s);
    }

    public static Short parseTo_Short(Object input) {
        return input == null ? null : parseTo_Short(input.toString());
    }

    public static Short parseTo_Short(String s, boolean isDecode) {
        if (isBlank(s)) {
            return null;
        }
        return isDecode ? parseTo_Short(s) : Short.parseShort(s);
    }

    public static Short parseTo_Short(Number input) {
        return input == null ? null : input.shortValue();
    }

    public static Short parseTo_Short(Double input) {
        return input == null || Double.isNaN(input) ? null : input.shortValue();
    }

    public static Short parseTo_Short(Float input) {
        return input == null || Float.isNaN(input) ? null : input.shortValue();
    }

    /*======Parse anything to short======*/

    public static short parseTo_short(String s) {
        return isBlank(s) ? defaultValueShort : Short.decode(s);
    }
    public static short parseTo_short(Number input) {
        return input == null ? defaultValueShort : input.shortValue();
    }
    public static short parseTo_short(Double input) {
        return input == null || Double.isNaN(input) ? defaultValueShort : input.shortValue();
    }

    public static short parseTo_short(Float input) {
        return input == null || Float.isNaN(input) ? defaultValueShort : input.shortValue();
    }

    /*======Parse anything to Timestamp======*/
    public static Timestamp parseTo_Timestamp(Date d) {
        return new Timestamp(d.getTime());
    }

    /*======toDatasetCompliantType facade methods======*/
    /**
     * Convert Date to Timestamp
     */
    public static Timestamp toDatasetCompliantType(Date d) {
        return d == null ? null : parseTo_Timestamp(d);
    }

    public static java.sql.Date toDatasetCompliantDate(Date d) {
        return d == null ? null : new java.sql.Date(d.getTime());
    }

    /**
     * Convert Character to String
     */
    public static String toDatasetCompliantType(Character c) {
        return parseTo_String(c);
    }

    /**
     * Convert ByteBuffer to byte[]
     */
    public static byte[] toDatasetCompliantType(ByteBuffer b) {
        return parseTo_bytes(b);
    }

    public static boolean isAFullURI(String folder) {
        return folder == null ? false : folder.contains("//");
    }
}
