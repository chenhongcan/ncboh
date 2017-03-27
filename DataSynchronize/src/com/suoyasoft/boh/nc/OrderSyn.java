package com.suoyasoft.boh.nc;

import com.suoyasoft.boh.nc.vo.NcSalesOrder;
import com.suoyasoft.boh.nc.vo.NcSalesOrderData;
import com.suoyasoft.boh.nc.vo.RSC_IMS_Form;
import com.suoyasoft.boh.nc.vo.RSC_IMS_Order;
import com.suoyasoft.boh.utils.MisConn;
import com.suoyasoft.boh.utils.NcMidConn;
import com.suoyasoft.boh.utils.Util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class OrderSyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  protected static Logger logger = Logger.getLogger(OrderSyn.class.getName());
  private List<String> okFormNos = new ArrayList();
  private String sql = "";
  private static final String C_ZS = "N003";
  private static final String T_ZS = "0";
  private static final String T_CG = "1";

  public void synData()
  {
    try
    {
      List formlist = loadMisForm();

      for (Iterator i = formlist.iterator(); i.hasNext(); )
      {
        RSC_IMS_Form form = (RSC_IMS_Form)i.next();

        List itemList = loadMisFormData(form);

        List ncForms = formatNcForms(form, itemList);

        if ((form.getFormType() == 2) || (form.getFormType() == 3))
        {
          insertPlusOrderForm(ncForms);
        }
        else {
          insertOrderForm(ncForms);
        }
      }
    }
    catch (Exception e)
    {
      logger.error(e.getMessage());
    }

    new Util().updateMisFormSynStatus(this.okFormNos);
  }

  private List<RSC_IMS_Form> loadMisForm()
  {
    List rst = new ArrayList();
    this.sql = "Select m.Store_Code, m.BizDate, m.FormNo, m2.type_code as DCCode, m.FormType, m.Comments,m1.formno as oldFormno From RSC_IMS_Form m left join  RSC_IMS_Form m1 on m.bizdate = m1.bizdate and m1.formtype =1 and m1.store_code = m.store_code Left join rsc_sys_data_type m2 on m2.type_id=38 and m2.status=1 Where m.FormType in(0,1, 2, 3, 8, 7) And m.NcSynStatus <> 1 And upper(subStr(m.Store_Code, 0, 2)) = 'CN' ";

    logger.info(this.sql);
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        RSC_IMS_Form form = new RSC_IMS_Form();
        form.setFormNo(this.result.getString("FormNo"));
        form.setBizDate(this.result.getString("BizDate"));
        form.setStore_Code(this.result.getString("Store_Code"));
        form.setFormType(this.result.getInt("FormType"));
        form.setDCCode(this.result.getString("DCCode"));
        form.setComments(this.result.getString("Comments"));
        form.setPreserved1(this.result.getString("oldFormno"));

        rst.add(form);
      }
      return rst;
    }
    catch (Exception e)
    {
      logger.error(this.sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      }
      catch (Exception localException5) {
      }
      try {
        this.misConn.close();
      } catch (Exception localException6) {
      }
    }
    return null;
  }

  private List<RSC_IMS_Order> loadMisFormData(RSC_IMS_Form form)
  {
    List rst = new ArrayList();

    this.sql = "Select O.Material_Code, M.Material_Name, O.AutoNo, O.OrderCount * NVL(m.checkscale, 1) as OrderCount, O.OrderUnit, O.Price, O.Comments, O.ReceiveDate, M.Category_Code From RSC_IMS_Order O Left join Rsc_Material M On M.Material_Code = O.Material_Code Where O.FormNo = '%s'  Order by M.Category_Code, O.Material_Code";

    this.sql = String.format(this.sql, new Object[] { form.getFormNo() });
    logger.info(this.sql);
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        RSC_IMS_Order order = new RSC_IMS_Order();
        order.setAutoNO(this.result.getInt("AutoNo"));
        order.setFormNO(form.getFormNo());
        order.setMaterial_Code(this.result.getString("Material_Code"));
        order.setMaterial_Name(this.result.getString("Material_Name"));
        order.setOrderCount(Float.valueOf(this.result.getFloat("OrderCount")));
        order.setComments((Util.isNull(this.result.getString("Comments")).booleanValue()) ? " " : this.result.getString("Comments"));
        order.setOrderUnit(this.result.getString("OrderUnit"));
        order.setPrice(Float.valueOf(this.result.getFloat("Price")));
        order.setCategoryCode(this.result.getString("Category_Code"));
        order.setReceiveDate((Util.isNull(this.result.getString("ReceiveDate")).booleanValue()) ? form.getBizDate() : this.result.getString("ReceiveDate"));
        rst.add(order);
      }
      return rst;
    }
    catch (Exception e)
    {
      logger.error(this.sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      }
      catch (Exception localException5) {
      }
      try {
        this.misConn.close();
      } catch (Exception localException6) {
      }
    }
    return rst;
  }

  private List<NcSalesOrder> formatNcForms(RSC_IMS_Form misForm, List<RSC_IMS_Order> itemList)
  {
    List ncForms = new ArrayList();
    String oldDcCode = "*";
    NcSalesOrder ncForm = null;

    for (Iterator item = itemList.iterator(); item.hasNext(); )
    {
      RSC_IMS_Order misFormData = (RSC_IMS_Order)item.next();

      if (!(oldDcCode.equals(misForm.getDCCode())))
      {
        ncForm = formatNcForm(misForm);
        ncForm.info1 = misForm.getDCCode();
        ncForm.orderData = new ArrayList();
        ncForms.add(ncForm);
        oldDcCode = misForm.getDCCode();
      }
      ncForm.orderData.add(formatNcFormData(misFormData));
    }

    return ncForms;
  }

  private NcSalesOrder formatNcForm(RSC_IMS_Form misForm)
  {
    NcSalesOrder order = new NcSalesOrder();

    order.pk_so_sale = misForm.getFormNo();
    order.misFormPK = misForm.getFormNo();
    order.dbilldate = misForm.getBizDate();
    order.djxh = misForm.getFormNo();
    order.ctbm = misForm.getStore_Code();
    order.sjly = misForm.getDCCode();
    if (misForm.getFormType() == 2)
    {
      order.info3 = "0";
    } else if (misForm.getFormType() == 3) {
      order.info3 = "1";
    }

    order.info2 = misForm.getPreserved1();

    switch (misForm.getFormType())
    {
    case 1:
      order.freqtypeflag = "1";
      break;
    case 7:
      order.freqtypeflag = "0";
    }

    return order;
  }

  private NcSalesOrderData formatNcFormData(RSC_IMS_Order misFormData)
  {
    NcSalesOrderData ncFormData = new NcSalesOrderData();

    ncFormData.pk_so_sale_b = String.valueOf(misFormData.getAutoNO());

    ncFormData.pk_so_sale = misFormData.getFormNO();

    ncFormData.invcode = misFormData.getMaterial_Code();

    ncFormData.Invname = misFormData.getMaterial_Name();

    ncFormData.npacknumber = misFormData.getOrderCount();

    ncFormData.ddeliverdate = misFormData.getReceiveDate();

    ncFormData.measname = misFormData.getOrderUnit();

    ncFormData.categoryCode = misFormData.getCategoryCode();

    ncFormData.frownote = misFormData.getComments();

    return ncFormData;
  }

  private List<NcSalesOrder> groupNcForms(List<NcSalesOrder> ncForms)
  {
    List rstForms = new ArrayList();
    List keys = new ArrayList();
    NcSalesOrder ncForm = null;

    for (Iterator form = ncForms.iterator(); form.hasNext(); )
    {
      ncForm = (NcSalesOrder)form.next();
      for (Iterator item = ncForm.orderData.iterator(); item.hasNext(); )
      {
        NcSalesOrderData ncItem = (NcSalesOrderData)item.next();

        if (ncItem.categoryCode.equals("N003"))
        {
          if (!(keys.contains("0")))
          {
            ncForm = ncForm.clone("0");
            rstForms.add(ncForm);
            keys.add("0");
          }

          if (!(ncForm.type.equals("0")))
          {
            ncForm = getForm("0", rstForms);
          }
          ncItem.pk_so_sale = ncForm.pk_so_sale;
          ncForm.orderData.add(ncItem);
        }
        else
        {
          if (ncItem.categoryCode.equals("N003"))
            continue;
          if (!(keys.contains("1")))
          {
            ncForm = ncForm.clone("1");
            rstForms.add(ncForm);
            keys.add("1");
          }
          if (!(ncForm.type.equals("1")))
          {
            ncForm = getForm("1", rstForms);
          }
          ncItem.pk_so_sale = ncForm.pk_so_sale;
          ncForm.orderData.add(ncItem);
        }
      }
    }

    return rstForms;
  }

  private void insertOrderForm(List<NcSalesOrder> ncForms)
  {
    String sqlInsForm = 
      "Insert Into nc_boh_requireapp(pk_requireapp, dapplydate, vbillcode, deptcode, freqtypeflag, invcorp, disposed, actiontype, isupdateallow, ts)Values('%s', '%s', '%s', '%s', '%s', '%s', 0, 0, 'Y', TO_CHAR(sysdate,'yyyy-mm-dd hh24:mi:ss'))";

    String sqlInsFormDetail = 
      "Insert Into nc_boh_requireapp_b(pk_requireapp_b, pk_requireapp, invcode, invname, nnumber, measname, demanddate, disposed, actiontype, isupdateallow, ts)Values('%s', '%s', '%s', '%s', %s, '%s', '%s', 0, 0, 'Y', TO_CHAR(sysdate,'yyyy-mm-dd hh24:mi:ss'))";
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.midConn.setAutoCommit(false);

      for (Iterator ncForm = ncForms.iterator(); ncForm.hasNext(); )
      {
        NcSalesOrder form = (NcSalesOrder)ncForm.next();
        try
        {
          this.sql = String.format(sqlInsForm, new Object[] { form.pk_so_sale, form.dbilldate, form.pk_so_sale, form.ctbm, form.freqtypeflag, form.sjly });
          logger.info(this.sql);
          this.stmt = this.midConn.prepareStatement(this.sql);
          this.stmt.execute();
          this.stmt.close();

          for (Iterator data = form.orderData.iterator(); data.hasNext(); )
          {
            NcSalesOrderData body = (NcSalesOrderData)data.next();
            this.sql = String.format(sqlInsFormDetail, new Object[] { body.pk_so_sale_b, body.pk_so_sale, body.invcode, body.Invname, body.npacknumber, "CNY", body.ddeliverdate });
            logger.info(this.sql);
            this.stmt = this.midConn.prepareStatement(this.sql);
            this.stmt.execute();
            this.stmt.close();
          }

          this.midConn.commit();
          if (!(this.okFormNos.contains(form.misFormPK)))
          {
            this.okFormNos.add(form.misFormPK);
          }
        } catch (SQLException e) {
          logger.error(this.sql);
          logger.error("发送 " + form.pk_so_sale + " 数据至NC失败..., 详情: " + e.getMessage(), e);
        }
      }
    }
    catch (Exception e)
    {
      try
      {
        this.midConn.rollback();
      }
      catch (Exception localException1) {
      }
      logger.error(this.sql);
      logger.error(e.getMessage(), e);
    }
    finally
    {
      try
      {
        this.stmt.close();
      }
      catch (Exception localException4) {
      }
      try {
        this.midConn.close();
      }
      catch (Exception localException5) {
      }
    }
  }

  private NcSalesOrder getForm(String type, List<NcSalesOrder> rstForms) {
    NcSalesOrder form = null;
    for (Iterator order = rstForms.iterator(); order.hasNext(); )
    {
      form = (NcSalesOrder)order.next();
      if (form.type.equals(type))
      {
        return form;
      }
    }
    return null;
  }

  private void insertPlusOrderForm(List<NcSalesOrder> ncForms)
  {
    String sqlInsForm = 
      "Insert Into nc_boh_require2order(dapplydate, vbillcode, oldvbillcode, Deptcode, freqtypeflag, Pk_requireapp, disposed, Ordertype, isupdateallow,invcorp,ISFIRSTORDER, ts) Values ('%s', '%s', '%s', '%s', 0, '%s', 0, '%s', 'Y', '%s', 'N', TO_CHAR(sysdate,'yyyy-mm-dd hh24:mi:ss'))";

    String sqlInsFormDetail = 
      "Insert Into nc_boh_require2order_b(PK_REQUIREAPP_B, Pk_requireapp, invcode, invname, nnumber, measname, demanddate, disposed, actiontype, isupdateallow, ts) Values ('%s', '%s', '%s', '%s', %s, '%s', '%s', 0, 0, 'Y', TO_CHAR(sysdate,'yyyy-mm-dd hh24:mi:ss'))";
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.midConn.setAutoCommit(false);

      for (Iterator ncForm = ncForms.iterator(); ncForm.hasNext(); )
      {
        NcSalesOrder form = (NcSalesOrder)ncForm.next();
        try
        {
          this.sql = String.format(sqlInsForm, new Object[] { form.dbilldate, form.pk_so_sale, form.info2, form.ctbm, form.pk_so_sale, form.info3, form.sjly });
          logger.info(this.sql);
          this.stmt = this.midConn.prepareStatement(this.sql);
          this.stmt.execute();
          this.stmt.close();

          int num = 1;

          for (Iterator data = form.orderData.iterator(); data.hasNext(); )
          {
            NcSalesOrderData body = (NcSalesOrderData)data.next();
            this.sql = String.format(sqlInsFormDetail, new Object[] { Integer.valueOf(num), body.pk_so_sale, body.invcode, body.Invname, body.npacknumber, body.measname, body.ddeliverdate });
            logger.info(this.sql);
            this.stmt = this.midConn.prepareStatement(this.sql);
            this.stmt.execute();
            this.stmt.close();

            ++num;
          }

          this.midConn.commit();
          if (!(this.okFormNos.contains(form.misFormPK)))
          {
            this.okFormNos.add(form.misFormPK);
          }
        } catch (SQLException e) {
          logger.error("发送加减单 " + form.misFormPK + " 数据至NC失败..., 详情: " + e.getMessage(), e);
        }
      }
    }
    catch (Exception e)
    {
      try
      {
        this.midConn.rollback();
      }
      catch (Exception localException1) {
      }
      logger.error(this.sql);
      logger.error(e.getMessage(), e);
    }
    finally
    {
      try
      {
        this.stmt.close();
      }
      catch (Exception localException4) {
      }
      try {
        this.midConn.close();
      }
      catch (Exception localException5)
      {
      }
    }
  }
}