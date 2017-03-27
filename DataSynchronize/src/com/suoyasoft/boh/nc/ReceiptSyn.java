package com.suoyasoft.boh.nc;

import com.suoyasoft.boh.nc.vo.MisCashDailyReport;
import com.suoyasoft.boh.nc.vo.NcReceipt;
import com.suoyasoft.boh.utils.MisConn;
import com.suoyasoft.boh.utils.NcMidConn;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class ReceiptSyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String sql = "";

  protected static Logger logger = Logger.getLogger(ReceiptSyn.class.getName());

  public void synData(String bizDate, String beginDate, String endDate)
  {
    try
    {
      List reports = loadMisCashDailyReport(beginDate, endDate);

      List receipts = initReceipt(reports);

      synchronizeReceipt(receipts);
    }
    catch (Exception e)
    {
      logger.error(e.getMessage());
    }
  }

  private List<MisCashDailyReport> loadMisCashDailyReport(String beginDate, String endDate)
  {
    List reports = new ArrayList();

    this.sql = "Select Store_Code , sum(GrossSales) as GrossSales, sum(DELIVERY) as Delivery, sum(GIFT) as Gift, sum(WASTE) as Waste, sum(HOGWASH) as Hogwash , sum(OTHER) as Other, sum(CASHOVERSHORT) as CashOvershort, sum(TC) as TC, sum(EMPLOYEEMEAL) as EmployeeMeal, sum(VIPCOUPON) as VIPCoupon , sum(GCRED) as GCRed, sum(PAYMENTDEDUCTION) as PaymentDeduction, sum(OUTBIZINCOMING) as OutBizIncoming, sum(REGISTERSALES) as RegisterSales, sum(SAVING) as Saving From RSC_CashDailyReport Where Store_Code Like 'CN%' And BizDate between '%s' and '%s' Group by Store_Code";

    this.sql = String.format(this.sql, new Object[] { beginDate, endDate });
    logger.info(this.sql);
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        MisCashDailyReport report = new MisCashDailyReport();
        report = new MisCashDailyReport();
        report.BizDate = endDate;
        report.Store_Code = this.result.getString("Store_Code");
        report.CashOvershort = this.result.getFloat("CashOvershort");
        report.Delivery = this.result.getFloat("Delivery");
        report.EmployeeMeal = this.result.getFloat("EmployeeMeal");
        report.GCRed = this.result.getFloat("GCRed");
        report.Gift = this.result.getFloat("Gift");
        report.GrossSales = this.result.getFloat("GrossSales");
        report.Hogwash = this.result.getFloat("Hogwash");
        report.Other = this.result.getFloat("Other");
        report.OutBizIncoming = this.result.getFloat("OutBizIncoming");
        report.PaymentDeduction = this.result.getFloat("PaymentDeduction");
        report.RegisterSales = this.result.getFloat("RegisterSales");
        report.Saving = this.result.getFloat("Saving");
        report.TC = this.result.getInt("TC");
        report.VIPCoupon = this.result.getFloat("VIPCoupon");
        report.Waste = this.result.getFloat("Waste");
        reports.add(report);
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
      } catch (Exception localException2) {
      }
    }
    return reports;
  }

  private List<NcReceipt> initReceipt(List<MisCashDailyReport> reports)
  {
    List rst = new ArrayList();
    for (Iterator i = reports.iterator(); i.hasNext(); )
    {
      MisCashDailyReport report = (MisCashDailyReport)i.next();
      NcReceipt receipt = new NcReceipt();

      receipt.billdate = report.BizDate;
      receipt.vbillno = report.Store_Code + report.BizDate.replaceAll("-", "");
      receipt.ctbm = report.Store_Code;
      receipt.money = report.GrossSales;
      receipt.pk_skd = receipt.vbillno;
      rst.add(receipt);
    }

    return rst;
  }

  private void synchronizeReceipt(List<NcReceipt> list)
  {
    String sqlInsertReceipt = 
      "Insert Into nc_boh_ar(busidate, vbillcode, deptcode, jsmodel, currency, custcode, money, offmoney, disposed ,actiontype, isupdateallow, ts) Values('%s' ,'%s','%s',1,'CNY','',%s,1,0,0,'Y',TO_CHAR(sysdate,'yyyy-mm-dd hh24:mi:ss'))";
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.midConn.setAutoCommit(false);

      for (Iterator i = list.iterator(); i.hasNext(); )
      {
        NcReceipt receipt = (NcReceipt)i.next();

        this.sql = String.format(sqlInsertReceipt, new Object[] { receipt.billdate, receipt.vbillno, receipt.ctbm, Float.valueOf(receipt.money) });
        logger.info(this.sql);
        this.stmt = this.midConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();
      }
      this.midConn.commit();
      this.midConn.close();
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
        this.midConn.close();
      }
      catch (Exception localException3)
      {
      }
    }
  }
}