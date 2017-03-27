package com.suoyasoft.boh.nc;

import com.suoyasoft.boh.nc.vo.MisStore;
import com.suoyasoft.boh.nc.vo.NcOtherInFormBody;
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

public class ExchangeInMonthlySyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String beginDate = "";
  private String endDate = "";
  private String sql = "";

  protected static Logger logger = Logger.getLogger(ExchangeInMonthlySyn.class.getName());

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

        this.sql = "Select * From (select Material_Code, sum(nvl(ExchangeInCount, 0)) as ExchangeInCount from RSC_IMS_MaterialStockReport where bizDate between '%s' and '%s' and Store_code='%s' and checkcount = 1 group by Material_Code) Where ExchangeInCount <> 0";

        this.sql = String.format(this.sql, new Object[] { this.beginDate, this.endDate, currentStore });
        logger.debug(this.sql);

        this.misConn = MisConn.getInstance().getConnection();
        this.stmt = this.misConn.prepareStatement(this.sql);
        this.result = this.stmt.executeQuery();

        while (this.result.next())
        {
          RSC_IMS_MaterialStockReport form = new RSC_IMS_MaterialStockReport();
          form.setMaterial_Code(this.result.getString("Material_Code"));
          form.setExchangeInCount(Float.valueOf(this.result.getFloat("ExchangeInCount")));
          String bodyPK = currentStore + this.beginDate.substring(0, 4) + this.beginDate.substring(5, 7) + this.beginDate.substring(8, 10) + form.getMaterial_Code();
          form.setAutoNO(bodyPK);
          rst.add(form);
        }
        this.stmt.close();
        this.misConn.close();
        formatNcExchangeInFormData(rst, bizDate, currentStore);
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

  private void formatNcExchangeInFormData(List<RSC_IMS_MaterialStockReport> dataSource, String bizDate, String storeCode)
  {
    List rst = new ArrayList();

    for (Iterator i = dataSource.iterator(); i.hasNext(); )
    {
      RSC_IMS_MaterialStockReport report = (RSC_IMS_MaterialStockReport)i.next();
      NcOtherInFormBody body = new NcOtherInFormBody();

      body.pk_itf_qtrk_b = String.valueOf(report.getAutoNO());
      body.cinventorycode = report.getMaterial_Code();
      body.ninassistnum = report.getExchangeInCount();
      body.dbizdate = bizDate;

      rst.add(body);
    }
    insertExchangeInForm(rst, bizDate, storeCode);
  }

  private void insertExchangeInForm(List<NcOtherInFormBody> list, String bizDate, String storeCode)
  {
    String sqlInsForm = "Insert into itf_qtrk_h(pk_itf_qtrk_h,restaurantcode,dbilldate) values('%s','%s','%s')";
    String sqlInsFormDetail = "Insert into itf_qtrk_b(pk_itf_qtrk_b,pk_itf_qtrk_h,cinventorycode,ninassistnum,dbizdate)values('%s','%s','%s',%s,'%s')";
    String sqlDelForm = "Delete From itf_qtrk_h Where pk_itf_qtrk_h = '%s'";
    String sqlDelFormDetail = "Delete From itf_qtrk_b Where pk_itf_qtrk_h = '%s'";
    try
    {
      String formNo = storeCode + this.beginDate.substring(0, 4) + this.beginDate.substring(5, 7) + "001";

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
        NcOtherInFormBody body = (NcOtherInFormBody)i.next();
        this.sql = String.format(sqlInsFormDetail, new Object[] { body.pk_itf_qtrk_b, formNo, body.cinventorycode, body.ninassistnum, body.dbizdate });
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