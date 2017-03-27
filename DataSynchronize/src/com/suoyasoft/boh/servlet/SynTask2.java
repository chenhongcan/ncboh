package com.suoyasoft.boh.servlet;

import com.suoyasoft.boh.nc.OrderSyn;
import com.suoyasoft.boh.nc.PurchaseSyn;
import com.suoyasoft.boh.nc.SaleOutOrderSyn;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class SynTask2 extends TimerTask
{
  protected static Logger logger = Logger.getLogger(SynTask2.class.getName());

  public void run()
  {
    logger.info("Task2=========开=始=同=步=========");

    logger.info("读取MIS的订单、退货单。保存到NC 销售订单");
    new OrderSyn().synData();

    logger.info("读取NC的销售出库单。保存到MIS的进、退货入库单");
    new SaleOutOrderSyn().synData();

    logger.info("读取MIS已确认进货单、退货单。保存到NC 采购订单");
    new PurchaseSyn().synData();

    logger.info("Task2=========同=步=结=束=========");
  }
}