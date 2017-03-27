package com.suoyasoft.boh.nc;

import com.suoyasoft.boh.nc.vo.NcMaterialAccount;
import com.suoyasoft.boh.nc.vo.RSC_MaterialAccount;
import com.suoyasoft.boh.utils.MisConn;
import com.suoyasoft.boh.utils.NcMidConn;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class MaterialAccountSyn
{
  private Connection midConn;
  private Connection misConn;
  private String bizDate;
  private PreparedStatement stmt;
  private ResultSet result;
  private String sql = "";

  protected static Logger logger = Logger.getLogger(SaleOutOrderSyn.class.getName());

  public void synData()
  {
    try
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      Calendar cal = Calendar.getInstance();
      this.bizDate = format.format(cal.getTime());

      List ncMaterialList = loadNcMaterial();

      List rscMaterialList = formatRSCMaterialAccount(ncMaterialList);

      insertMaterialAccount(rscMaterialList);
    }
    catch (Exception e)
    {
      logger.debug(e.getMessage());
    }
  }

  private List<NcMaterialAccount> loadNcMaterial()
  {
    List rst = new ArrayList();
    this.sql = "select t.pk_defdoc2 as pk_defdoc2,t.doccode as doccode,t.vuserdef2 as vuserdef2,t.dbilldate as dbilldate,t.vbillcode as vbillcode,t.invcode as invcode,t.invname as invname,t.noutassistnum as noutassistnum, t.noriginalcurtaxprice as noriginalcurtaxprice,t.nsummny as nsummny,t.measname as measname,t.unitcode as unitcode,t.unitname as unitname,t.pk_corp as pk_corp from nc.VW_IC_SALEOUT t where  t.dbilldate='%s' and t.doccode like '%s' ";

    this.sql = String.format(this.sql, new Object[] { this.bizDate, "CN%" });
    logger.debug(this.sql);
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.stmt = this.midConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();
      while (this.result.next())
      {
        NcMaterialAccount materialAccount = new NcMaterialAccount();
        materialAccount.pk_defdoc2 = this.result.getString("pk_defdoc2");
        materialAccount.doccode = this.result.getString("doccode");
        materialAccount.vuserdef2 = this.result.getString("vuserdef2");
        materialAccount.dbilldate = this.result.getString("dbilldate");
        materialAccount.vbillcode = this.result.getString("vbillcode");
        materialAccount.invcode = this.result.getString("invcode");
        materialAccount.invname = this.result.getString("invname");
        materialAccount.noutassistnum = this.result.getString("noutassistnum");
        materialAccount.noriginalcurtaxprice = this.result.getString("noriginalcurtaxprice");
        materialAccount.nsummny = this.result.getString("nsummny");
        materialAccount.measname = this.result.getString("measname");
        materialAccount.unitcode = this.result.getString("unitcode");
        materialAccount.unitname = this.result.getString("unitname");
        materialAccount.pk_corp = this.result.getString("pk_corp");
        rst.add(materialAccount);
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

  private List<RSC_MaterialAccount> formatRSCMaterialAccount(List<NcMaterialAccount> ncMaterialList)
  {
    RSC_MaterialAccount account = null;
    NcMaterialAccount ncSalesOutOrder = null;
    ArrayList rst = new ArrayList();
    for (Iterator i = ncMaterialList.iterator(); i.hasNext(); )
    {
      ncSalesOutOrder = (NcMaterialAccount)i.next();
      account = new RSC_MaterialAccount();
      account.setStore_Code(ncSalesOutOrder.doccode);
      account.setAmount(Float.valueOf(Float.parseFloat(ncSalesOutOrder.nsummny)));
      account.setCount(Float.valueOf(Float.parseFloat(ncSalesOutOrder.noutassistnum)));
      account.setMaterial_Code(ncSalesOutOrder.invcode);
      account.setMaterialName(ncSalesOutOrder.invname);
      account.setPrice(Float.valueOf(Float.parseFloat(ncSalesOutOrder.noriginalcurtaxprice)));
      account.setUnit(ncSalesOutOrder.measname);
      account.setBizDate(ncSalesOutOrder.dbilldate);

      rst.add(account);
    }
    return rst;
  }

  private void insertMaterialAccount(List<RSC_MaterialAccount> list)
  {
    String sqlIns = "Insert into RSC_IMS_MaterialAccount(Store_Code,BizDate,Price,Material_Code,Count,MaterialName,Unit,Amount) values('%s','%s','%s','%s',%s,'%s','%s','%s')";

    String sqlDel = "Delete From RSC_IMS_MaterialAccount Where BizDate ='%s'";
    try
    {
      RSC_MaterialAccount rscMaterial = null;
      this.misConn = MisConn.getInstance().getConnection();
      this.misConn.setAutoCommit(false);

      this.sql = String.format(sqlDel, new Object[] { this.bizDate });
      this.stmt = this.misConn.prepareStatement(this.sql);
      logger.debug(this.sql);
      this.stmt.execute();
      this.stmt.close();
      for (Iterator i = list.iterator(); i.hasNext(); )
      {
        rscMaterial = (RSC_MaterialAccount)i.next();

        this.sql = String.format(sqlIns, new Object[] { rscMaterial.getStore_Code(), rscMaterial.getBizDate(), rscMaterial.getPrice(), rscMaterial.getMaterial_Code(), rscMaterial.getCount(), rscMaterial.getMaterialName(), rscMaterial.getUnit(), rscMaterial.getAmount() });
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