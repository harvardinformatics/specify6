/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ku.brc.ui.forms.formatters;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.security.AccessController;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.forms.DataObjectGettable;
import edu.ku.brc.ui.forms.DataObjectGettableFactory;


/**
 * This class manages all the Data Object Formatters. A DataObjectFormatter is used to create a string representation 
 * of a data object. Much of the time this is a single field, but sometimes it is a concatenation of several fields.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Jan 17, 2007
 *
 */
public class DataObjFieldFormatMgr
{
    public static final String factoryName = "edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr";
    
    protected static final Logger log = Logger.getLogger(DataObjFieldFormatMgr.class);
    
    protected static DataObjFieldFormatMgr  instance = null;
    
    protected boolean domFound = false;


    protected Hashtable<String, DataObjSwitchFormatter>   formatHash      = new Hashtable<String, DataObjSwitchFormatter>();
    protected Hashtable<Class<?>, DataObjSwitchFormatter> formatClassHash = new Hashtable<Class<?>, DataObjSwitchFormatter>();
    protected Hashtable<String, DataObjAggregator>        aggHash         = new Hashtable<String, DataObjAggregator>();
    protected Hashtable<Class<?>, DataObjAggregator>      aggClassHash    = new Hashtable<Class<?>, DataObjAggregator>();
    protected Object[]                                    args            = new Object[2]; // start with two slots
    
    protected Hashtable<String, Class<?>>                 typeHash        = new Hashtable<String, Class<?>>();
    
    /**
     * Protected Constructor
     */
    protected DataObjFieldFormatMgr()
    {
        Object[] initTypeData = {"string", String.class, 
                                 "int",     Integer.class, 
                                 "float",   Float.class, 
                                 "double",  Double.class, 
                                 "boolean", Boolean.class};
        for (int i=0;i<initTypeData.length;i++)
        {
            typeHash.put((String)initTypeData[i], (Class<?>)initTypeData[i+1]);
            i++;
        }
        load();
    }
    
    /**
     * @return the DOM to process
     */
    protected Element getDOM() throws Exception
    {
        AppContextMgr mgr = AppContextMgr.getInstance();
        if (mgr != null)
        {
            return mgr.getResourceAsDOM("DataObjFormatters");
        }
        return null;
    }

    /**
     * Loads formats from config file
     *
     */
    public void load()
    {
        try
        {
            Element root  = getDOM();
            
            if (root != null)
            {
                domFound = true;
                List<?> formatters = root.selectNodes("/formatters/format");
                for ( Object formatObj : formatters)
                {
                    Element formatElement = (Element)formatObj;

                    String name       = formatElement.attributeValue("name");
                    String className  = formatElement.attributeValue("class");
                    String format     = formatElement.attributeValue("format");
                    boolean isDefault = XMLHelper.getAttr(formatElement, "default", true);
                    
                    Class<?> dataClass = null;
                    if (StringUtils.isNotEmpty(className))
                    {
                        try
                        {
                            dataClass = Class.forName(className);
                        } catch (Exception ex)
                        {
                            log.error("Couldn't load class ["+className+"]");
                        }
                    } else
                    {
                        log.error("Class name ["+className+"] is empty and can't be. Skipping.");
                        continue;
                    }
                    
                    Element switchElement = (Element)formatElement.selectObject("switch");
                    if (switchElement != null)
                    {
                        boolean  isSingle     = getAttr(switchElement, "single", false);
                        String   switchField  = getAttr(switchElement, "field", null);
                        
                        DataObjSwitchFormatter switchFormatter = new DataObjSwitchFormatter(name, isSingle, isDefault, dataClass, switchField);
                        
                        if (formatHash.get(name) == null)
                        {
                            formatHash.put(name, switchFormatter);
    
                        } else
                        {
                            throw new RuntimeException("Duplicate formatter name["+name+"]");
                        }
                        
                        DataObjSwitchFormatter sf = formatClassHash.get(dataClass);
                        if (sf == null || isDefault)
                        {
                            formatClassHash.put(dataClass, switchFormatter);
                        }
                        
                        Element external = (Element)switchElement.selectSingleNode("external");
                        if (external != null)
                        {
                            String externalClassName = getAttr(external, "class", (String)null);
                            if (StringUtils.isNotEmpty(externalClassName))
                            {
                                Properties props = new Properties();
                                
                                List<?> paramElements = external.selectNodes("param");
                                for (Object param : paramElements)
                                {
                                    String nameStr = getAttr((Element)param, "name", null);
                                    String val     = StringUtils.deleteWhitespace(((Element)param).getTextTrim());
                                    if (StringUtils.isNotEmpty(nameStr) && StringUtils.isNotEmpty(val))
                                    {
                                        props.put(nameStr, val);
                                    }
                                }
                                try 
                                {
                                    DataObjDataFieldFormatIFace fmt = Class.forName(externalClassName).asSubclass(DataObjDataFieldFormatIFace.class).newInstance();
                                    fmt.init(name, props);
                                    switchFormatter.add(fmt);
                                    
                                } catch (Exception ex)
                                {
                                    log.error(ex);
                                    ex.printStackTrace();
                                }
                            } else
                            {
                                throw new RuntimeException("The 'class' attribute cannot be empty for an external formatter! ["+name+"]");
                            }
                        } else
                        {
                            List<?> fieldsElements = switchElement.selectNodes("fields");
                            for (Object fieldsObj : fieldsElements)
                            {
                                Element fieldsElement = (Element)fieldsObj;
                                String   valueStr  = getAttr(fieldsElement, "value", null);
                                
                                List<?> fldList = fieldsElement.selectNodes("field");
                                DataObjDataField[] fields = new DataObjDataField[fldList.size()];
                                int inx = 0;
                                for (Object fldObj : fldList)
                                {
                                    Element  fieldElement  = (Element)fldObj;
                                    String   fieldName     = fieldElement.getTextTrim();
                                    String   dataTypeStr   = getAttr(fieldElement, "type",      "string");
                                    String   formatStr     = getAttr(fieldElement, "format",    null);
                                    String   sepStr        = getAttr(fieldElement, "sep",       null);
                                    String   formatterName = getAttr(fieldElement, "formatter", null);
                                    String   uifieldformatter = getAttr(fieldElement, "uifieldformatter", null);
                                    
                                    Class<?> classObj      = typeHash.get(dataTypeStr);
                                    if (classObj == null)
                                    {
                                        log.error("Couldn't map standard type["+dataTypeStr+"]");
                                    }
                                    fields[inx] = new DataObjDataField(fieldName, classObj, formatStr, sepStr, formatterName, uifieldformatter);
                                    inx++;
                                }
                                switchFormatter.add(new DataObjDataFieldFormat(name, dataClass, isDefault, format, valueStr, fields));
                            }
                        }
                    } else
                    {
                        log.error("No switch element! ["+name+"]"); // not needed once we start using a DTD/Schema
                    }
                }
                
                for ( Object aggObj : root.selectNodes("/formatters/aggregators/aggregator"))
                {
                    Element aggElement = (Element)aggObj;

                    String name       = aggElement.attributeValue("name");
                    String  dataClassName = XMLHelper.getAttr(aggElement, "class", null);
                    String separator  = aggElement.attributeValue("separator");
                    String countStr   = aggElement.attributeValue("count");
                    String ending     = aggElement.attributeValue("ending");
                    String format     = aggElement.attributeValue("format");
                    String ordFldName = XMLHelper.getAttr(aggElement, "orderfieldname", null);
                    boolean isDefault = XMLHelper.getAttr(aggElement, "default", true);
                    
                    Integer count = StringUtils.isNotEmpty(countStr) && StringUtils.isNumeric(countStr) ? Integer.parseInt(countStr) : null;
                    
                    Class<?> dataClass = null;
                    if (StringUtils.isNotEmpty(dataClassName))
                    {
                        try
                        {
                            dataClass = Class.forName(dataClassName);
                        } catch (Exception ex)
                        {
                            log.error("Couldn't load class ["+dataClassName+"]");
                        }
                    } else
                    {
                        log.error("Class name ["+dataClassName+"] is empty and can't be. Skipping.");
                        continue;
                    }
                    
                    // TODO check for duplicates!
                    aggHash.put(name, new DataObjAggregator(name, dataClass, isDefault, separator, count, ending, format, ordFldName));
                }
                    
            } else
            {
                log.debug("Couldn't get resource [DataObjFormatters]");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
    }
    
    /**
     * Gets a unique name for a formatter if it doesn't yet have one
     */
    private String getFormatterUniqueName(DataObjSwitchFormatter formatter)
    {
    	String name = formatter.getName();
 
    	if (name == null || name.equals(""))
    	{
    		// find a formatter name that doesn't yet exist in the hash
    		// name formation patter is <field name>.i where i is a counter
    		int i = 1;
    		Set<String> names = formatHash.keySet();
    		String prefix = formatter.getFieldName();
    		name = prefix + "." + Integer.toString(i);
    		while (names.contains((String) name))
    		{
        		name = prefix + "." + Integer.toString(++i);
    		}
    	}
    	formatter.setName(name);
    	return null;
    }
    
    /**
     * Saves formatters
     * @param 
     */
    public void save() 
    {
		StringBuilder sb = new StringBuilder(1024);
    	
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<formats>\n");
    	Iterator<DataObjSwitchFormatter> it = formatHash.values().iterator();
    	while (it.hasNext()) 
    	{
    		//it.next().toXML(sb);
    	}
		sb.append("\n</formats>\n");

        AppResourceIFace escAppRes = AppContextMgr.getInstance().getResourceFromDir("Collection", "XXX");
        if (escAppRes != null)
        {
            escAppRes.setDataAsString(sb.toString());
            AppContextMgr.getInstance().saveResource(escAppRes);
           
        } else
        {
            AppContextMgr.getInstance().putResourceAsXML("XXX", sb.toString());    
        }
    }
    
    /**
     * Adds a new formatter
     */
    public void addFormatter(DataObjSwitchFormatter formatter)
    {
    	getFormatterUniqueName(formatter);
    	formatHash.put(formatter.getName(), formatter);
    	formatClassHash.put(formatter.getDataClass(), formatter);
    }
    
    /**
     * Deletes a formatter from the 
     */
    public void removeFormatter(UIFieldFormatterIFace formatter)
    {
    	formatHash.remove(formatter.getName());
    	formatClassHash.remove(formatter.getName());
    }
    
    /**
     * Returns a data formatter.
     * @param formatName the name
     * @return the formatter
     */
    public static DataObjSwitchFormatter getFormatter(final String formatName)
    {
        return getInstance().formatHash.get(formatName);
    }

    /**
     * Returns all the formatters as a Collection
     * @return all the formatters
     */
    public static Collection<DataObjSwitchFormatter> getFormatters()
    {
        return getInstance().formatHash.values();
    }

    /**
     * Format a data object using a named formatter.
     * @param dataObj the data object for which fields will be formatted for it
     * @param formatName the name of the formatter to use
     * @return the string result of the format
     */
    protected DataObjDataFieldFormatIFace getDataFormatter(final Object dataObj, final String formatName)
    {
        DataObjSwitchFormatter switcherFormatter = formatHash.get(formatName);
        if (switcherFormatter != null)
        {
            return switcherFormatter.getDataFormatter(dataObj);
            
        }
        // else
        log.error("Couldn't find a switchable name ["+formatName+"]");

        return null;
    }

    /**
     * Returns a list of formatters that match the class, the default (if there is one) is at the beginning of the list.
     * @param clazz the class of the data that the formatter is used for.
     * @return return a list of formatters that match the class
     */
    public static List<DataObjSwitchFormatter> getFormatterList(final Class<?> clazz)
    {
        Vector<DataObjSwitchFormatter> list = new Vector<DataObjSwitchFormatter>();
        DataObjSwitchFormatter         defFormatter = null;
        
        for (Enumeration<DataObjSwitchFormatter> e=getInstance().formatHash.elements();e.hasMoreElements();)
        {
        	DataObjSwitchFormatter f = e.nextElement();
            if (clazz == f.getDataClass())
            {
                if (f.isDefault() && defFormatter == null)
                {
                    defFormatter = f;
                } else
                {
                    list.add(f);
                }
            }
        }
        if (defFormatter != null)
        {
            list.insertElementAt(defFormatter, 0);
        }
        return list;
    }

    /**
     * Format a data object using a named formatter
     * @param dataObj the data object for which fields will be formatted for it
     * @return the string result of the format
     */
    protected String formatInternal(final DataObjDataFieldFormatIFace format, final Object dataObj)
    {
        if (format != null)
        {
            if (format.isDirectFormatter())
            {
                return format.format(dataObj);
            }
            
            // XXX FIXME this shouldn't be hard coded here
            DataObjectGettable getter = DataObjectGettableFactory.get(format.getDataClass().getName(), 
                                                                      "edu.ku.brc.ui.forms.DataGetterForObj");
            if (getter != null)
            {
                StringBuilder strBuf = new StringBuilder(128);
                for (DataObjDataField field : format.getFields())
                {
                    Object[] values = UIHelper.getFieldValues(new String[]{field.getName()}, dataObj, getter);
                    
                    
                    Object value = values != null ? values[0] : null;//getter.getFieldValue(dataObj, field.getName());
                    if (value != null)
                    {
                        if (field.getDataObjFormatterName() != null )
                        {
                            String fmtStr = formatInternal(getDataFormatter(value, field.getDataObjFormatterName()), value);
                            if (fmtStr != null)
                            {
                                strBuf.append(fmtStr);
                            }
                            
                        } else if (field.getUiFieldFormatter() != null )
                        {
                            UIFieldFormatterIFace fmt = UIFieldFormatterMgr.getFormatter(field.getUiFieldFormatter());
                            if (fmt != null)
                            {
                                strBuf.append(fmt.formatInBound(value));
                            } else
                            {
                                strBuf.append(value);
                            }
                            
                        } else if (value.getClass() == field.getType())
                        {
                            // When format is null then it is a string
                            if (field.getType() == String.class &&
                                (field.getFormat() == null || format.equals("%s")))
                            {
                                if (field.getSep() != null)
                                {
                                    strBuf.append(field.getSep());
                                }
                                strBuf.append(value.toString());
                            } else
                            {
                                String sep = field.getSep();
                                if (sep != null)
                                {
                                    strBuf.append(sep);
                                }
                                //log.debug("["+value+"]["+format.getFormat()+"]");
                                args[0] = value;
                                Formatter formatter = new Formatter();
                                formatter.format(field.getFormat(), args);
                                strBuf.append(formatter.toString());
                                args[0] = null;
                            }
                        } else
                        {
                            log.error("Mismatch of types data retrieved as class["+value.getClass().getSimpleName()+"] and the format requires ["+field.getType().getSimpleName()+"]");
                        }
                    }
                }
                return strBuf.toString();
            }
        }
        return "";
    }

    /**
     * Format a data object using a named formatter
     * @param dataObj the data object for which fields will be formatted for it
     * @return the string result of the format
     */
    protected String formatInternal(final DataObjDataFieldFormat format, final Object[] dataObjs)
    {
        if (format != null)
        {
            // XXX FIXME this shouldn't be hard coded here
            DataObjectGettable getter = DataObjectGettableFactory.get(format.getDataClass().getName(), 
                                                                      "edu.ku.brc.ui.forms.DataGetterForObj");
            if (getter != null)
            {
                StringBuilder strBuf = new StringBuilder(128);
                
                if (dataObjs.length == format.getFields().length)
                {
                    int inx = 0;
                    for (DataObjDataField field : format.getFields())
                    {
                        Object value = dataObjs[inx++];
                        if (value != null)
                        {
                            
                            if (field.getDataObjFormatterName() != null )
                            {
                                String fmtStr = formatInternal(getDataFormatter(value, field.getDataObjFormatterName()), value);
                                if (fmtStr != null)
                                {
                                    strBuf.append(fmtStr);
                                }
                                
                            } else if (field.getUiFieldFormatter() != null )
                            {
                                UIFieldFormatterIFace fmt = UIFieldFormatterMgr.getFormatter(field.getUiFieldFormatter());
                                if (fmt != null)
                                {
                                    strBuf.append(fmt.formatInBound(value));
                                } else
                                {
                                    strBuf.append(value);
                                }
                                
                            } else if (value.getClass() == field.getType())
                            {
                                // When format is null then it is a string
                                if (field.getType() == String.class &&
                                    (field.getFormat() == null || format.equals("%s")))
                                {
                                    if (field.getSep() != null)
                                    {
                                        strBuf.append(field.getSep());
                                    }
                                    strBuf.append(value.toString());
                                } else
                                {
                                    args[0] = value;
                                    Formatter formatter = new Formatter();
                                    formatter.format(format.getFormat(), args);
                                    strBuf.append(formatter.toString());
                                    args[0] = null;
                                }
                            } else
                            {
                                log.error("Mismatch of types data retrieved as class["+value.getClass().getSimpleName()+"] and the format requires ["+field.getType().getSimpleName()+"]");
                            }
                        }
                    }
                } else
                {
                    log.error("Data Array sent to formatter is not the same length ["+dataObjs.length+"] as the formatter ["+format.getFields().length+"]");
                }
                return strBuf.toString();
            }
        }
        return "";
    }

    /**
     * Aggregates all the items in a Collection into a string given a formatter 
     * @param items the collection of items
     * @param aggName the name of the aggregator to use
     * @return a string representing a collection of all the objects 
     */
    protected String aggregateInternal(final Collection<?> items, final DataObjAggregator agg)
    {
        if (agg != null)
        {
            StringBuilder aggStr = new StringBuilder(128);
            
            int count = 0;
            for (Object obj : items)
            {
                if (obj != null)
                {
                    if (count > 0)
                    {
                        aggStr.append(agg.getSeparator());
                    }
                    aggStr.append(formatInternal(getInstance().getDataFormatter(obj, agg.getFormatName()), obj));
                    
                    if (agg.getCount() != null && count < agg.getCount())
                    {
                        aggStr.append(agg.getEnding());
                        break;
                    }
                }
                count++;
            }
            return aggStr.toString();
            
        }
        // else
        log.error("Aggegrator was null.");
        return null;
    }
    
    /**
     * Format a data object using a named formatter.
     * @param dataObj the data object for which fields will be formatted for it
     * @param formatName the name of the formatter to use
     * @return the string result of the format
     */
    public static String format(final Object dataObj, final String formatName)
    {
        if (getInstance().domFound)
        {
            DataObjSwitchFormatter sf = getInstance().formatHash.get(formatName);
            if (sf != null)
            {
                DataObjDataFieldFormatIFace dff = sf.getDataFormatter(dataObj);
                if (dff != null)
                {
                    return getInstance().formatInternal(dff, dataObj);
                    
                }
                // else
                log.error("Couldn't find DataObjDataFieldFormat for ["+sf.getName()+"] value["+dataObj+"]");
            } else
            {
                log.error("Couldn't find DataObjSwitchFormatter for class ["+formatName+"]"); 
            }
        }
        return null;
    }

    /**
     * Format a data object using a named formatter.
     * @param dataObj the data object for which fields will be formatted for it
     * @param dataClass the class for the data to be formatted
     * @return the string result of the format
     */
    public static String format(final Object dataObj, final Class<?> dataClass)
    {
        if (getInstance().domFound)
        {
            DataObjSwitchFormatter sf = getInstance().formatClassHash.get(dataClass);
            if (sf != null)
            {
                DataObjDataFieldFormatIFace dff = sf.getDataFormatter(dataObj);
                if (dff != null)
                {
                    return getInstance().formatInternal(dff, dataObj);
                }
                // else
                log.error("Couldn't find DataObjDataFieldFormat for ["+sf.getName()+"] value["+dataObj+"]");
            }
            // else
            log.error("Couldn't find DataObjSwitchFormatter for class ["+dataClass.getName()+"]");
        }
        return null;
    }

    /**
     * Format a data object using a named formatter.
     * @param dataObjs the array data objects for which fields will be formatted for it
     * @param formatName the name of the formatter to use
     * @return the string result of the format
     */
    public static String format(@SuppressWarnings("unused")final Object[] dataObjs, @SuppressWarnings("unused") final String formatName)
    {
        throw new RuntimeException("OK, I am used, so come and fix me up!");
        //return instance.formatInternal(dataObjs, formatName);
    }
    
    public static DataObjAggregator getAggregator(final String aggName)
    {
        return getInstance().aggHash.get(aggName);
    }
    
    /**
     * Aggregates all the items in a Collection into a string given a formatter.
     * @param items the collection of items
     * @param aggName the name of the aggregator to use
     * @return a string representing a collection of all the objects 
     */
    public static String aggregate(final Collection<?> items, final String aggName)
    {
        if (getInstance().domFound)
        {
            if (items != null && items.size() > 0)
            {
                DataObjAggregator agg = getInstance().aggHash.get(aggName);
                if (agg != null)
                {
                    return getInstance().aggregateInternal(items, agg);
                    
                }
                // else
                log.error("Couldn't find Aggegrator ["+aggName+"]");
            }
        }
        // else
        return "";
    }
    
    /**
     * Aggregates all the items in a Collection into a string given a formatter.
     * @param items the collection of items
     * @param aggName the name of the aggregator to use
     * @return a string representing a collection of all the objects 
     */
    public static String aggregate(final Collection<?> items, final Class<?> dataClass)
    {
        if (getInstance().domFound)
        {
            DataObjAggregator defAgg = null;
            if (dataClass == Determination.class)
            {
                int x = 0;
                x++;
            }
            for (Enumeration<DataObjAggregator> e=getInstance().aggHash.elements();e.hasMoreElements();)
            {
                DataObjAggregator agg = e.nextElement();
                if (dataClass == agg.getDataClass())
                {
                    if (agg.isDefault())
                    {
                        defAgg = agg;
                        break;
                        
                    } else if (defAgg == null)
                    {
                        defAgg = agg;
                    }
                }
            }
            
            if (defAgg != null)
            {
                return getInstance().aggregateInternal(items, defAgg);
                
            }
            // else
            log.error("Could find aggregator of class ["+dataClass.getCanonicalName()+"]");
        }
        return "";
    }
    
    /**
     * Returns the instance to the singleton
     * @return  the instance to the singleton
     */
    public static DataObjFieldFormatMgr getInstance()
    {
        if (instance != null)
        {
            return instance;
        }
        
        if (StringUtils.isEmpty(factoryName))
        {
            return instance = new DataObjFieldFormatMgr();
        }
        
        // else
        String factoryNameStr = AccessController.doPrivileged(new java.security.PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(factoryName);
                    }
                });
            
        if (StringUtils.isNotEmpty(factoryNameStr)) 
        {
            try 
            {
                return instance = (DataObjFieldFormatMgr)Class.forName(factoryNameStr).newInstance();
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate DataObjFieldFormatMgr factory " + factoryNameStr);
                error.initCause(e);
                throw error;
            }
        } 
        
        instance = new DataObjFieldFormatMgr();
        
        // now that all formats have been loaded, set table/field/formatter info\
        // must be executed after the instance is set
        for ( DataObjSwitchFormatter format : instance.formatHash.values() )
        {
        	format.setTableAndFieldInfo();
        }

        return instance;
        
        // should not happen
        //throw new RuntimeException("Can't instantiate DataObjFieldFormatMgr factory [" + factoryNameStr+"]");
    }
}
