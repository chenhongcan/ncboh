package com.suoyasoft.boh.nc.vo;

import java.util.ArrayList;
import java.util.List;

public class NcSalesOrder
{
  public String pk_so_sale;
  public String ctbm;
  public String djxh;
  public String dbilldate;
  public String freqtypeflag;
  public String sjly;
  public String pstatus;
  public String info2;
  public String ywpch;
  public String fsrxm;
  public String fssj;
  public String vnote;
  public String ptime;
  public String pinfo;
  public String ts;
  public String info1;
  public String info3;
  public List<NcSalesOrderData> orderData;
  public String misFormPK;
  public String type;

  public NcSalesOrder clone(String type)
  {
    NcSalesOrder order = new NcSalesOrder();
    order.type = type;
    order.ctbm = this.ctbm;
    order.dbilldate = this.dbilldate;
    order.djxh = this.djxh;
    order.fsrxm = this.fsrxm;
    order.fssj = this.fssj;
    order.info1 = this.info1;
    order.info2 = this.info2;
    order.info3 = this.info3;
    order.pinfo = this.pinfo;
    order.pstatus = this.pstatus;
    order.ptime = this.ptime;
    order.sjly = this.sjly;
    order.ts = this.ts;
    order.vnote = this.vnote;
    order.ywpch = this.ywpch;
    order.misFormPK = this.misFormPK;
    order.freqtypeflag = this.freqtypeflag;
    order.pk_so_sale = order.misFormPK + this.info1 + type;
    order.orderData = new ArrayList();
    return order;
  }
}