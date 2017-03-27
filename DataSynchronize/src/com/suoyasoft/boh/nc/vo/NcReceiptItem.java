package com.suoyasoft.boh.nc.vo;

public class NcReceiptItem
{
  private String itemCode;
  private String itemName;
  private String summary;
  private String settlementType;

  public NcReceiptItem(String itemCode, String itemName, String settlementType, String summary)
  {
    this.itemCode = itemCode;
    this.itemName = itemName;
    this.settlementType = settlementType;
    this.summary = summary;
  }

  public String getItemCode()
  {
    return this.itemCode;
  }

  public String getItemName()
  {
    return this.itemName;
  }

  public String getSettlementType()
  {
    return this.settlementType;
  }

  public String getSummary()
  {
    return this.summary;
  }
}