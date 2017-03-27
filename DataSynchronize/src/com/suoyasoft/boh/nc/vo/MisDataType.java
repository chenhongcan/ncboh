package com.suoyasoft.boh.nc.vo;

public class MisDataType
{
  private String dictCode;
  private int typeID;
  private String typeCode;
  private String typeName;
  private int status;
  private String joinID;

  public String getJoinID()
  {
    return this.joinID;
  }

  public void setJoinID(String joinID) {
    this.joinID = joinID;
  }

  public void setDictCode(String value) {
    this.dictCode = value;
  }

  public String getDictCode() {
    return this.dictCode;
  }

  public void setTypeID(int value)
  {
    this.typeID = value;
  }

  public int getTypeID() {
    return this.typeID;
  }

  public void setTypeCode(String value)
  {
    this.typeCode = value;
  }

  public String getTypeCode() {
    return this.typeCode;
  }

  public void setTypeName(String value)
  {
    this.typeName = value;
  }

  public String getTypeName() {
    return this.typeName;
  }

  public void setStatus(int value)
  {
    this.status = value;
  }

  public int getStatus() {
    return this.status;
  }
}