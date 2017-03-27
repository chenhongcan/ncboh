package com.suoyasoft.boh.servlet;

import com.suoyasoft.boh.nc.MonthlyUseSyn;
import com.suoyasoft.boh.nc.ReceiptSyn;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class SynTask3 extends TimerTask
{
  public static final int RUNDAY = 2;
  private int currentDay = 0;
  private String currentDate = "";
  private String beginDate = "";
  private String endDate = "";
  protected static Logger logger = Logger.getLogger(SynTask3.class.getName());

  private void calcDate()
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    Calendar cal = Calendar.getInstance();
    this.currentDay = cal.get(5);
    this.currentDate = sdf.format(cal.getTime());

    cal.set(5, 1);

    cal.add(5, -1);
    this.endDate = sdf.format(cal.getTime());

    cal.set(5, 1);
    this.beginDate = sdf.format(cal.getTime());
  }

  public void run()
  {
    calcDate();

    if (this.currentDay != 2) return;

    logger.info("Task3=========开=始=同=步=========");

    logger.info("同步 BOH收款单 到 NC收款单");
    new ReceiptSyn().synData(this.currentDate, this.beginDate, this.endDate);

    logger.info("同步 月物资消耗 到 NC餐厅每月盘点表");
    new MonthlyUseSyn().synData(this.currentDate, this.beginDate, this.endDate);

    logger.info("Task3=========同=步=结=束=========");
  }
}