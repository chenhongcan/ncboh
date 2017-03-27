package com.suoyasoft.boh.servlet;

import java.util.Calendar;
import java.util.Timer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.log4j.Logger;

public class SynService4 extends HttpServlet
{
  protected static Logger logger = Logger.getLogger(SynService4.class.getName());
  private static final int MINUTE = 30;
  private static final long period = 1800000L;
  private Timer _timer = new Timer();

  public void init()
    throws ServletException
  {
    super.init();

    Calendar cal = Calendar.getInstance();
    int min = cal.get(12) % 30;
    min = 30 - min;

    cal.add(12, min);
    cal.set(13, 0);

    this._timer.schedule(new SynTask4(), cal.getTime(), 1800000L);

    logger.info("SynService4成功启动!");
  }

  public void destroy()
  {
    super.destroy();

    this._timer.cancel();
    this._timer = null;

    logger.info("SynService4成功关闭!");
  }
}