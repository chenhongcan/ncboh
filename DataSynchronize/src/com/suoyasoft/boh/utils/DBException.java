package com.suoyasoft.boh.utils;

public class DBException extends RuntimeException
{
  private static final long serialVersionUID = 1067511273076179967L;
  public static final int ERR_LOAD_RESOURCE = 8001;
  public static final int ERR_INIT_DBDRIVER = 8002;
  public static final int ERR_CLOSE_CONNECTION = 8003;
  private int errCode;

  public DBException(Throwable cause)
  {
    super(cause);
  }

  public DBException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public DBException(String message)
  {
    super(message);
  }

  public DBException()
  {
  }

  public DBException(int errCode)
  {
    this.errCode = errCode;
  }

  public DBException(int errCode, String message)
  {
    this.errCode = errCode;
  }

  public DBException(int errCode, Throwable cause)
  {
    super(cause);
    this.errCode = errCode;
  }

  public void SetErrCode(int errcode)
  {
    this.errCode = errcode;
  }

  public int GetErrCode()
  {
    return this.errCode;
  }
}