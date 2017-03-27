package com.suoyasoft.boh.nc.vo;

public class MisMaterialPrice
{
  private String BizDate;
  private Float Price;
  private String Material_Code;
  private String Material_Name;
  private String Unit;
  private String CompanyID;
  private String ts;

  public String getTs()
  {
    return this.ts;
  }

  public void setTs(String ts) {
    this.ts = ts;
  }

  public String getBizDate() {
    return this.BizDate;
  }

  public void setBizDate(String bizDate) {
    this.BizDate = bizDate;
  }

  public String getMaterial_Code() {
    return this.Material_Code;
  }

  public void setMaterial_Code(String material_Code) {
    this.Material_Code = material_Code;
  }

  public String getMaterial_Name() {
    return this.Material_Name;
  }

  public void setMaterial_Name(String material_Name) {
    this.Material_Name = material_Name;
  }

  public String getUnit() {
    return this.Unit;
  }

  public void setUnit(String unit) {
    this.Unit = unit;
  }

  public String getCompanyID() {
    return this.CompanyID;
  }

  public void setCompanyID(String companyID) {
    this.CompanyID = companyID;
  }

  public Float getPrice() {
    return this.Price;
  }

  public void setPrice(Float price) {
    this.Price = price;
  }
}