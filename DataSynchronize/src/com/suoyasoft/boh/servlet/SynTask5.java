package com.suoyasoft.boh.servlet;

import com.suoyasoft.boh.pos.PosSyn;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class SynTask5 extends TimerTask
{
  protected static Logger logger = Logger.getLogger(SynTask5.class.getName());

  public void run()
  {
    logger.info("Task5=========��=ʼ=ͬ=��=========");

    logger.info("��������POS�˲˵�");
    new PosSyn().synData();

    logger.info("Task5=========ͬ=��=��=��=========");
  }
}