package com.suoyasoft.boh.nc.vo;

public class MisCompanyStore
{
  private String CompanyID;
  private String Store_Code;
  private String CompanyName;

  public String getCompanyID()
  {
    return this.CompanyID;
  }

  public void setCompanyID(String companyID) {
    this.CompanyID = companyID;
  }

  public String getStore_Code() {
    return this.Store_Code;
  }

  public void setStore_Code(String store_Code) {
    this.Store_Code = store_Code;
  }

  public String getCompanyName() {
    return this.CompanyName;
  }

  public void setCompanyName(String companyName) {
    this.CompanyName = companyName;
  }
}