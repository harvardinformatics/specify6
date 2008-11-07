/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.util;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;


/**
 * Helper for methods for convert to and from various different formats to Decimal Degrees.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jan 10, 2007
 *
 */
public class LatLonConverter
{
    protected final static int DDDDDD_LEN = 7;
    protected final static int DDMMMM_LEN = 5;
    protected final static int DDMMSS_LEN = 3;
    
    protected static int[] DECIMAL_SIZES = {7, 5, 3};
    
    public enum LATLON          {Latitude, Longitude}
    public enum FORMAT          {DDDDDD, DDMMMM, DDMMSS}
    public enum DEGREES_FORMAT  {None, Symbol, String}
    public enum DIRECTION       {None, NorthSouth, EastWest}
    
    private static LatLonConverter latLonConverter = new LatLonConverter();
    private static boolean         useDB           = false;
    
    public static DecimalFormat    decFormatter    = new DecimalFormat("#0.0000000#");
    protected static StringBuffer  zeroes          = new StringBuffer(64);
    
    
    protected static BigDecimal    minusOne        = new BigDecimal("-1.0");
    protected static DecimalFormat decFormatter2   = new DecimalFormat("#0");
    protected static BigDecimal    one             = new BigDecimal("1.0");
    protected static BigDecimal    sixty           = new BigDecimal("60.0");
    
    public static String[] NORTH_SOUTH;
    public static String[] EAST_WEST;
    
    public static String[] northSouth  = null;
    public static String[] eastWest    = null;

    
    static 
    {
        NORTH_SOUTH = new  String[] {"N", "S"};
        EAST_WEST   = new  String[] {"E", "W"};
        northSouth = new String[] {getResourceString(NORTH_SOUTH[0]), getResourceString(NORTH_SOUTH[1])};
        eastWest   = new String[] {getResourceString(EAST_WEST[0]), getResourceString(EAST_WEST[1])};
        
        for (int i=0;i<8;i++)
        {
            zeroes.append("00000000");
        }
    }
    
    /**
     * @param str
     * @return
     */
    public static Part[] parseLatLonStr(final String str)
    {
        String seps = "\u00b0" + "'\" ";
        if (StringUtils.isNotEmpty(str))
        {
            ArrayList<Part> parts = new ArrayList<Part>(10);
            String[] tokens = StringUtils.split(str, seps);
            for (String token : tokens)
            {
                int inx = token.indexOf(".");
                int len = -1;
                if (inx > -1)
                {
                    len = token.length() - inx - 1;
                }
                parts.add(latLonConverter.new Part(token, len));
            }
            return parts.toArray(new Part[parts.size()]);
        }
        return new Part[] {};
    }
    
    /**
     * @param formatInt
     * @return
     */
    public static FORMAT convertIntToFORMAT(final int formatInt)
    {
        switch (formatInt)
        {
            case 0 : return FORMAT.DDDDDD;
            case 1 : return FORMAT.DDMMMM;
            case 2 : return FORMAT.DDMMSS;
            default : return FORMAT.DDDDDD;
        }
    }
    
    /**
     * @param bd
     * @param llStr
     * @param type
     * @return
     */
    public static String getFormattedLatLon(final BigDecimal bd, final String llStr, final FORMAT type)
    {
        if (StringUtils.isEmpty(llStr))
        {
            if (bd == null)
            {
                return null;
            }
            
            switch (type)
            {
                case DDDDDD :
                    return convertToSignedDDDDDD(bd, DDDDDD_LEN);
                    
                case DDMMMM :
                    return convertToSignedDDDDDD(bd, DDMMMM_LEN);
                    
                case DDMMSS :
                    return convertToSignedDDDDDD(bd, DDMMSS_LEN);
            }
            
        } else
        {
            return llStr;
        }
        return null;
    }
    
    /**
     * @param str
     * @param defaultFormat
     * @return
     */
    public static FORMAT getFormat(final String str, final FORMAT defaultFormat)
    {
        String seps = "\u00b0" + "'\" ";
        if (StringUtils.isNotEmpty(str))
        {
            String[] tokens = StringUtils.split(str, seps);
            switch (tokens.length)
            {
                case 0:
                    break;
                    
                case 1:
                    return FORMAT.DDDDDD;
                    
                case 2:
                    return FORMAT.DDMMMM;
                    
                case 3:
                    return FORMAT.DDMMSS;
                    
                default:
                    break;
                
            }
        }
        return defaultFormat;
    }
    
    /**
     * @param str
     * @param fromFmt
     * @param toFmt
     * @param latOrLon
     * @return
     */
    public static String convert(final String str, 
                                 final FORMAT fromFmt, 
                                 final FORMAT toFmt, 
                                 final LATLON latOrLon)
    {
        if (StringUtils.isNotEmpty(str))
        {
            BigDecimal bd = null;
            switch (fromFmt)
            {
                case DDDDDD:
                    /*switch (toFmt)
                    {
                        case DDMMMM :
                            bd = convertDDDDTo(str)
                            
                    }*/
                    bd = convertDDDDToDDDD(str);
                    break;
    
                case DDMMMM:
                    bd = convertDDMMMMToDDDD(str);
                    break;
    
                case DDMMSS:
                    bd = convertDDMMSSToDDDD(str);
                    break;
            }
            
            String outStr = format(bd, latOrLon, toFmt, DEGREES_FORMAT.Symbol, DECIMAL_SIZES[toFmt.ordinal()]);
            
            /*switch (toFmt)
            {
                case DDDDDD:
                    outStr = format(bd, latOrLon, FORMAT.DDDDDD, DEGREES_FORMAT.Symbol
                            final FORMAT         format,
                            final DEGREES_FORMAT degreesFMT,
                            final int            decimalLen
                    break;
    
                case DDMMMM:
                    outStr = convertToDDMMMM(bd, 5);
                    break;
    
                case DDMMSS:
                    outStr = cconvertTo
                    break;
            }*/
            return outStr;
        }
        return null;
    }
    
    public static String formatValueFromStrings(final String... strArgs)
    {
        String degrees = "\u00b0";
        
        StringBuilder sb = new StringBuilder();
        
        int cnt = 0;
        for (String strArg : strArgs)
        {
            String str = StringUtils.deleteWhitespace(strArg);
            if (StringUtils.isEmpty(str))
            {
                str = "0";
            }
            
            if (cnt > 0) sb.append(' ');
            
            sb.append(str);
            if (cnt == 0)
            {
                sb.append(degrees);
                
            } else if (strArgs.length == 2)
            {
                sb.append("'");
                
            } else if (strArgs.length == 3)
            {
                sb.append(cnt == 1 ? "'" : "\"");
            }
            cnt++;
        }
        return sb.toString();
    }

    /**
     * Converts BigDecimal to Degrees, Minutes and Decimal Seconds.
     * @param bc the DigDecimal to be converted.
     * @return a 3 piece string
     */
    public static String convertToDDMMSS(final BigDecimal bc,
                                         final int        decimalLen)
    {
        return convertToDDMMSS(bc, DEGREES_FORMAT.None, DIRECTION.None, decimalLen);
    }
    
    /**
     * @param bd
     * @param decimalLen
     * @return
     */
    public static String convertToSignedDDMMSS(final BigDecimal bd,
                                               final int        decimalLen)
    {
        String sign = "";
        if (bd.compareTo(bd.abs()) < 0)
        {
            sign = "-";
        }
        
        String convertedAbs = convertToDDMMSS(bd, decimalLen);
        return sign + convertedAbs;
    }
    
    /**
     * Calculates how many decimal places there is in the string.
     * @param latLonStr the string to be checked
     * @return the number of decimals places
     */
    public static int getDecimalLength(final String latLonStr)
    {
        int decimalFmtLen = 0;
        if (StringUtils.isNotEmpty(latLonStr))
        {
            int decIndex = latLonStr.lastIndexOf('.');
            if (decIndex > -1 && latLonStr.length() > decIndex)
            {
                decimalFmtLen = latLonStr.length() - decIndex;
            }
        }
        return decimalFmtLen;
    }
    
    /**
     * Converts BigDecimal to Degrees, Minutes and Decimal Seconds.
     * @param bc the DigDecimal to be converted.
     * @return a 3 piece string
     */
    public static String convertToDDMMSS(final BigDecimal     bc, 
                                         final DEGREES_FORMAT degreesFMT,
                                         final DIRECTION      direction,
                                         final int            decimalLen)
    {
        
        if (bc.doubleValue() == 0.0)
        {
            return "0." + zeroes.substring(0, decimalLen);
        }
        
        if (useDB)
        {
            BigDecimal remainder = bc.remainder(one);
          
            BigDecimal num = bc.subtract(remainder);
            
            BigDecimal minutes         = new BigDecimal(remainder.multiply(sixty).abs().intValue());
            BigDecimal secondsFraction = remainder.abs().multiply(sixty).subtract(minutes);                  
            BigDecimal seconds         = secondsFraction.multiply(sixty);
            
            //System.out.println("["+decFormatter2.format(num)+"]["+minutes+"]["+seconds+"]");
            
            return decFormatter2.format(num) + " " + decFormatter2.format(minutes) + " " + decFormatter.format(seconds);
            
        }
        //else
        
        double num       = Math.abs(bc.doubleValue());
        int    whole     = (int)Math.floor(num);
        double remainder = num - whole;
        
        double minutes      = remainder * 60.0;
        int    minutesWhole = (int)Math.floor(minutes);
        double secondsFraction = minutes - minutesWhole;
        double seconds = secondsFraction * 60.0;
        
        StringBuilder sb = new StringBuilder();
        if (degreesFMT == DEGREES_FORMAT.Symbol)
        {
            sb.append("\u00B0");
        }
        
        if (minutesWhole == 60)
        {
            whole += 1;
            minutesWhole = 0;
        }
        
        // round to 2 decimal places precision
        seconds = Math.round(seconds * 1000) / 1000.0;
        
        int secondsWhole = (int)Math.floor(seconds);
        if (secondsWhole == 60)
        {
            minutesWhole += 1;
            seconds = 0.0;
        }

        sb.append(whole);
        sb.append(' ');
        sb.append(minutesWhole);
        sb.append(' ');
        
        sb.append(StringUtils.stripEnd(decFormatter.format(seconds), "0"));
        
        if (degreesFMT == DEGREES_FORMAT.String)
        {
            int inx = bc.doubleValue() < 0.0 ? 1 : 0;
            sb.append(' ');
            sb.append(direction == DIRECTION.NorthSouth ? northSouth[inx] : eastWest[inx]);
        }
        //return whole + (DEGREES_FORMAT.None ? "\u00B0" : "") + " " + minutesWhole + " " + StringUtils.strip(String.format("%12.10f", new Object[] {seconds}), "0");
        return sb.toString();
    }
    
    /**
     * Converts BigDecimal to Degrees and Decimal Minutes.
     * @param bc the DigDecimal to be converted.
     * @return a 2 piece string
     */
    public static String convertToDDMMMM(final BigDecimal bc,
                                         final int        decimalLen)
    {
        return convertToDDMMMM(bc, DEGREES_FORMAT.None, DIRECTION.None, decimalLen);
    }
    
    /**
     * @param dd
     * @param decimalLen
     * @return
     */
    public static String convertToSignedDDMMMM(final BigDecimal dd,
                                               final int        decimalLen)
    {
        String sign = "";
        if (dd.compareTo(dd.abs()) < 0)
        {
            sign = "-";
        }
        
        String convertedAbs = convertToDDMMMM(dd, decimalLen);
        return sign + convertedAbs;
    }
    
    /**
     * Converts BigDecimal to Degrees and Decimal Minutes.
     * @param bc the DigDecimal to be converted.
     * @return a 2 piece string
     */
    public static String convertToDDMMMM(final BigDecimal     bc, 
                                         final DEGREES_FORMAT degreesFMT,
                                         final DIRECTION      direction,
                                         final int            decimalLen)
    {
        if (bc.doubleValue() == 0.0)
        {
            return "0.0";
        }
        
        if (useDB)
        {
            BigDecimal remainder = bc.remainder(one);
          
            BigDecimal num = bc.subtract(remainder);
            
            BigDecimal minutes = remainder.multiply(sixty).abs();
            
            //System.out.println("["+decFormatter2.format(num)+"]["+minutes+"]");
            return decFormatter2.format(num) + " " + decFormatter2.format(minutes);
            
        }
        //else
        
        double num       = Math.abs(bc.doubleValue());
        int    whole     = (int)Math.floor(num);
        double remainder = num - whole;
        
        double minutes = remainder * 60.0;
        //System.out.println("["+whole+"]["+String.format("%10.10f", new Object[] {minutes})+"]");
        
        StringBuilder sb = new StringBuilder();
        if (degreesFMT == DEGREES_FORMAT.Symbol)
        {
            sb.append("\u00B0");
        }
        sb.append(whole);
        sb.append(' ');
        
        // round to four decimal places of precision
        minutes = Math.round(minutes*10000) / 10000;
        
        sb.append(String.format("%"+decimalLen+"."+decimalLen+"f", minutes));
        
        if (degreesFMT == DEGREES_FORMAT.String)
        {
            int inx = bc.doubleValue() < 0.0 ? 1 : 0;
            sb.append(' ');
            sb.append(direction == DIRECTION.NorthSouth ? northSouth[inx] : eastWest[inx]);
        }
        //return whole + (degreesFMT == DEGREES_FORMAT.Symbol ? "\u00B0" : "") + " " + StringUtils.strip(String.format("%10.10f", new Object[] {minutes}), "0");
        return sb.toString();
        
    }
        
    /**
     * Converts BigDecimal to Decimal Degrees.
     * @param bc the DigDecimal to be converted.
     * @return a 1 piece string
     */
    public static String convertToDDDDDD(final BigDecimal bc,
                                         final int        decimalLen)
    {
        return convertToDDDDDD(bc, DEGREES_FORMAT.None, DIRECTION.None, decimalLen);
    }
    
    /**
     * @param dd
     * @param decimalLen
     * @return
     */
    public static String convertToSignedDDDDDD(final BigDecimal dd,
                                               final int        decimalLen)
    {
        String sign = "";
        if (dd.compareTo(dd.abs()) < 0)
        {
            sign = "-";
        }
        
        String convertedAbs = convertToDDDDDD(dd, decimalLen);
        return sign + convertedAbs;
    }
    
    /**
     * Converts BigDecimal to Decimal Degrees.
     * @param bc the DigDecimal to be converted.
     * @param degreesFMT indicates whether to include the degrees symbol
     * @return a 1 piece string
     */
    public static String convertToDDDDDD(final BigDecimal     bc, 
                                         final DEGREES_FORMAT degreesFMT,
                                         final DIRECTION      direction,
                                         final int            decimalLen)
    {
        if (bc == null || bc.doubleValue() == 0.0)
        {
            return "0.0";
        }
        
        StringBuilder sb = new StringBuilder();
        
        //sb.append(format(bc.abs()));
        sb.append(String.format("%"+decimalLen+"."+decimalLen+"f", bc.abs()));
        
        if (degreesFMT == DEGREES_FORMAT.Symbol)
        {
            sb.append("\u00B0");
            
        } else if (degreesFMT == DEGREES_FORMAT.String)
        {
            int inx = bc.doubleValue() < 0.0 ? 1 : 0;
            sb.append(' ');
            sb.append(direction == DIRECTION.NorthSouth ? northSouth[inx] : eastWest[inx]);
        }
        //return format(bc.abs()) + (degreesFMT == DEGREES_FORMAT.Symbol ? "\u00B0" : "");
        return sb.toString();

        
    }
    
    /**
     * Given a single character string  should the direction be negative.
     * @param direction the string
     * @return true negative, false positive
     */
    protected static boolean isNegative(final String direction)
    {
        return direction.equals("S") || direction.equals("W");
    }
    
    
    /**
     * Converts Decmal Degrees to BigDecimal.
     * @param bc the DigDecimal to be converted.
     * @return a BigDecimal
     */
    public static BigDecimal convertDDDDToDDDD(final String str)
    {
        String withoutDegSign = StringUtils.chomp(str, "°");
        return new BigDecimal(withoutDegSign);
    }
    
    /**
     * Converts Decimal Degrees to BigDecimal.
     * @param bc the DigDecimal to be converted.
     * @param direction the direction
     * @return a BigDecimal
     */
    public static BigDecimal convertDDDDToDDDD(final String str, final String direction)
    {
        BigDecimal bd = new BigDecimal(str);
        if (isNegative(direction))
        {
            return bd.multiply(minusOne);
        }
        return bd;
    }
    
    /**
     * Converts Degrees, Minutes and Decimal Seconds to BigDecimal.
     * @param bc the DigDecimal to be converted.
     * @return a BigDecimal
     */
    public static BigDecimal convertDDMMSSToDDDD(final String str)
    {
        String[] parts = StringUtils.split(str," d°'\"");
        double p0 =  Double.parseDouble(parts[0]);
        boolean neg = false;
        if (p0 < 0)
        {
            p0 = p0*-1;
            neg = true;
        }
        double p1 =  Double.parseDouble(parts[1]);
        double p2 =  Double.parseDouble(parts[2]);

        BigDecimal val = new BigDecimal(p0 + ((p1 + (p2 / 60.0)) / 60.0));
        if (neg)
        {
            val = val.multiply(minusOne);
        }
        return val;
    }
    
    /**
     * Converts Degrees, Minutes and Decimal Seconds to BigDecimal.
     * @param bc the DigDecimal to be converted.
     * @param direction the direction
     * @return a BigDecimal
     */
    public static BigDecimal convertDDMMSSToDDDD(final String str, final String direction)
    {
        BigDecimal bd = convertDDMMSSToDDDD(str);

        if (isNegative(direction))
        {
            return bd.multiply(minusOne);
        }
        return bd;
    }
    
    /**
     * Converts Degrees decimal Minutes to BigDecimal.
     * @param bc the DigDecimal to be converted.
     * @return a BigDecimal
     */
    public static BigDecimal convertDDMMMMToDDDD(final String str)
    {
        String[] parts = StringUtils.split(str," d°'\"");
        
        
        double p0 =  Double.parseDouble(parts[0]);
        boolean neg = false;
        if (p0 < 0)
        {
            p0 = p0*-1;
            neg = true;
        }
        double p1 =  Double.parseDouble(parts[1]);

        BigDecimal val = new BigDecimal(p0 + (p1 / 60.0));

        if (neg)
        {
            val = val.multiply(minusOne);
        }
        return val;
    }
    
    /**
     * Converts Degrees decimal Minutes to BigDecimal.
     * @param bc the DigDecimal to be converted.
     * @param direction the direction
     * @return a BigDecimal
     */
    public static BigDecimal convertDDMMMMToDDDD(final String str, final String direction)
    {
        BigDecimal bd = convertDDMMMMToDDDD(str);
        if (isNegative(direction))
        {
            return bd.multiply(minusOne);
        }
        return bd;
    }
    
    /**
     * Strinps any zeros at end of string, but will append a zero if string would end in a decimal.
     * @param str the string to be converted
     * @return the new string
     */
    public static String stripZeroes(final String str)
    {
        if (str.indexOf('.') == -1)
        {
            return str;
            
        }
        // else
        String newStr = StringUtils.stripEnd(str, "0");
        if (newStr.endsWith("."))
        {
            return newStr + "0";
        }
        return newStr;
    }
    
    /**
     * Returns a formatted string using the class level formatter and then strips any extra zeroes.
     * @param bd the BigDecimal to be formatted.
     * @return the formatted string
     */
    public static String format(final BigDecimal bd)
    {
        String formatted = decFormatter.format(bd);
        return stripZeroes(formatted);
    }

    
    /**
     * Converts a Lat/Lon BigDecimal to a String
     * @param value the value
     * @param latOrLon whether it is a latitude or longitude
     * @param format the format to use
     * @param degreesFMT indicates whether to use a symbol or append single character text representation of the direction ('N', 'S', 'E', 'W")
     * @return string of the value
     */
    public static String format(final BigDecimal     value, 
                                final LATLON         latOrLon, 
                                final FORMAT         format,
                                final DEGREES_FORMAT degreesFMT,
                                final int            decimalLen)
    {
        DIRECTION dir = latOrLon == LATLON.Latitude ? DIRECTION.NorthSouth : DIRECTION.EastWest;
        switch (format)
        {
            case DDDDDD:
                return convertToDDDDDD(value, degreesFMT, dir, decimalLen);
                
            case DDMMMM:
                return convertToDDMMMM(value, degreesFMT, dir, decimalLen);
                
            case DDMMSS: 
                return convertToDDMMSS(value, degreesFMT, dir, decimalLen);
        }
        return "";
    }
    
    /**
     * @param str
     * @return
     */
    public static BigDecimal convertDirectionalDDMMSSToDDDD(final String str)
    {
        String[] parts = StringUtils.split(str," d°'\"");
        double p0 =  Double.parseDouble(parts[0]);
        double p1 =  Double.parseDouble(parts[1]);
        double p2 =  Double.parseDouble(parts[2]);
        String dir = parts[3].substring(0, 1);

        BigDecimal val = new BigDecimal(p0 + ((p1 + (p2 / 60.0)) / 60.0));

        if ( isNegative(dir) )
        {
            val = val.multiply(minusOne);
        }

        return val;
    }

    /**
     * @param dm
     * @return
     */
    public static BigDecimal convertDirectionalDDMMMMToDDDD(final String dm)
    {
        String[] parts = StringUtils.split(dm," d°'\"");
        double p0 =  Double.parseDouble(parts[0]);
        double p1 =  Double.parseDouble(parts[1]);
        String dir = parts[2].substring(0, 1);

        BigDecimal val = new BigDecimal(p0 + (p1 / 60.0));

        if ( isNegative(dir) )
        {
            val = val.multiply(minusOne);
        }

        return val;
    }

    /**
     * @param str
     * @return
     */
    public static BigDecimal convertDirectionalDDDDToDDDD(final String str)
    {
        String[] parts = StringUtils.split(str," d°'\"");
        double p0  = Double.parseDouble(parts[0]);
        String dir = parts[1].substring(0, 1);
        
        BigDecimal val = new BigDecimal(p0);

        if ( isNegative(dir) )
        {
            val = val.multiply(minusOne);
        }

        return val;
    }
    
    //--------------------------------------------------------
    // Inner Class
    //--------------------------------------------------------
    public class Part 
    {
        protected String part;
        protected int    decimalLen;
        
        /**
         * @param part
         * @param decimalLen
         */
        public Part(String part)
        {
            this(part, -1);
        }

        /**
         * @param part
         * @param decimalLen
         */
        public Part(String part, int decimalLen)
        {
            super();
            this.part       = part;
            this.decimalLen = decimalLen;
            
            System.err.println(part+"  "+decimalLen);
        }

        /**
         * @return the part
         */
        public String getPart()
        {
            return part;
        }

        /**
         * @return the decimalLen
         */
        public int getDecimalLen()
        {
            return decimalLen;
        }
    }
}
