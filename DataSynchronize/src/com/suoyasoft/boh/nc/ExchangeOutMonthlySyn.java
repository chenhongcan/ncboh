package com.suoyasoft.boh.nc;

import com.suoyasoft.boh.nc.vo.MisStore;
import com.suoyasoft.boh.nc.vo.NcOtherOutFormBody;
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

public class ExchangeOutMonthlySyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String beginDate = "";
  private String endDate = "";
  private String sql = "";

  protected static Logger logger = Logger.getLogger(ExchangeOutMonthlySyn.class.getName());

  public void synData(String bizDate, String beginDate, String endDate)
  {
    this.beginDate = beginDate;
    this.endDate = endDate;

    loadRSCMaterialStockReport(bizDate);
  }

  private void loadRSCMaterialStockReport(String bizDate)
  {
    String currentStore = "";
    try
    {
      for (Iterator i = new Util().getStoreList().iterator(); i.hasNext(); )
      {
        List rst = new ArrayList();
        MisStore store = (MisStore)i.next();
        currentStore = store.getStoreCode();

        this.sql = "Select * From (select Material_Code,sum(nvl(ExchangeOutCount, 0)) as ExchangeOutCount from RSC_IMS_MaterialStockReport where bizDate between '%s' and '%s' and Store_code='%s' and checkcount = 1 group by Material_Code) Where ExchangeOutCount <> 0";

        this.sql = String.format(this.sql, new Object[] { this.beginDate, this.endDate, currentStore });
        logger.debug(this.sql);

        this.misConn = MisConn.getInstance().getConnection();
        this.stmt = this.misConn.prepareStatement(this.sql);
        this.result = this.stmt.executeQuery();

        while (this.result.next())
        {
          RSC_IMS_MaterialStockReport form = new RSC_IMS_MaterialStockReport();
          form.setMaterial_Code(this.result.getString("Material_Code"));
          form.setExchangeOutCount(Float.valueOf(this.result.getFloat("ExchangeOutCount")));
          String bodyPK = currentStore + this.beginDate.substring(0, 4) + this.beginDate.substring(5, 7) + this.beginDate.substring(8, 10) + form.getMaterial_Code();
          form.setAutoNO(bodyPK);
          rst.add(form);
        }
        this.stmt.close();
        this.misConn.close();
        formatSalesOrderData(rst, bizDate, currentStore);
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

  private void formatSalesOrderData(List<RSC_IMS_MaterialStockReport> dataSource, String bizDate, String storeCode)
  {
    List rst = new ArrayList();

    for (Iterator i = dataSource.iterator(); i.hasNext(); )
    {
      RSC_IMS_MaterialStockReport report = (RSC_IMS_MaterialStockReport)i.next();
      NcOtherOutFormBody body = new NcOtherOutFormBody();

      body.cgeneralbid = String.valueOf(report.getAutoNO());
      body.cinvcode = report.getMaterial_Code();
      body.noutassistnum = report.getExchangeOutCount();

      rst.add(body);
    }
    insertExchangeOutForm(rst, bizDate, storeCode);
  }

  private void insertExchangeOutForm(List<NcOtherOutFormBody> list, String bizDate, String storeCode)
  {
    String sqlInsForm = "Insert into itf_qtck_h(cgeneralhid,cdptcode,dbilldate) values('%s','%s','%s')";
    String sqlInsFormDetail = "Insert into itf_qtck_b(cgeneralbid,cgeneralhid,cinvcode,noutassistnum)values('%s','%s','%s',%s)";
    String sqlDelForm = "Delete From itf_qtck_h Where cgeneralhid = '%s'";
    String sqlDelFormDetail = "Delete From itf_qtck_b Where cgeneralhid = '%s'";
    try
    {
      String formNo = storeCode + this.beginDate.substring(0, 4) + this.beginDate.substring(5, 7) + "002";

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
        NcOtherOutFormBody body = (NcOtherOutFormBody)i.next();
        this.sql = String.format(sqlInsFormDetail, new Object[] { body.cgeneralbid, formNo, body.cinvcode, body.noutassistnum });
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