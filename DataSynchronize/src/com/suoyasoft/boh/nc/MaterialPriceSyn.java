package com.suoyasoft.boh.nc;

import com.suoyasoft.boh.nc.vo.NcMaterialPrice;
import com.suoyasoft.boh.utils.MisConn;
import com.suoyasoft.boh.utils.NcMidConn;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class MaterialPriceSyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String sql = "";

  protected static Logger logger = Logger.getLogger(SaleOutOrderSyn.class.getName());

  public void synData()
  {
    try
    {
      List ncMaterialList = loadNcMaterialPrice();

      insertMaterialAccount(ncMaterialList);
    }
    catch (Exception e)
    {
      logger.debug(e.getMessage());
    }
  }

  private List<NcMaterialPrice> loadNcMaterialPrice()
  {
    List rst = new ArrayList();
    this.sql = "Select pk_corp,invcode,invname,unitcode,unitname,cdate,nprice0,ts From nc.XS_INVPRICECORP ";

    this.sql = String.format(this.sql, new Object[0]);
    logger.debug(this.sql);
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.stmt = this.midConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();
      while (this.result.next())
      {
        NcMaterialPrice materialPrice = new NcMaterialPrice();
        materialPrice.pk_corp = this.result.getString("pk_corp");
        materialPrice.invcode = this.result.getString("invcode");
        materialPrice.invname = this.result.getString("invname");
        materialPrice.unitcode = this.result.getString("unitcode");
        materialPrice.unitname = this.result.getString("unitname");
        materialPrice.cdate = this.result.getString("cdate");
        materialPrice.nprice0 = Double.valueOf(this.result.getDouble("nprice0"));
        materialPrice.ts = this.result.getString("ts");
        rst.add(materialPrice);
      }
      return rst;
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
        this.midConn.close();
      } catch (Exception localException3) {
      }
    }
    return null;
  }

  private void insertMaterialAccount(List<NcMaterialPrice> list)
  {
    String sqlIns = "Insert into XS_INVPRICECORP(pk_corp,unitcode,unitname,invcode,invname,cdate,nprice0,ts) values('%s',%s,'%s','%s','%s','%s','%s','%s')";

    String sqlDel = "Delete From XS_INVPRICECORP";
    try
    {
      NcMaterialPrice ncMaterial = null;
      this.misConn = MisConn.getInstance().getConnection();
      this.misConn.setAutoCommit(false);

      this.sql = String.format(sqlDel, new Object[0]);
      this.stmt = this.misConn.prepareStatement(this.sql);
      logger.debug(this.sql);
      this.stmt.execute();
      this.stmt.close();
      for (Iterator i = list.iterator(); i.hasNext(); )
      {
        ncMaterial = (NcMaterialPrice)i.next();

        this.sql = String.format(sqlIns, new Object[] { ncMaterial.pk_corp, ncMaterial.unitcode, ncMaterial.unitname, ncMaterial.invcode, ncMaterial.invname, ncMaterial.cdate, ncMaterial.nprice0, ncMaterial.ts });
        logger.debug(this.sql);
        this.stmt = this.misConn.prepareStatement(this.sql);
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
      logger.error(this.sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
        this.misConn.close();
      }
      catch (Exception localException3)
      {
      }
    }
  }
}