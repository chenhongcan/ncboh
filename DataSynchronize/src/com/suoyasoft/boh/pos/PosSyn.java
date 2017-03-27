package com.suoyasoft.boh.pos;

import com.suoyasoft.boh.nc.vo.MisStore;
import com.suoyasoft.boh.utils.MisConn;
import com.suoyasoft.boh.utils.Util;
import com.suoyasoft.boh.utils.ZyConn;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class PosSyn
{
  private Connection zyConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  protected static Logger logger = Logger.getLogger(PosSyn.class.getName());

  private String path = null;

  public void synData()
  {
    updatePosScreenButtons();

    getStoreList();
  }

  private void updatePosScreenButtons()
  {
    try
    {
      CallableStatement proc = null;
      this.misConn = MisConn.getInstance().getConnection();
      this.misConn.setAutoCommit(false);
      proc = this.misConn.prepareCall("{ call SUOYASOFT.SP_POS_SCREEN_BUTTONS_UPDATE }");
      proc.execute();
      this.misConn.commit();
    }
    catch (Exception e)
    {
      try
      {
        this.misConn.rollback();
      }
      catch (Exception localException1)
      {
      }
    }
    finally
    {
      try
      {
        this.misConn.close();
      }
      catch (Exception localException3)
      {
      }
    }
  }

  private ResultSet getStoreList()
  {
    String sql = " Select s.Store_Code From RSC_Store s Left join Rsc_Sys_Data_Type t1 On t1.dict_code = s.Market_Code Left join Rsc_Sys_Data_Type t2 On t2.dict_code = s.brand_code Left join Rsc_Sys_Data_Type t3 On t3.dict_code = s.City Where s.Market_Code is not null And s.Status = 1 Order by s.brand_code, s.Market_Code, s.city, s.Store_Code ";
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.misConn.setAutoCommit(false);
      this.stmt = this.misConn.prepareStatement(sql);
      this.result = this.stmt.executeQuery();
      while (this.result.next())
      {
        exportFile(this.result.getString("Store_Code"));
      }
      this.stmt.close();
      this.misConn.commit();
    }
    catch (Exception e)
    {
      try
      {
        this.misConn.rollback();
      } catch (Exception localException1) {
      }
      logger.error(sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      }
      catch (Exception localException4) {
      }
      try {
        this.misConn.close();
      } catch (Exception localException5) {
      }
    }
    return this.result;
  }

  private ResultSet getScreenList()
  {
    ResultSet rst = null;
    String sql = " Select * From Pos_Screen Order by ID ";
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.misConn.setAutoCommit(false);
      this.stmt = this.misConn.prepareStatement(sql);
      rst = this.stmt.executeQuery();
      this.misConn.close();
      this.misConn.commit();
    }
    catch (Exception e)
    {
      try
      {
        this.misConn.rollback();
      } catch (Exception localException1) {
      }
      logger.error(sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      }
      catch (Exception localException4) {
      }
      try {
        this.misConn.close();
      } catch (Exception localException5) {
      }
    }
    return rst;
  }

  private ResultSet getScreenButtonList(String storeCode, String screenId)
  {
    ResultSet rst = null;
    StringBuffer sql = new StringBuffer();
    sql.append(" Select * From Pos_Screen_Buttons Where 1 = 1 ")
      .append("And StoreCode = '").append(storeCode).append("'");
    if (screenId != null) {
      sql.append(" And ScreenID = '").append(screenId).append("'");
    }
    sql.append("Order by ScreenID, ButtonID");
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.misConn.setAutoCommit(false);
      this.stmt = this.misConn.prepareStatement(sql.toString());
      rst = this.stmt.executeQuery();
      this.stmt.close();
      this.misConn.commit();
    }
    catch (Exception e)
    {
      try
      {
        this.misConn.rollback();
      } catch (Exception localException1) {
      }
      logger.error(sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      }
      catch (Exception localException4) {
      }
      try {
        this.misConn.close();
      } catch (Exception localException5) {
      }
    }
    return rst;
  }

  private void exportFile(String storeCode)
  {
    try
    {
      ResultSet rst = getScreenList();

      StringBuffer content = new StringBuffer("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
      content.append("<Screens>\n");
      content.append(" <Screen ID=\"000\" Timeout=\"no\" Type=\"2\" Title=\"空白菜单\">\n");
      content.append("   <Button ID=\"90\" Class=\"1233128\" Title=\"取消\\产品\" Colors=\"1|15|15|1\" Key=\"0|0\" Bitmap=\"\" OnClick=\"cfmOC_CancelItem\"/>\n");
      content.append(" </Screen>\n");
      while (rst.next())
      {
        content.append(toXmlHead(rst));
        ResultSet rsts = getScreenButtonList(storeCode, String.valueOf(rst.getInt("ID")));
        while (rsts.next())
        {
          if (rsts.getString("TITLE") == null) continue; if (rsts.getString("TITLE") == "") {
            continue;
          }

          content.append(toXml(rsts));
        }
        content.append("\t</Screen>\n");
      }
      writeFile(storeCode, content.toString());
    }
    catch (Exception e)
    {
      logger.error(e.getMessage());
    }
  }

  private void writeFile(String storeCode, String content)
  {
    try
    {
      String tmp = getRootPath() + "PosConfig\\" + storeCode + "\\";
      File f = new File(tmp);
      if (!(f.exists()))
      {
        f.mkdirs();
      }

      f = new File(f.getAbsolutePath() + "\\Screen.xml");
      if (!(f.exists()))
      {
        f.createNewFile();
      }

      BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
      output.write(content);
      output.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private String getRootPath()
  {
    if (this.path == null)
    {
      String tmp = "";
      String url = Thread.currentThread().getContextClassLoader().getResource("serviceConfig.xml").toString();
      int idx = url.lastIndexOf("WEB-INF");
      this.path = url.substring(0, idx);
      if (System.getProperty("os.name").indexOf("Windows") != -1)
      {
        tmp = "\\";
        this.path = this.path.replace("/", "\\");
        this.path = this.path.replace("vfsfile:" + tmp, "");
      }
      else
      {
        tmp = "/";
        this.path = this.path.replace("\\", "/");
        this.path = this.path.replace("vfsfile:", "");
      }
      this.path += tmp;
    }
    return this.path;
  }

  public String toXml(ResultSet rsts)
  {
    String s = "";
    try {
      s = String.format(
        "\t\t<Button ID=\"%s\" Code=\"%s\" Class=\"%s\" Title=\"%s\" Colors=\"%s\" Key=\"%s\" Bitmap=\"%s\" OnClick=\"%s\"/>\n", new Object[] { 
        rsts.getString("BUTTONID"), 
        rsts.getString("ITEMCODE") + "," + rsts.getInt("ITEMTYPE"), 
        (rsts.getString("CLASSS") == null) ? "1233072" : rsts.getString("CLASSS"), 
        (rsts.getString("TITLE") == null) ? "" : rsts.getString("TITLE"), 
        (rsts.getString("COLORS") == null) ? "1|1|1|1" : rsts.getString("COLORS"), 
        (rsts.getString("KEY") == null) ? "0|0" : rsts.getString("KEY"), 
        (rsts.getString("BITMAP") == null) ? "" : rsts.getString("BITMAP"), 
        (rsts.getString("ONCLICK") == null) ? "" : rsts.getString("ONCLICK") });
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
    return s;
  }

  public String toXmlHead(ResultSet rst)
    throws SQLException
  {
    String s = "";
    s = String.format(
      "\t<Screen ID=\"%s\" Timeout=\"%s\" NeedVerify=\"%s\" Type=\"%s\" Title=\"%s\" OnActivate=\"%s\">\n", new Object[] { 
      rst.getString("ID"), 
      (rst.getInt("TIMEOUT") == 0) ? "No" : "Yes", 
      (rst.getInt("NEEDVERIFY") == 0) ? "No" : "Yes", 
      rst.getString("TYPE"), 
      rst.getString("TITLE"), 
      rst.getString("ONACTIVATE") });

    return s;
  }

  private void synItemcodeStoreMap()
  {
    for (Iterator i = new Util().getStoreList().iterator(); i.hasNext(); )
    {
      synStoreItemcode(((MisStore)i.next()).getStoreCode());
    }
  }

  private void synStoreItemcode(String storeCode)
  {
    String sql = "";
    try
    {
      this.zyConn = ZyConn.getInstance().getConnection();
      this.misConn = MisConn.getInstance().getConnection();
      this.misConn.setAutoCommit(false);

      sql = "Select ITEM_CODE, STORE_CODE, STATUS From Mid_ItemcodeStore_Map Where Store_Code = '" + storeCode + "'";
      this.stmt = this.zyConn.prepareStatement(sql);
      this.result = this.stmt.executeQuery();

      sql = "Delete From T_Rsc_StoreItemcodeMap Where Store_Code = '" + storeCode + "'";
      this.stmt = this.misConn.prepareStatement(sql);
      this.stmt.execute();
      this.stmt.close();

      while (this.result.next())
      {
        sql = "Insert Into T_Rsc_StoreItemcodeMap (ITEM_CODE, STORE_CODE, STATUS) Values ('" + this.result.getString("ITEM_CODE") + "', '" + storeCode + "', " + this.result.getString("STATUS") + ")";
        this.stmt = this.misConn.prepareStatement(sql);
        this.stmt.execute();
        this.stmt.close();
      }
      this.misConn.commit();
    }
    catch (Exception e)
    {
      try
      {
        this.misConn.rollback();
      } catch (Exception localException1) {
      }
      logger.error(sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      }
      catch (Exception localException4) {
      }
      try {
        this.zyConn.close();
        this.misConn.close();
      }
      catch (Exception localException5)
      {
      }
    }
  }
}