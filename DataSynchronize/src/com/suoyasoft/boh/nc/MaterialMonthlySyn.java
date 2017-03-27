package com.suoyasoft.boh.nc;

import com.suoyasoft.boh.nc.vo.MisStore;
import com.suoyasoft.boh.nc.vo.NcMaterialOutFormBody;
import com.suoyasoft.boh.nc.vo.RSC_IMS_MaterialStockReport;
import com.suoyasoft.boh.utils.MisConn;
import com.suoyasoft.boh.utils.NcMidConn;
import com.suoyasoft.boh.utils.Util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class MaterialMonthlySyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String beginDate = "";
  private String endDate = "";
  private String sql = "";

  protected static Logger logger = Logger.getLogger(MaterialMonthlySyn.class.getName());

  public void synData(String bizDate, String beginDate, String endDate)
  {
    this.beginDate = beginDate;
    this.endDate = endDate;

    loadMisFormData(bizDate);
  }

  private void loadMisFormData(String bizDate)
  {
    String currentStore = "";
    try
    {
      for (Iterator i = new Util().getStoreList().iterator(); i.hasNext(); )
      {
        List rst = new ArrayList();

        MisStore store = (MisStore)i.next();
        currentStore = store.getStoreCode();

        this.sql = "Select * From (select Material_Code,sum(ActualCount) as SaleCount from RSC_IMS_MaterialStockReport where bizdate between '%s' and '%s' and store_code='%s' and checkCount = 1 group by Material_Code) Where SaleCount > 0";

        this.sql = String.format(this.sql, new Object[] { this.beginDate, this.endDate, currentStore });
        logger.debug(this.sql);

        this.misConn = MisConn.getInstance().getConnection();
        this.stmt = this.misConn.prepareStatement(this.sql);
        this.result = this.stmt.executeQuery();

        while (this.result.next())
        {
          RSC_IMS_MaterialStockReport order = new RSC_IMS_MaterialStockReport();
          order.setMaterial_Code(this.result.getString("Material_Code"));
          order.setSaleCount(Float.valueOf(this.result.getFloat("SaleCount")));
          String bodyPK = currentStore + this.beginDate.substring(0, 4) + this.beginDate.substring(5, 7) + this.beginDate.substring(8, 10) + order.getMaterial_Code();
          order.setAutoNO(bodyPK);
          rst.add(order);
        }
        this.stmt.close();
        this.misConn.close();
        formatMaterialData(rst, bizDate, currentStore);
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
        this.misConn.close();
      }
      catch (Exception localException2)
      {
      }
    }
  }

  private void formatMaterialData(List<RSC_IMS_MaterialStockReport> dataSource, String bizDate, String storeCode)
  {
    List rst = new ArrayList();

    for (Iterator i = dataSource.iterator(); i.hasNext(); )
    {
      RSC_IMS_MaterialStockReport report = (RSC_IMS_MaterialStockReport)i.next();
      NcMaterialOutFormBody body = new NcMaterialOutFormBody();

      body.pk_cg_gener_b = report.getAutoNO();
      body.pk_cg_gener_h = report.getFormNO();
      body.cinvcode = report.getMaterial_Code();
      body.dbizdate = bizDate;
      body.noutassistnum = report.getSaleCount();

      rst.add(body);
    }
    insertMaterialForm(rst, bizDate, storeCode);
  }

  private void insertMaterialForm(List<NcMaterialOutFormBody> list, String bizDate, String storeCode)
  {
    String sqlInsForm = "Insert into cg_gener_h(pk_cg_gener_h,cdptcode,Dbilldate) values('%s','%s','%s')";
    String sqlInsFormDetail = "Insert into cg_gener_b(pk_cg_gener_b,pk_cg_gener_h,cinvcode,noutassistnum,dbizdate)values('%s','%s','%s',%s,'%s')";
    String sqlDelForm = "Delete From cg_gener_h Where pk_cg_gener_h = '%s'";
    String sqlDelFormDetail = "Delete From cg_gener_b Where pk_cg_gener_h = '%s'";
    try
    {
      String formNo = storeCode + this.beginDate.substring(0, 4) + this.beginDate.substring(5, 7) + "009";
      this.midConn = NcMidConn.getInstance().getConnection();
      this.midConn.setAutoCommit(false);

      this.sql = String.format(sqlDelFormDetail, new Object[] { formNo });
      this.stmt = this.midConn.prepareStatement(this.sql);
      this.stmt.execute();
      this.stmt.close();

      this.sql = String.format(sqlDelForm, new Object[] { formNo });
      this.stmt = this.midConn.prepareStatement(this.sql);
      this.stmt.execute();
      this.stmt.close();

      this.sql = String.format(sqlInsForm, new Object[] { formNo, storeCode, bizDate });
      logger.debug(this.sql);
      this.stmt = this.midConn.prepareStatement(this.sql);
      this.stmt.execute();
      this.stmt.close();

      for (Iterator i = list.iterator(); i.hasNext(); )
      {
        NcMaterialOutFormBody body = (NcMaterialOutFormBody)i.next();
        this.sql = String.format(sqlInsFormDetail, new Object[] { body.pk_cg_gener_b, formNo, body.cinvcode, body.noutassistnum, bizDate });
        logger.debug(this.sql);
        this.stmt = this.midConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();
      }
      this.midConn.commit();
    }
    catch (Exception e)
    {
      try
      {
        this.midConn.rollback();
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
        this.midConn.close();
      }
      catch (Exception localException3)
      {
      }
    }
  }
}