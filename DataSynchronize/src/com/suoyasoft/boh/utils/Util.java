package com.suoyasoft.boh.utils;

import com.suoyasoft.boh.nc.vo.MisStore;
import com.suoyasoft.boh.nc.vo.NcReceipt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class Util
{
  private Connection misConn;
  private Connection midConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String sql = "";
  protected static Logger logger = Logger.getLogger(Util.class.getName());

  public static Boolean IsTestRun()
  {
    return Boolean.valueOf(false);
  }

  public static Boolean isNull(String str)
  {
    if (str == null) return Boolean.valueOf(true);
    if (str.equals("")) return Boolean.valueOf(true);
    if (str.toLowerCase().equals("null")) return Boolean.valueOf(true);
    return Boolean.valueOf(false);
  }

  public static String padRight(String str, int size, char padChar)
  {
    StringBuffer padded = new StringBuffer(str);
    while (padded.length() < size)
    {
      padded.append(padChar);
    }
    return padded.toString();
  }

  public static String padLeft(String str, int size, char padChar)
  {
    String tmp = str;
    while (tmp.length() < size)
    {
      tmp = padChar + tmp;
    }
    return tmp;
  }

  public void updateMisFormSynStatus(List<String> list)
  {
    if (list.size() == 0) return;
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      for (Iterator i = list.iterator(); i.hasNext(); )
      {
        String formNo = (String)i.next();
        this.sql = String.format("Update Rsc_Ims_Form Set NcSynStatus = 1 Where FormNo = '%s'", new Object[] { formNo });
        logger.debug(this.sql);
        this.stmt = this.misConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();
      }
    }
    catch (Exception e)
    {
      logger.error(this.sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      }
      catch (Exception localException3) {
      }
      try {
        this.misConn.close();
      }
      catch (Exception localException4)
      {
      }
    }
  }

  public void updateMisCashReportSynStatus(List<NcReceipt> list) {
    if (list.size() == 0) return;
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      for (Iterator i = list.iterator(); i.hasNext(); )
      {
        NcReceipt receipt = (NcReceipt)i.next();
        this.sql = String.format("Update RSC_CashDailyReport Set NcSynStatus = 1 Where Store_Code = '%s' And Bizdate = '%s'", new Object[] { receipt.ctbm, receipt.billdate });
        logger.debug(this.sql);
        this.stmt = this.misConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();
      }
    }
    catch (Exception e)
    {
      logger.error(this.sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      }
      catch (Exception localException3) {
      }
      try {
        this.misConn.close();
      }
      catch (Exception localException4)
      {
      }
    }
  }

  public void updateMisStockReportSynStatus(List<String> list) {
    if (list.size() == 0) return;
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      for (Iterator i = list.iterator(); i.hasNext(); )
      {
        String formNo = (String)i.next();
        this.sql = 
          String.format("Update RSC_IMS_MaterialStockReport Set NcSynStatus = 1 Where Store_Code = '%s' And Bizdate = '%s'", new Object[] { 
          formNo.substring(0, 8), formNo.substring(8, 12) + "-" + formNo.substring(12, 14) + "-" + formNo.substring(14, 16) });
        logger.debug(this.sql);
        this.stmt = this.misConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();
      }
    }
    catch (Exception e)
    {
      logger.error(this.sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      }
      catch (Exception localException3) {
      }
      try {
        this.misConn.close();
      }
      catch (Exception localException4)
      {
      }
    }
  }

  public void updateNcSaleoutSynStatus(List<String> list) {
    if (list.size() == 0) return;
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      for (Iterator i = list.iterator(); i.hasNext(); )
      {
        String formNo = (String)i.next();
        this.sql = String.format("Update Nc_boh_generaldbout Set disposed = 1 Where vbillcode = '%s'", new Object[] { formNo });
        logger.info(this.sql);
        this.stmt = this.midConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();
      }
    }
    catch (Exception e)
    {
      logger.error(this.sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      }
      catch (Exception localException3) {
      }
      try {
        this.midConn.close();
      }
      catch (Exception localException4)
      {
      }
    }
  }

  public List<MisStore> getStoreList() {
    List list = new ArrayList();

    this.sql = "Select * From RSC_Store Where Store_Code Like 'CN%' Order by Store_Code";
    logger.debug(this.sql);
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        MisStore store = new MisStore();
        store.setStoreCode(this.result.getString("Store_Code"));
        list.add(store);
      }
      return list;
    }
    catch (Exception e)
    {
      logger.error(this.sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      }
      catch (Exception localException5) {
      }
      try {
        this.midConn.close();
      } catch (Exception localException6) {
      }
    }
    return null;
  }

  public void updateRSCEmployeeExchange(List<String> employeeCodes, String tableName)
  {
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      for (Iterator i = employeeCodes.iterator(); i.hasNext(); )
      {
        String employeeCode = (String)i.next();
        this.sql = String.format("Update %s Set NCSynStatus = 1 Where EmployeeCode = '%s'", new Object[] { tableName, employeeCode });
        logger.debug(this.sql);
        this.stmt = this.misConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();
      }
    }
    catch (Exception e)
    {
      logger.error(this.sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      }
      catch (Exception localException3) {
      }
      try {
        this.misConn.close();
      }
      catch (Exception localException4)
      {
      }
    }
  }

  public void updateRSCSynStatus(String tableName, List<String> employeeCodes, String keyWord)
  {
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      for (Iterator i = employeeCodes.iterator(); i.hasNext(); )
      {
        String keyValue = (String)i.next();
        this.sql = String.format("Update %s Set NcSynStatus = 1 Where %s = '%s'", new Object[] { tableName, keyWord, keyValue });
        logger.debug(this.sql);
        this.stmt = this.misConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();
      }
    }
    catch (Exception e)
    {
      logger.error(this.sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      }
      catch (Exception localException3) {
      }
      try {
        this.misConn.close();
      }
      catch (Exception localException4)
      {
      }
    }
  }
}