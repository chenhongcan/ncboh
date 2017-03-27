package com.suoyasoft.boh.zy;

import com.suoyasoft.boh.nc.vo.MisStore;
import com.suoyasoft.boh.utils.MisConn;
import com.suoyasoft.boh.utils.Util;
import com.suoyasoft.boh.utils.ZyConn;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class MenuSyn
{
  private Connection zyConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  protected static Logger logger = Logger.getLogger(MenuSyn.class.getName());

  public void synData()
  {
    synItemcodeCategory();

    synItemcode();

    synItemcodeMap();

    synItemcodeStoreMap();
  }

  private void synItemcodeCategory()
  {
    String sql = 
      "Select CATEGORY_CODE, CATEGORY_NAME, SORT_NO, STATUS, MNEMONICCODE, BRAND_CODE From Mid_Itemcode_Category";

    MessageFormat mf = new MessageFormat(
      "Merge Into T_Rsc_Itemcode_Category R Using (Select '1' From dual) N on (R.CATEGORY_CODE=''{0}'') WHEN MATCHED THEN      UPDATE Set CATEGORY_NAME=''{1}'', SORT_NO={2}, Status={3}, MNEMONICCODE=''{4}'', BRAND_CODE=''{5}'' WHEN NOT MATCHED THEN      INSERT (CATEGORY_CODE, CATEGORY_NAME, SORT_NO, Status, MNEMONICCODE, BRAND_CODE) Values(''{0}'', ''{1}'', {2}, {3}, ''{4}'', ''{5}'')");
    try
    {
      this.zyConn = ZyConn.getInstance().getConnection();
      this.misConn = MisConn.getInstance().getConnection();
      this.misConn.setAutoCommit(false);

      this.stmt = this.zyConn.prepareStatement(sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        sql = mf.format(
          new String[] { 
          this.result.getString("CATEGORY_CODE"), 
          this.result.getString("CATEGORY_NAME"), 
          this.result.getString("SORT_NO"), 
          this.result.getString("STATUS"), 
          this.result.getString("MNEMONICCODE"), 
          this.result.getString("BRAND_CODE") });

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

  private void synItemcode()
  {
    String sql = 
      "Select ITEM_CODE, ITEM_NAME, ITEM_TYPE, STATUS, MNEMONICCODE, CATEGORY_CODE From Mid_Itemcode Where ITEM_CODE != ''";

    MessageFormat mf = new MessageFormat(
      "Merge Into T_Rsc_Itemcode R Using (Select '1' From dual) N on (R.ITEM_CODE=''{0}'') WHEN MATCHED THEN      UPDATE Set ITEM_NAME=''{1}'', ITEM_TYPE=''{2}'', Status={3}, MNEMONICCODE=''{4}'', CATEGORY_CODE=''{5}'' WHEN NOT MATCHED THEN      INSERT (ITEM_CODE, ITEM_NAME, ITEM_TYPE, Status, MNEMONICCODE, CATEGORY_CODE) Values(''{0}'', ''{1}'', ''{2}'', {3}, ''{4}'', ''{5}'')");
    try
    {
      this.zyConn = ZyConn.getInstance().getConnection();
      this.misConn = MisConn.getInstance().getConnection();
      this.misConn.setAutoCommit(false);

      this.stmt = this.zyConn.prepareStatement(sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        sql = mf.format(
          new String[] { 
          this.result.getString("ITEM_CODE"), 
          this.result.getString("ITEM_NAME"), 
          this.result.getString("ITEM_TYPE"), 
          this.result.getString("STATUS"), 
          this.result.getString("MNEMONICCODE"), 
          this.result.getString("CATEGORY_CODE") });

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

  private void synItemcodeMap()
  {
    String sql = "Select ITEM_CODE, ITEM_CODE_PK, AMOUNT From Mid_Itemcode_Map Order by ITEM_CODE";
    try
    {
      this.zyConn = ZyConn.getInstance().getConnection();
      this.misConn = MisConn.getInstance().getConnection();
      this.misConn.setAutoCommit(false);

      this.stmt = this.zyConn.prepareStatement(sql);
      this.result = this.stmt.executeQuery();

      String key = "";
      String tmp = "";
      while (this.result.next())
      {
        key = this.result.getString("ITEM_CODE");
        if (key != tmp)
        {
          sql = "Delete From T_Rsc_Itemcode_map Where ITEM_CODE = '" + key + "'";
          this.stmt = this.misConn.prepareStatement(sql);
          this.stmt.execute();
          this.stmt.close();
        }

        tmp = key;
        sql = "Insert Into T_Rsc_Itemcode_map (ITEM_CODE, ITEM_CODE_PK, COUNT) Values ('" + key + "', '" + this.result.getString("ITEM_CODE_PK") + "', " + this.result.getString("AMOUNT") + ")";
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