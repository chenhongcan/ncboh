package com.suoyasoft.boh.nc;

import com.suoyasoft.boh.nc.vo.NcPurchaseOrder;
import com.suoyasoft.boh.nc.vo.NcPurchaseOrderBody;
import com.suoyasoft.boh.nc.vo.RSC_IMS_Form;
import com.suoyasoft.boh.nc.vo.RSC_IMS_Receive;
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

public class PurchaseSyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String sql = "";

  protected static Logger logger = Logger.getLogger(PurchaseSyn.class.getName());

  public void synData()
  {
    try
    {
      List<RSC_IMS_Form> formlist = loadMisForm();
      for (Iterator i = formlist.iterator(); i.hasNext(); )
      {
        RSC_IMS_Form form = (RSC_IMS_Form)i.next();
        logger.info("开始同步确认单: " + form.getFormNo());

        NcPurchaseOrder ncForm = formatOrderForm(form);
        List<RSC_IMS_Receive> misList = loadMisFormData(form.getFormNo());
        logger.info("确认单 " + form.getFormNo() + " 明细条数: " + misList.size());
        if (misList.size() != 0) {
          List ncList = formatPurchaseOrderData(misList);
          insertOrderForm(ncForm, ncList);
        }
      }
    }
    catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  private List<RSC_IMS_Form> loadMisForm()
  {
    List<RSC_IMS_Form> rst = new ArrayList();

    this.sql = 
      "Select Store_Code,BizDate,FormNo,DCCode,FormType,Comments,Preserved5,Preserved7,Preserved8 From RSC_IMS_Form where FormType in(5,9) And NcSynStatus <> 1  and Preserved5 in('5','6')";

    logger.debug(this.sql);
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        RSC_IMS_Form form = new RSC_IMS_Form();
        form.setStore_Code(this.result.getString("Store_Code"));
        form.setBizDate(this.result.getString("BizDate"));
        form.setFormNo(this.result.getString("FormNo"));
        form.setFormType(this.result.getInt("FormType"));
        form.setComments(this.result.getString("Comments"));
        form.setPreserved5(this.result.getString("Preserved5"));
        form.setPreserved7(this.result.getString("Preserved7"));
        form.setPreserved8(this.result.getString("Preserved8")); //edit by chc
        rst.add(form);
      }
      return rst;
    }
    catch (Exception e)
    {
      logger.error(this.sql);
      logger.error(e.getMessage(), e);
    }
    finally
    {
      try
      {
        this.stmt.close();
        this.misConn.close();
      } catch (Exception localException3) {
      }
    }
    return null;
  }

  private List<RSC_IMS_Receive> loadMisFormData(String formNo)
  {
    List rst = new ArrayList();

    this.sql = 
      "Select R.FormNO,R.Material_Code,R.AutoNo,R.ReceiveCount,R.ReceiveUnit,R.Comments,M.Material_Name From RSC_IMS_Receive R Left join Rsc_Material M On M.Material_Code = R.Material_Code Where FormNo='%s' and receiveCount <> 0  union all Select R.FormNO,R.Material_Code,R.AutoNo,R.ReceiveCount,R.OrderUnit as ReceiveUnit,R.Comments,M.Material_Name From rsc_ims_order R Left join Rsc_Material M On M.Material_Code = R.Material_Code Where FormNo='%s' and receiveCount <> 0 ";

    this.sql = String.format(this.sql, new Object[] { formNo, formNo });
   // logger.info(this.sql);
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        RSC_IMS_Receive order = new RSC_IMS_Receive();
        order.setAutoNo(this.result.getInt("AutoNo"));
        order.setFormNO(this.result.getString("FormNO"));
        order.setMaterial_Code(this.result.getString("Material_Code"));
        order.setMaterial_Name(this.result.getString("Material_Name"));
        order.setReceiveCount(Float.valueOf(this.result.getFloat("ReceiveCount")));
        order.setReceiveUnit(this.result.getString("ReceiveUnit"));
        order.setComments(this.result.getString("Comments"));

        rst.add(order);
      }
      return rst;
    }
    catch (Exception e)
    {
      logger.error(this.sql);
      logger.error(e.getMessage(), e);
    }
    finally
    {
      try
      {
        this.stmt.close();
        this.misConn.close();
      } catch (Exception localException3) {
      }
    }
    return rst;
  }

  private NcPurchaseOrder formatOrderForm(RSC_IMS_Form form)
  {
    NcPurchaseOrder order = new NcPurchaseOrder();
    order.pk_cg_order_h = form.getFormNo(); //"CYJH"+入库单号
    order.orderdate = form.getBizDate();
    order.cdptcode = form.getStore_Code();
    if ( Integer.parseInt(form.getPreserved5()) == 5 ){     //直送
      order.vbillcode = form.getPreserved7();  //订货单号
    }else if(Integer.parseInt(form.getPreserved5()) == 6 ){   //非直送
    	order.vbillcode = form.getFormNo();  //入库单号
    }
    order.DCCODE = form.getPreserved8();  //配送业务是NC出库单号，直送业务是入库单号
    if ( Integer.parseInt(form.getPreserved5()) == 5 ){
        order.NCDCCODE = form.getPreserved8();
    }else if(Integer.parseInt(form.getPreserved5()) == 6){
    	 order.NCDCCODE = form.getPreserved8().substring(2);
    }
    if (form.getFormType() == 5 || form.getFormType() == 9 )
    {
      order.Breturn = 'N';
    }
    else if (form.getFormType() == 6)
    {
      order.Breturn = 'Y';
    }

    switch (Integer.parseInt(form.getPreserved5()))
    {
    case 1:
    case 2:
      order.freqtypeFlag = "1";  //常规订单
      break;
    case 5:
      order.freqtypeFlag = "0";  //直送订单
      break;
    case 6:
        order.freqtypeFlag = "1";  //常规订单
        break;  
    case 3:
    case 4:
    default:
      order.freqtypeFlag = "9";  //位置
    }

    return order;
  }

  private List<NcPurchaseOrderBody> formatPurchaseOrderData(List<RSC_IMS_Receive> dataSource)
  {
    List rst = new ArrayList();

    for (Iterator i = dataSource.iterator(); i.hasNext(); )
    {
      RSC_IMS_Receive order = (RSC_IMS_Receive)i.next();
      NcPurchaseOrderBody body = new NcPurchaseOrderBody();

      body.pk_cg_order_b = order.getFormNO() + String.valueOf(order.getAutoNo());
      body.pk_cg_order_h = order.getFormNO();
      body.cinvcode = order.getMaterial_Code();
      body.Invname = order.getMaterial_Name();
      body.nassistnum = order.getReceiveCount();
      body.castunitid = order.getReceiveUnit();

      body.ntaxrate = Float.valueOf(0.0F);

      body.norgtaxprice = Float.valueOf((Util.isNull(order.getPreserved4()).booleanValue()) ? 0.0F : Float.valueOf(order.getPreserved4()).floatValue());

      body.Noriginalcurprice = Float.valueOf((Util.isNull(order.getPreserved3()).booleanValue()) ? 0.0F : Float.valueOf(order.getPreserved3()).floatValue());

      body.nconvertrate = Float.valueOf((Util.isNull(order.getPreserved2()).booleanValue()) ? 0.0F : Float.valueOf(order.getPreserved2()).floatValue());

      rst.add(body);
    }
    return rst;
  }

  private void insertOrderForm(NcPurchaseOrder form, List<NcPurchaseOrderBody> list)
  {
    String sqlInsForm = "Insert Into nc_boh_generalin(busidate, vbillcode, deptcode, freqtypeflag, disposed, actiontype, isupdateallow, ts, pk_generalin,DCCODE,NCDCCODE) Values('%s', '%s', '%s', '%s', 0, 0, 'Y', TO_CHAR(sysdate,'yyyy-mm-dd hh24:mi:ss'), '%s','%s','%s')";

    String sqlInsFormDetail = "Insert Into nc_boh_generalin_b(pk_generalin_b, pk_generalin, invcode, invname, measname, nnumber, disposed, actiontype, isupdateallow, ts) Values('%s','%s','%s','%s','%s',%s,0,0,0,TO_CHAR(sysdate,'yyyy-mm-dd hh24:mi:ss'))";

    String sqlSynStatus = "Update Rsc_Ims_Form Set NcSynStatus = 1 Where FormNo = '%s'";
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.midConn.setAutoCommit(false);

      this.sql = String.format(sqlInsForm, new Object[] { form.orderdate, form.vbillcode, form.cdptcode, form.freqtypeFlag, form.pk_cg_order_h,form.DCCODE,form.NCDCCODE });
      logger.info(this.sql);
      this.stmt = this.midConn.prepareStatement(this.sql);
      this.stmt.execute();
      this.stmt.close();

      for (Iterator j = list.iterator(); j.hasNext(); ) {
        try
        {
          NcPurchaseOrderBody body = (NcPurchaseOrderBody)j.next();
          this.sql = String.format(sqlInsFormDetail, new Object[] { body.pk_cg_order_b, body.pk_cg_order_h, body.cinvcode, body.Invname, body.castunitid, body.nassistnum });

          logger.info(this.sql);
          this.stmt = this.midConn.prepareStatement(this.sql);

          this.stmt.execute();

          this.stmt.close();
        } catch (SQLException e) {
          logger.error("发送进货单、退货单 " + form.pk_cg_order_h + " 数据至NC失败..., 详情: " + e.getMessage(), e);
        }

      }

      this.sql = String.format(sqlSynStatus, new Object[] { form.pk_cg_order_h });
      logger.info(this.sql);
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.stmt.execute();
      this.stmt.close();

      this.midConn.commit();
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
        this.midConn.close();
        this.misConn.close();
      }
      catch (Exception localException3)
      {
      }
    }
  }
}