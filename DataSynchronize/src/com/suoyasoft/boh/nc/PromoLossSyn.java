package com.suoyasoft.boh.nc;

import com.suoyasoft.boh.nc.vo.MisLossData;
import com.suoyasoft.boh.nc.vo.NcReceipt;
import com.suoyasoft.boh.nc.vo.NcReceiptBody;
import com.suoyasoft.boh.nc.vo.NcReceiptItem;
import com.suoyasoft.boh.utils.MisConn;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class PromoLossSyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String sql = "";

  protected static Logger logger = Logger.getLogger(PromoLossSyn.class.getName());

  public void synData(String bizDate, String beginDate, String endDate)
  {
    List datas = loadLossData(beginDate, endDate);

    List data = initData(datas);

    synchronizeData(data);
  }

  private List<MisLossData> loadLossData(String beginDate, String endDate)
  {
    List datas = new ArrayList();

    this.sql = "Select Store_Code, Sum(LossCount * Price) as LossAmount, Sum(PromoCount * Price) as PromoAmount From RSC_IMS_MaterialStockReport Where BizDate between '%s' and '%s' And CheckCount = 1 And (LossCount + PromoCount > 0) Group by Store_Code";

    this.sql = String.format(this.sql, new Object[] { beginDate, endDate });
    logger.debug(this.sql);
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        MisLossData data = new MisLossData();
        data.BizDate = endDate;
        data.StoreCode = this.result.getString("Store_Code");
        data.LossAmount = this.result.getFloat("LossAmount");
        data.PromoAmount = this.result.getFloat("PromoAmount");
        datas.add(data);
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
    return datas;
  }

  private List<NcReceipt> initData(List<MisLossData> reports)
  {
    List rst = new ArrayList();
    for (Iterator i = reports.iterator(); i.hasNext(); )
    {
      MisLossData data = (MisLossData)i.next();

      NcReceipt receipt = new NcReceipt();
      receipt.ctbm = data.StoreCode;
      receipt.billdate = data.BizDate;

      receipt.sjly = "MIS";
      receipt.vbillno = receipt.pk_skd;
      receipt.body = initDataBody(receipt.pk_skd, data);

      rst.add(receipt);
    }

    return rst;
  }

  private List<NcReceiptBody> initDataBody(String pk, MisLossData data)
  {
    List itemList = new ArrayList();
    itemList.add(new NcReceiptItem("640100102", "LossAmount", "1006", "成本-丢弃食品"));
    itemList.add(new NcReceiptItem("640100103", "PromoAmount", "1006", "成本-赠送食品"));

    List rst = new ArrayList();
    for (Integer i = Integer.valueOf(0); i.intValue() < itemList.size(); i = Integer.valueOf(i.intValue() + 1))
    {
      NcReceiptItem item = (NcReceiptItem)itemList.get(i.intValue());

      NcReceiptBody body = new NcReceiptBody();
      Float val = Float.valueOf(0.0F);
      try
      {
        val = Float.valueOf(data.getClass().getField(item.getItemName()).getFloat(data));
      } catch (Exception localException) {
      }
      body.ybje = val.floatValue();
      if (body.ybje == 0.0F)
        continue;
      body.pk_skd = pk;
      body.pk_skd_b = pk + "00" + i.toString();
      body.bz = "CNY";
      body.hl = 1.0F;
      body.bbje = (body.ybje * body.hl);
      body.jy = item.getSummary();
      body.szxm = item.getItemCode();
      body.jsfs = item.getSettlementType();

      rst.add(body);
    }

    return rst;
  }

  private void synchronizeData(List<NcReceipt> list)
  {
  }
}