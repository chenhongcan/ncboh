package com.suoyasoft.boh.servlet;

import java.util.Calendar;
import java.util.Timer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.log4j.Logger;

public class SynService1 extends HttpServlet
{
  protected static Logger logger = Logger.getLogger(SynService1.class.getName());
  private static final int HH = 6;
  private static final int MM = 20;
  private static final int SS = 0;
  private static final long period = 86400000L;
  private Timer _timer = new Timer();

  public void init()
    throws ServletException
  {
    super.init();
    Calendar firstSchedTime = Calendar.getInstance();
    firstSchedTime.add(13, 30);

    Calendar secondSchedTime = Calendar.getInstance();
    secondSchedTime.set(11, 6);
    secondSchedTime.set(12, 20);
    secondSchedTime.set(13, 0);
    this._timer.schedule(new SynTask1(), firstSchedTime.getTime());
    this._timer.schedule(new SynTask1(), secondSchedTime.getTime(), 86400000L);
    logger.info("SynService1成功启动!");
  }

  public void destroy()
  {
    super.destroy();

    this._timer.cancel();
    this._timer = null;

    logger.info("SynService1成功关闭!");
  }
}