package com.suoyasoft.boh.servlet;

import com.suoyasoft.boh.zy.MenuSyn;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class SynTask4 extends TimerTask
{
  protected static Logger logger = Logger.getLogger(SynTask4.class.getName());

  public void run()
  {
    logger.info("Task4=========开=始=同=步=========");

    logger.info("测试同步泽阳菜单");
    new MenuSyn().synData();

    logger.info("Task4=========同=步=结=束=========");
  }
}