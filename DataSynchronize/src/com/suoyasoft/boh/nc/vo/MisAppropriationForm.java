package com.suoyasoft.boh.nc.vo;

public class MisAppropriationForm
{
  private String FormNo;
  private String FromStoreCode;
  private String ToStoreCode;
  private String BizDate;
  private int Status;
  private int FormType;

  public String getFormNo()
  {
    return this.FormNo;
  }

  public void setFormNo(String formNo)
  {
    this.FormNo = formNo;
  }

  public String getFromStoreCode()
  {
    return this.FromStoreCode;
  }

  public void setFromStoreCode(String fromStoreCode)
  {
    this.FromStoreCode = fromStoreCode;
  }

  public String getToStoreCode()
  {
    return this.ToStoreCode;
  }

  public void setToStoreCode(String toStoreCode)
  {
    this.ToStoreCode = toStoreCode;
  }

  public String getBizDate()
  {
    return this.BizDate;
  }

  public void setBizDate(String bizDate)
  {
    this.BizDate = bizDate;
  }

  public int getStatus()
  {
    return this.Status;
  }

  public void setStatus(int status)
  {
    this.Status = status;
  }

  public int getFormType()
  {
    return this.FormType;
  }

  public void setFormType(int formType)
  {
    this.FormType = formType;
  }
}