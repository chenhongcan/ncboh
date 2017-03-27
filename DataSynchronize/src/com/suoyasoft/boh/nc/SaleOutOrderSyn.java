package com.suoyasoft.boh.nc;

import com.suoyasoft.boh.nc.vo.NcSalesOutOrder;
import com.suoyasoft.boh.nc.vo.NcSalesOutOrderBody;
import com.suoyasoft.boh.nc.vo.RSC_IMS_Form;
import com.suoyasoft.boh.nc.vo.RSC_IMS_Receive;
import com.suoyasoft.boh.utils.MisConn;
import com.suoyasoft.boh.utils.NcMidConn;
import com.suoyasoft.boh.utils.Util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class SaleOutOrderSyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String sql = "";
  private List<String> okFormNos = new ArrayList();

  protected static Logger logger = Logger.getLogger(SaleOutOrderSyn.class.getName());

  public void synData()
  {
    try
    {
      List formlist = loadNcSalesOutOrder();
      for (Iterator i = formlist.iterator(); i.hasNext(); )
      {
        NcSalesOutOrder form = (NcSalesOutOrder)i.next();

        RSC_IMS_Form misForm = formatRSC_IMS_Form(form);
        List ncList = loadNcSalesOutOrderBody(form.Pk_generalout);
        List misList = formatIMSReceiveData(ncList);
        insertMisForm(misForm, misList);
      }
    }
    catch (Exception e)
    {
      logger.info(e.getMessage());
    }

    new Util().updateNcSaleoutSynStatus(this.okFormNos);
  }

  private List<NcSalesOutOrder> loadNcSalesOutOrder()
  {
    List rst = new ArrayList();

    this.sql = "Select Pk_generalout,vbillcode,deptcode,billdate From Nc_boh_generaldbout where  disposed <> 1 ";

    logger.info(this.sql);
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.stmt = this.midConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        NcSalesOutOrder order = new NcSalesOutOrder();
        order.Pk_generalout = this.result.getString("Pk_generalout");
        order.vbillcode = this.result.getString("vbillcode");
        order.deptcode = this.result.getString("deptcode");
        order.billdate = this.result.getString("billdate");

        rst.add(order);
      }
      return rst;
    }
    catch (Exception e)
    {
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
        this.midConn.close();
      } catch (Exception localException3) {
      }
    }
    return null;
  }

  private List<NcSalesOutOrderBody> loadNcSalesOutOrderBody(String formNo)
  {
    List rst = new ArrayList();

    this.sql = "Select n.vbillcode,m.Pk_generalout_b,m.Pk_generalout,m.Invcode,m.Invname,m.meaname,m.nnumber,m.Meanamefz,m.Nnumberfz from Nc_boh_generaldbout_b m left join Nc_boh_generaldbout n  on m.Pk_generalout = n.Pk_generalout where  m.Pk_generalout = '%s' order by m.pk_generalout, m.invcode ";

    this.sql = String.format(this.sql, new Object[] { formNo });
    logger.debug(this.sql);
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.stmt = this.midConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        NcSalesOutOrderBody body = new NcSalesOutOrderBody();

        body.vbillcode = this.result.getString("vbillcode");
        body.Pk_generalout_b = this.result.getString("Pk_generalout_b");
        body.Pk_generalout = this.result.getString("Pk_generalout");

        body.Invcode = this.result.getString("Invcode");
        body.Invname = this.result.getString("Invname");
        body.meaname = this.result.getString("meaname");
        body.nnumber = this.result.getFloat("nnumber");
        body.Meanamefz = this.result.getString("Meanamefz");
        body.Nnumberfz = this.result.getFloat("Nnumberfz");

        rst.add(body);
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
        this.midConn.close();
      } catch (Exception localException3) {
      }
    }
    return rst;
  }

  private RSC_IMS_Form formatRSC_IMS_Form(NcSalesOutOrder ncSalesOutOrder)
  {
    RSC_IMS_Form form = new RSC_IMS_Form();
    form.setBizDate(ncSalesOutOrder.billdate);
    form.setFormNo(ncSalesOutOrder.vbillcode);
    form.setStore_Code(ncSalesOutOrder.deptcode);
    form.setFormType(6);
    form.setComments("");
    form.setDCCode("");
    form.setPreserved5("6");

    return form;
  }

  private List<RSC_IMS_Receive> formatIMSReceiveData(List<NcSalesOutOrderBody> dataSource)
  {
    List rst = new ArrayList();

    int num = 1;

    for (Iterator i = dataSource.iterator(); i.hasNext(); )
    {
      NcSalesOutOrderBody ncbody = (NcSalesOutOrderBody)i.next();
      RSC_IMS_Receive misbody = new RSC_IMS_Receive();

      misbody.setFormNO(ncbody.vbillcode);
      misbody.setMaterial_Code(ncbody.Invcode);

      misbody.setReceiveCount(Float.valueOf(ncbody.Nnumberfz));
      misbody.setReceiveUnit(ncbody.Meanamefz);
      misbody.setAutoNo(num);
      misbody.setComments("");

      ++num;

      rst.add(misbody);
    }
    return rst;
  }

  private void insertMisForm(RSC_IMS_Form form, List<RSC_IMS_Receive> list)
  {
    String sqlInsForm = "Insert into RSC_IMS_Form(Store_Code,BizDate,FormNO,DCCode,FormType,Comments,Preserved1,Preserved2,Preserved3,Preserved4,Preserved5)values('%s','%s','%s','%s',%s,'%s','%s','%s','%s','%s','%s')";
    String sqlInsFormDetail = "Insert into RSC_IMS_Receive(AutoNO,FormNO,Material_Code,ReceiveCount,ReceiveUnit,Comments,Preserved1,Preserved2,Preserved3,Preserved4,Preserved5,Preserved6,Preserved7)values('%s','%s','%s',%s,'%s','%s','%s','%s','%s','%s','%s','%s','%s')";
    String sqlDelForm = "Delete From RSC_IMS_Form Where FormNO = '%s'";
    String sqlDelFormDetail = "Delete From RSC_IMS_Receive Where FormNO = '%s'";
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.misConn.setAutoCommit(false);

      this.sql = String.format(sqlDelFormDetail, new Object[] { form.getFormNo() });
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.stmt.execute();
      this.stmt.close();

      this.sql = String.format(sqlDelForm, new Object[] { form.getFormNo() });
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.stmt.execute();
      this.stmt.close();

      this.sql = String.format(sqlInsForm, new Object[] { form.getStore_Code(), form.getBizDate(), form.getFormNo(), form.getDCCode(), Integer.valueOf(form.getFormType()), form.getComments(), form.getPreserved1(), form.getPreserved2(), form.getPreserved3(), form.getPreserved4(), form.getPreserved5() });
      logger.debug(this.sql);
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.stmt.execute();
      this.stmt.close();

      for (Iterator j = list.iterator(); j.hasNext(); )
      {
        RSC_IMS_Receive body = (RSC_IMS_Receive)j.next();
        this.sql = String.format(sqlInsFormDetail, new Object[] { Integer.valueOf(body.getAutoNo()), body.getFormNO(), body.getMaterial_Code(), body.getReceiveCount(), body.getReceiveUnit(), body.getComments(), body.getPreserved1(), body.getPreserved2(), body.getPreserved3(), body.getPreserved4(), body.getPreserved5(), body.getPreserved6(), body.getPreserved7() });

        logger.info(this.sql);
        this.stmt = this.misConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();
      }
      this.misConn.commit();

      this.okFormNos.add(form.getFormNo());
    }
    catch (Exception e)
    {
      try
      {
        this.misConn.rollback();
      } catch (Exception localException1) {
      }
      logger.error(this.sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
        this.misConn.close();
      }
      catch (Exception localException3)
      {
      }
    }
  }
}