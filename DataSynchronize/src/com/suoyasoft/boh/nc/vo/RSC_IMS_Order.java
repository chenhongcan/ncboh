package com.suoyasoft.boh.nc.vo;

public class RSC_IMS_Order
{
  private String FormNO;
  private String Material_Code;
  private String Material_Name;
  private int AutoNO;
  private Float OrderCount;
  private String OrderUnit;
  private Float Price;
  private String Comments;
  private String ReceiveDate;
  private String dcCode;
  private String categoryCode;

  public String getCategoryCode()
  {
    return this.categoryCode;
  }

  public void setCategoryCode(String code) {
    this.categoryCode = code;
  }

  public String getDcCode()
  {
    return this.dcCode;
  }

  public void setDcCode(String code) {
    this.dcCode = code;
  }

  public String getFormNO()
  {
    return this.FormNO;
  }

  public void setFormNO(String formNO)
  {
    this.FormNO = formNO;
  }

  public String getMaterial_Code()
  {
    return this.Material_Code;
  }

  public void setMaterial_Code(String material_Code)
  {
    this.Material_Code = material_Code;
  }

  public int getAutoNO()
  {
    return this.AutoNO;
  }

  public void setAutoNO(int autoNO)
  {
    this.AutoNO = autoNO;
  }

  public Float getOrderCount()
  {
    return this.OrderCount;
  }

  public void setOrderCount(Float orderCount)
  {
    this.OrderCount = orderCount;
  }

  public String getOrderUnit()
  {
    return this.OrderUnit;
  }

  public void setOrderUnit(String orderUnit)
  {
    this.OrderUnit = orderUnit;
  }

  public Float getPrice()
  {
    return this.Price;
  }

  public void setPrice(Float price)
  {
    this.Price = price;
  }

  public String getComments()
  {
    return this.Comments;
  }

  public void setComments(String comments)
  {
    this.Comments = comments;
  }

  public String getReceiveDate()
  {
    return this.ReceiveDate;
  }

  public void setReceiveDate(String receiveDate)
  {
    this.ReceiveDate = receiveDate; }

  public String getMaterial_Name() {
    return this.Material_Name; }

  public void setMaterial_Name(String material_Name) {
    this.Material_Name = material_Name;
  }
}