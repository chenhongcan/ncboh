package com.suoyasoft.boh.servlet;

import java.util.Calendar;
import java.util.Timer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.log4j.Logger;

public class SynService2 extends HttpServlet
{
  protected static Logger logger = Logger.getLogger(SynService2.class.getName());
  private static final int MINUTE = 15;
  private static final long period = 900000L;
  private Timer _timer = new Timer();

  public void init()
    throws ServletException
  {
    super.init();

    Calendar firstSchedTime = Calendar.getInstance();
    firstSchedTime.add(13, 30);

    Calendar cal = Calendar.getInstance();
    int min = cal.get(12) % 15;
    min = 15 - min;

    cal.add(12, min);
    cal.set(13, 0);

    this._timer.schedule(new SynTask2(), firstSchedTime.getTime());
    this._timer.schedule(new SynTask2(), cal.getTime(), 900000L);
    logger.info("SynService2成功启动!");
  }

  public void destroy()
  {
    super.destroy();

    this._timer.cancel();
    this._timer = null;

    logger.info("SynService2成功关闭!");
  }
}