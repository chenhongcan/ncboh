package com.suoyasoft.boh.servlet;

import com.suoyasoft.boh.nc.DataSyn;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class SynTask1 extends TimerTask
{
  protected static Logger logger = Logger.getLogger(SynTask1.class.getName());

  public void run()
  {
    logger.info("Task1=========开=始=同=步=========");

    new DataSyn().synData();

    logger.info("Task1=========同=步=结=束=========");
  }
}