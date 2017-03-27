package com.suoyasoft.boh.nc.vo;

public class MisMaterial
{
  private String MaterialCode;
  private String MaterialName;
  private String Spec;
  private String OrderUnit;
  private String CheckUnit;
  private float CheckScale;
  private float orderScale;
  private float Price;
  private int Status;

  public void setMaterialCode(String value)
  {
    this.MaterialCode = value;
  }

  public String getMaterialCode() {
    return this.MaterialCode;
  }

  public void setMaterialName(String value)
  {
    this.MaterialName = value;
  }

  public String getMaterialName() {
    return this.MaterialName;
  }

  public void setSpec(String value)
  {
    if ((value == null) || (value.equals("null")))
      this.Spec = " ";
    else
      this.Spec = value;
  }

  public String getSpec() {
    return this.Spec;
  }

  public void setOrderUnit(String value)
  {
    this.OrderUnit = value;
  }

  public String getOrderUnit() {
    return this.OrderUnit;
  }

  public void setStatus(int value)
  {
    this.Status = value;
  }

  public int getStatus() {
    return this.Status; }

  public String getCheckUnit() {
    return this.CheckUnit; }

  public void setCheckUnit(String checkUnit) {
    this.CheckUnit = checkUnit; }

  public float getCheckScale() {
    return this.CheckScale; }

  public void setCheckScale(float checkScale) {
    this.CheckScale = checkScale;
  }

  public float getOrderScale() {
    return this.orderScale;
  }

  public void setOrderScale(float orderScale) {
    this.orderScale = orderScale;
  }

  public float getPrice() {
    return this.Price; }

  public void setPrice(float price) {
    this.Price = price;
  }
}