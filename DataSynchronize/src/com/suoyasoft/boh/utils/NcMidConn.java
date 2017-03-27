package com.suoyasoft.boh.utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class NcMidConn
{
  private static final String PROPERTY = "ncMid.properties";
  private static final String URL = "url";
  private static final String DRIVER = "driver";
  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";
  private String dburl;
  private String dbdriver;
  private String dbusername;
  private String dbpassword;
  private static NcMidConn _OracleDBConn;

  public NcMidConn()
  {
    loadPropertyFile();
  }

  public static NcMidConn getInstance()
  {
    if (_OracleDBConn == null)
    {
      _OracleDBConn = new NcMidConn();
    }

    return _OracleDBConn;
  }

  private void loadPropertyFile()
  {
    InputStream is = super.getClass().getResourceAsStream("ncMid.properties");
    Properties props = new Properties();
    try {
      props.load(is);
      this.dburl = props.getProperty("url");
      this.dbdriver = props.getProperty("driver");
      this.dbusername = props.getProperty("username");
      this.dbpassword = props.getProperty("password");
    }
    catch (Exception e)
    {
      throw new DBException(8001, e);
    }
  }

  public Connection getConnection()
  {
    try
    {
      Class.forName(this.dbdriver).newInstance();
      return DriverManager.getConnection(this.dburl, this.dbusername, this.dbpassword);
    }
    catch (Exception e) {
      throw new DBException(8002, e);
    }
  }
}