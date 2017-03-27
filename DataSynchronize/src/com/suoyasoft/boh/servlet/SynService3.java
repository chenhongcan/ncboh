package com.suoyasoft.boh.servlet;

import java.util.Calendar;
import java.util.Timer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.log4j.Logger;

public class SynService3 extends HttpServlet
{
  protected static Logger logger = Logger.getLogger(SynService3.class.getName());
  private static final int HH = 3;
  private static final int MM = 10;
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
    secondSchedTime.set(11, 3);
    secondSchedTime.set(12, 10);
    secondSchedTime.set(13, 0);

    this._timer.schedule(new SynTask3(), secondSchedTime.getTime(), 86400000L);
    logger.info("SynService3成功启动!");
  }

  public void destroy()
  {
    super.destroy();

    this._timer.cancel();
    this._timer = null;

    logger.info("SynService3成功关闭!");
  }
}