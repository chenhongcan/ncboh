package com.suoyasoft.boh.nc.vo;

public class RSC_MaterialAccount
{
  private String store_Code;
  private String bizDate;
  private Float price;
  private String material_Code;
  private Float count;
  private String materialName;
  private String unit;
  private Float amount;

  public String getStore_Code()
  {
    return this.store_Code;
  }

  public void setStore_Code(String store_Code) {
    this.store_Code = store_Code;
  }

  public String getBizDate() {
    return this.bizDate;
  }

  public void setBizDate(String bizDate) {
    this.bizDate = bizDate;
  }

  public Float getPrice() {
    return this.price;
  }

  public void setPrice(Float price) {
    this.price = price;
  }

  public String getMaterial_Code() {
    return this.material_Code;
  }

  public void setMaterial_Code(String material_Code) {
    this.material_Code = material_Code;
  }

  public Float getCount() {
    return this.count;
  }

  public void setCount(Float count) {
    this.count = count;
  }

  public String getMaterialName() {
    return this.materialName;
  }

  public void setMaterialName(String materialName) {
    this.materialName = materialName;
  }

  public String getUnit() {
    return this.unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public Float getAmount() {
    return this.amount;
  }

  public void setAmount(Float amount) {
    this.amount = amount;
  }
}