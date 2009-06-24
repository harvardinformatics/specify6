/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.dbsupport;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.mysql.management.driverlaunched.ServerLauncherSocketFactory;

import edu.ku.brc.ui.UIRegistry;

/**
 * A singleton that remembers all the information needed for creating a JDBC Database connection. 
 * It uses the DBConnection
 * After setting the necessary parameters you can ask it for a connection at any time.<br><br>
 * Also, has a factory method for creating instances so users can connect to more than one database at a time.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class DBConnection
{
    private static final Logger log = Logger.getLogger(DBConnection.class);
    
    protected String dbUsername;
    protected String dbPassword;
    protected String dbConnectionStr;             // For Create or Open
    protected String dbCloseConnectionStr = null; // for closing
    protected String dbDriver;
    protected String dbDialect;                   // needed for Hibernate
    protected String dbName;
    protected String serverName;                  // Hostname
    protected String dbDriverName;                // Hostname
    
    protected boolean argHaveBeenChecked = false;
    protected boolean skipDBNameCheck    = false;
    
    protected Connection connection = null;
     
    protected String     errMsg = ""; //$NON-NLS-1$
    
    // Static Data Members
    protected static final DBConnection  instance;
    protected static Boolean             isEmbeddedDB;
    protected static File                embeddedDataDir;
    protected static Stack<DBConnection> connections;
    protected static boolean             isShuttingDown;
    protected static File                mobileTmpDir = null;
    protected static boolean             isCopiedToMachineDisk = false;
    
    static
    {
        isShuttingDown  = false;
        isEmbeddedDB    = null;
        embeddedDataDir = null;
        connections     = new Stack<DBConnection>();
        instance        = new DBConnection();
        
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() 
            {
                Runtime.getRuntime().addShutdownHook(new Thread() 
                {
                    @Override
                    public void run() 
                    {
                        if (isEmbeddedDB)
                        {
                            ServerLauncherSocketFactory.shutdown(embeddedDataDir, null);
                        }
                    }
                });
                return null;
            }
        });
    }
    
    /**
     * Protected Default constructor
     *
     */
    protected DBConnection()
    {
        connections.push(this);
    }
    
    /**
     * @param connectionStr
     * @return
     */
    public static boolean isEmbedded(final String connectionStr)
    {
        return StringUtils.isNotEmpty(connectionStr) && StringUtils.contains(connectionStr, "mxj");
    }
    
    /**
     * For Embedded MySQL.
     * @param connectionStr JDBC connection string
     */
    public static void checkForEmbeddedDir(final String connectionStr)
    {
        isEmbeddedDB = isEmbedded(connectionStr);
        if (isEmbeddedDB)
        {
            String attr = "server.basedir=";
            int inx = connectionStr.indexOf(attr);
            if (inx > -1)
            {
                inx += attr.length();
                int eInx = connectionStr.indexOf("&", inx);
                if (eInx > -1)
                {
                    embeddedDataDir = new File(connectionStr.substring(inx, eInx));
                }
            }
        }
    }
    
    /**
     * @return whether the database is being run in "embedded" mode
     */
    public boolean isEmbedded()
    {
        return isEmbeddedDB;
    }
    
    /**
     * @param dbUsername
     * @param dbPassword
     * @param dbConnectionStr
     * @param dbDriver
     * @param dbDialect
     * @param dbName
     */
    public DBConnection(String dbUsername, 
                        String dbPassword, 
                        String dbConnectionStr,
                        String dbDriver, 
                        String dbDialect, 
                        String dbName)
    {
        super();
        this.dbUsername      = dbUsername;
        this.dbPassword      = dbPassword;
        this.dbConnectionStr = dbConnectionStr;
        this.dbDriver        = dbDriver;
        this.dbDialect       = dbDialect;
        this.dbName          = dbName;
        this.skipDBNameCheck = dbName == null;
        
        connections.push(this);
        
        checkForEmbeddedDir(dbConnectionStr);
    }

    /**
     * The error message if it was caused by an exception.
     * @return the error message if it was caused by an exception
     */
    public String getErrorMsg()
    {
        return this.errMsg;
    }
    
    /**
     * @param skipDBNameCheck the skipDBNameCheck to set
     */
    public void setSkipDBNameCheck(boolean skipDBNameCheck)
    {
        this.skipDBNameCheck = skipDBNameCheck;
    }

    /**
     * Returns a new connection to the database from an instance of DBConnection.
     * It uses the database name, driver, username and password to connect.
     * @return the JDBC connection to the database
     */
    public Connection createConnection()
    {
        if (UIRegistry.isMobile() && this == getInstance())
        {
            copyToMachineDisk();
        }
        
        Connection con = null;
        try
        {
            if (!argHaveBeenChecked)
            {
                if (!skipDBNameCheck && StringUtils.isEmpty(dbName))
                {
                    errMsg = getResourceString("DBConnection.NO_DB_NAME"); //$NON-NLS-1$
                    return null;
                }
                if (StringUtils.isEmpty(dbConnectionStr))
                {
                    errMsg = getResourceString("DBConnection.NO_DB_CONN_STR"); //$NON-NLS-1$
                    return null;
                }
                if (StringUtils.isEmpty(dbUsername))
                {
                    errMsg = getResourceString("DBConnection.NO_DB_USERNAME");//"The Username is empty."; //$NON-NLS-1$
                    return null;
                }
                if (StringUtils.isEmpty(dbPassword))
                {
                    errMsg = getResourceString("DBConnection.NO_DB_PASSWORD");//"The Password is empty."; //$NON-NLS-1$
                    return null;
                }
                if (StringUtils.isEmpty(dbDriver))
                {
                    errMsg = getResourceString("DBConnection.NO_DB_DRIVER"); //$NON-NLS-1$
                    return null;
                }
                argHaveBeenChecked = true;
            }
            Class.forName(dbDriver); // load driver
            
            //log.debug("["+dbConnectionStr+"]["+dbUsername+"]["+dbPassword+"] ");
            con = DriverManager.getConnection(dbConnectionStr, dbUsername, dbPassword);
            
        } catch (SQLException sqlEX)
        {
            sqlEX.printStackTrace();
            
            log.error("Error in getConnection", sqlEX);
            if (sqlEX.getNextException() != null)
            {
                errMsg = sqlEX.getNextException().getMessage();
            } else
            {
                errMsg = sqlEX.getMessage();
            }
                
        } catch (Exception ex)
        {
//            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
//            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DBConnection.class, ex);
            log.error("Error in getConnection", ex);
            errMsg = ex.getMessage();
        }
        return con;
    }
    
    /**
     * Closes the connection to the database and disposes it.
     */
    public void close()
    {
        try
        {
            if (connections.indexOf(this) > -1)
            {
                connections.remove(this);
            } else
            {
                String msg = "The DBConnection ["+this+"] has already been removed!";
                log.error(msg);
                //UIRegistry.showError(msg);
            }
            
            if (!isShuttingDown)
            {
                if (this == instance)
                {
                    String msg = "The DBConnection.getInstance().close() should not be called. (Call DBConnection.shutdown()).";
                    log.error(msg);
                    UIRegistry.showError(msg);
                }
            }
            
            // This is primarily for Derby non-networked database. 
            if (dbCloseConnectionStr != null)
            {
                Connection con = DriverManager.getConnection(dbCloseConnectionStr, dbUsername, dbPassword);
                if (con != null)
                {
                    con.close();
                }
            } else if (connection != null)
            {
                connection.close();
                connection = null;
            }
        } catch (Exception ex)
        {
            log.error(ex);
        }
        
    }
    
    /**
     * Sets the user name and password.
     * @param dbUsername the username
     * @param dbPassword the password
     */
    public void setUsernamePassword(final String dbUsername, final String dbPassword)
    {
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        argHaveBeenChecked = false;
    }
    
    /**
     * Sets the database name.
     * @param dbName the database name
     */
    public void setDatabaseName(final String dbName)
    {
        this.dbName = dbName;
        argHaveBeenChecked = false;
    }
    
    /**
     * Sets the driver name.
     * @param dbDriver the driver name
     */
    public void setDriver(final String dbDriver)
    {
        this.dbDriver = dbDriver;
        argHaveBeenChecked = false;
    }
    
    /**
     * Sets the Hibernate Dialect class name.
     * @param dbDialect the driver name
     */
    public void setDialect(final String dbDialect)
    {
        this.dbDialect = dbDialect;
        argHaveBeenChecked = false;
    }
    
    /**
     * @return the serverName
     */
    public String getServerName()
    {
        return serverName;
    }

    /**
     * @param server the server to set
     */
    public void setServerName(String serverName)
    {
        this.serverName = serverName;
    }

    /**
     * @return the dbDriverName
     */
    public String getDriverName()
    {
        return dbDriverName;
    }

    /**
     * @param dbDriverName the dbDriverName to set
     */
    public void setDriverName(String dbDriverName)
    {
        this.dbDriverName = dbDriverName;
    }

    /**
     * Sets the fully specified path to connect to the database.
     * i.e. jdbc:mysql://localhost/fish<br>Some databases may need to construct their fully specified path.
     * @param dbConnectionStr the full connection string
     */
    public void setConnectionStr(final String dbConnectionStr)
    {
        this.dbConnectionStr = dbConnectionStr;
        argHaveBeenChecked = false;
        
        checkForEmbeddedDir(dbConnectionStr);
    }
    
    /**
     * Returns the driver
     * @return the driver
     */
    public String getDriver()
    {
        return dbDriver;
    }

    /**
     * Gets the fully specified path to connect to the database.
     * i.e. jdbc:mysql://localhost/fish<br>Some databases may need to construct their fully specified path.
     * @return the full connection string
     */
    public String getConnectionStr()
    {
        return dbConnectionStr;
    }

    /**
     * Returns the Close Connection String.
     * @return the Close Connection String.
     */
    public String getDbCloseConnectionStr()
    {
        return dbCloseConnectionStr;
    }

    /**
     * Sets the Close Connection String.
     * @param dbCloseConnectionStr the string (can be null to clear it)
     */
    public void setDbCloseConnectionStr(final String dbCloseConnectionStr)
    {
        this.dbCloseConnectionStr = dbCloseConnectionStr;
    }

    /**
     * Returns the Database Name.
     * @return the Database Name.
     */
    public String getDatabaseName()
    {
        return dbName;
    }

    /**
     * Returns the Password.
     * @return the Password.
     */
    public String getPassword()
    {
        return dbPassword;
    }

    /**
     * Returns the USe Name.
     * @return the USe Name.
     */
    public String getUserName()
    {
        return dbUsername;
    }
    
    /**
     * Returns the Dialect.
     * @return the Dialect.
     */
    public String getDialect()
    {
        return dbDialect;
    }

    /**
     * Returns a new connection to the database. 
     * @return the JDBC connection to the database
     */
    public Connection getConnection()
    {
        if (connection == null)
        {
            connection = createConnection();
        }
        
        return connection;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    public void finalize()
    {
        //DataProviderFactory.getInstance().shutdown();
        close();
    }
    
    /**
     * Create a new instance.
     * @param dbDriver the driver name
     * @param dbDialect the dialect class name for Hibernate
     * @param dbName the database name (just the name)
     * @param dbConnectionStr the full connection string
     * @param dbUsername the username
     * @param dbPassword the password
     * @return a new instance of a DBConnection
     */
    public static DBConnection createInstance(final String dbDriver, 
                                              final String dbDialect, 
                                              final String dbName, 
                                              final String dbConnectionStr, 
                                              final String dbUsername, 
                                              final String dbPassword)
    {
        DBConnection dbConnection = new DBConnection();
        
        dbConnection.setDriver(dbDriver);
        dbConnection.setDialect(dbDialect);
        dbConnection.setDatabaseName(dbName);
        dbConnection.setConnectionStr(dbConnectionStr);
        dbConnection.setUsernamePassword(dbUsername, dbPassword);
        
        checkForEmbeddedDir(dbConnectionStr);
        
        return dbConnection;
    }
    
    /**
     * Returns the instance to the singleton.
     * @return the instance to the singleton
     */
    public static DBConnection getInstance()
    {
        return instance;
    }
    
    /**
     * Shuts down all the connections (including the main getInstance()).
     */
    public static void shutdown()
    {
        isShuttingDown = true;
        for (DBConnection dbc : new Vector<DBConnection>(connections))
        {
            dbc.close();
        }
        connections.clear();
        isShuttingDown = false;
        
        if (UIRegistry.isMobile())
        {
            copyToMobileDisk();
        }
    }
    
    public static File getMobileTempDir() throws IOException
    {
        if (mobileTmpDir == null)
        {
            String path = UIRegistry.getDefaultUserHomeDir();
            
            mobileTmpDir = new File(path + File.separator + "specify_data_" + Long.toString(System.currentTimeMillis()));
    
            if (mobileTmpDir.exists() || !(mobileTmpDir.mkdir())) 
            { 
                throw new IOException("Could not create temp directory: " + mobileTmpDir.getAbsolutePath()); 
            }
        }
        return mobileTmpDir;
    }
    
    /**
     * @return
     */
    private static boolean copyToMachineDisk()
    {
        if (!isCopiedToMachineDisk)
        {
            try
            {
                mobileTmpDir = getMobileTempDir();
                FileUtils.copyDirectory(new File(UIRegistry.getMobileEmbeddedDBPath()), mobileTmpDir, true);
                UIRegistry.setEmbeddedDBDir(mobileTmpDir.getAbsolutePath());
                isCopiedToMachineDisk = true;
                
                for (Object fObj : FileUtils.listFiles(mobileTmpDir, null, true))
                {
                    File f = (File)fObj;
                    if (f.getName().endsWith("DS_Store"))
                    {
                        f.delete();
                    }
                }
                return true;
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        return false;
    }

    private static boolean copyToMobileDisk()
    {
        if (mobileTmpDir != null && isCopiedToMachineDisk)
        {
            try
            {
                File mobileDir = new File(UIRegistry.getMobileEmbeddedDBPath());
                FileUtils.deleteDirectory(mobileDir);
                FileUtils.copyDirectory(mobileTmpDir, mobileDir, true);
                for (Object fObj : FileUtils.listFiles(mobileDir, null, true))
                {
                    File f = (File)fObj;
                    if (f.getName().endsWith("DS_Store"))
                    {
                        f.delete();
                    }
                }
                for (Object fObj : FileUtils.listFiles(mobileTmpDir, null, true))
                {
                    File f = (File)fObj;
                    if (f.exists() && !f.getName().equals("mysql.sock"))
                    {
                        f.delete();
                    }
                }
                return true;
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        return false;
    }

}
