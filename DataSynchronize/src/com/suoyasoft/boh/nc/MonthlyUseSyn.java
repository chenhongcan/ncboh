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

public class MonthlyUseSyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String beginDate = "";
  private String endDate = "";
  private String sql = "";

  protected static Logger logger = Logger.getLogger(MonthlyUseSyn.class.getName());

  public void synData(String bizDate, String beginDate, String endDate)
  {
    try
    {
      this.beginDate = beginDate;
      this.endDate = endDate;

      loadMisData();
    }
    catch (Exception e)
    {
      logger.error(e.getMessage());
    }
  }

  private void loadMisData()
  {
    String currentStore = "";
    try
    {
      for (Iterator i = new Util().getStoreList().iterator(); i.hasNext(); )
      {
        List rst = new ArrayList();

        MisStore store = (MisStore)i.next();
        currentStore = store.getStoreCode();

        this.sql = 
          "Select R.Material_Code, M.Material_Name, R.EndCount, R.Store_Code, R.Unit From RSC_IMS_MaterialStockReport r, Rsc_Material M, Rsc_Basematerial B Where M.Material_Code = R.Material_Code And M.Basematerial_Code = b.Basematerial_Code And r.BizDate = '%s' And r.Store_Code = '%s' And r.CheckCount = 1";

        this.sql = String.format(this.sql, new Object[] { this.endDate, currentStore });

        logger.info(this.sql);

        this.misConn = MisConn.getInstance().getConnection();
        this.stmt = this.misConn.prepareStatement(this.sql);
        this.result = this.stmt.executeQuery();

        while (this.result.next())
        {
          logger.info("Material_Code: " + this.result.getString("Material_Code") + ", EndCount: " + this.result.getFloat("EndCount"));
          RSC_IMS_MaterialStockReport order = new RSC_IMS_MaterialStockReport();
          order.setMaterial_Code(this.result.getString("Material_Code"));
          order.setSaleCount(Float.valueOf(this.result.getFloat("EndCount")));
          String bodyPK = currentStore + this.endDate.replace("-", "") + order.getMaterial_Code();
          order.setAutoNO(bodyPK);
          order.setUnit(this.result.getString("unit"));
          order.setMaterial_Name(this.result.getString("Material_Name"));
          rst.add(order);
        }
        this.stmt.close();
        this.misConn.close();

        if (rst.size() == 0)
          continue;
        formatMaterialData(rst, this.endDate, currentStore);
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
      body.companyid = report.getDcCode();
      body.dbizdate = bizDate;
      body.castunitid = report.getUnit();
      body.Invname = report.getMaterial_Name();

      body.noutassistnum = report.getSaleCount();

      rst.add(body);
    }
    insertMaterialForm(rst, bizDate, storeCode);
  }

  private void insertMaterialForm(List<NcMaterialOutFormBody> list, String bizDate, String storeCode)
  {
    String sqlInsForm = "Insert Into nc_boh_generalout(pk_generalout, busidate, vbillcode, deptcode, disposed, actiontype, isupdateallow, ts) Values('%s', '%s', '%s', '%s', 0, 0, 'Y', TO_CHAR(sysdate,'yyyy-mm-dd hh24:mi:ss'))";

    String sqlInsFormDetail = "Insert Into nc_boh_generalout_b(pk_generalout_b, pk_generalout, invcode, invname, meaname, nnumber, disposed,actiontype,isupdateallow,ts) Values('%s', '%s', '%s', '%s', '%s', '%s', 0,0,0,TO_CHAR(sysdate,'yyyy-mm-dd hh24:mi:ss'))";
    try
    {
      String formNo = storeCode + this.endDate.replace("-", "");
      this.midConn = NcMidConn.getInstance().getConnection();
      this.midConn.setAutoCommit(false);

      this.sql = String.format(sqlInsForm, new Object[] { formNo, bizDate, formNo, storeCode });
      logger.info(this.sql);
      this.stmt = this.midConn.prepareStatement(this.sql);
      this.stmt.execute();
      this.stmt.close();

      for (Iterator i = list.iterator(); i.hasNext(); )
      {
        NcMaterialOutFormBody body = (NcMaterialOutFormBody)i.next();
        this.sql = String.format(sqlInsFormDetail, new Object[] { body.pk_cg_gener_b, formNo, body.cinvcode, body.Invname, body.castunitid, body.noutassistnum });
        logger.info(this.sql);
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
      }
      catch (Exception localException1) {
      }
      logger.error("", e);
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