package com.suoyasoft.boh.servlet;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.Timer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.log4j.Logger;

public class SynService5 extends HttpServlet
{
  protected static Logger logger = Logger.getLogger(SynService5.class.getName());
  private static final int HH = 18;
  private static final int MM = 10;
  private static final int SS = 0;
  private static final long DelayTime = 10000L;
  private Timer _timer = new Timer();

  public void init()
    throws ServletException
  {
    super.init();

    Calendar first = Calendar.getInstance();
    first.set(11, 18);
    first.set(12, 10);
    first.set(13, 0);
    System.out.println(first.getTime());

    this._timer.schedule(new SynTask5(), first.getTime(), 10000L);

    logger.info("SynService5成功启动!");
  }

  public void destroy()
  {
    super.destroy();

    this._timer.cancel();
    this._timer = null;

    logger.info("SynService5成功关闭!");
  }
}