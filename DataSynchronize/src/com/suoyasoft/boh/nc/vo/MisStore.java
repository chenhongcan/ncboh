package com.suoyasoft.boh.nc.vo;

public class MisStore
{
  private String storeCode;
  private String storeName;
  private String is24Hours;
  private int status;

  public void setStoreCode(String value)
  {
    this.storeCode = value;
  }

  public String getStoreCode() {
    return this.storeCode;
  }

  public void setStoreName(String value)
  {
    this.storeName = value;
  }

  public String getStoreName() {
    return this.storeName; }

  public String getIs24Hours() {
    return this.is24Hours; }

  public void setIs24Hours(String is24Hours) {
    this.is24Hours = is24Hours; }

  public int getStatus() {
    return this.status; }

  public void setStatus(int status) {
    this.status = status;
  }
}