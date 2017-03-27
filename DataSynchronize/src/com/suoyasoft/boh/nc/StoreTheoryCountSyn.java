package com.suoyasoft.boh.nc;

import com.suoyasoft.boh.nc.vo.MisStore;
import com.suoyasoft.boh.nc.vo.NcMIdDataaBody;
import com.suoyasoft.boh.nc.vo.RSC_IMS_MaterialStockReport;
import com.suoyasoft.boh.utils.MisConn;
import com.suoyasoft.boh.utils.NcMidConn;
import com.suoyasoft.boh.utils.Util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class StoreTheoryCountSyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String sql = "";
  private List<String> okFormNos = new ArrayList();
  protected static Logger logger = Logger.getLogger(StoreTheoryCountSyn.class.getName());

  public void synData()
  {
    try
    {
      loadMisFormData();

      new Util().updateMisStockReportSynStatus(this.okFormNos);
    }
    catch (Exception e)
    {
      logger.debug(e.getMessage());
    }
  }

  private void loadMisFormData()
  {
    String storeCode = "";
    try
    {
      List storeList = new Util().getStoreList();
      for (Iterator i = storeList.iterator(); i.hasNext(); )
      {
        MisStore store = (MisStore)i.next();
        storeCode = store.getStoreCode();

        List bizdates = getBizdates(storeCode);
        for (Iterator d = bizdates.iterator(); d.hasNext(); )
        {
          String bizDate = (String)d.next();

          List rst = new ArrayList();

          this.sql = "Select distinct Material_Code,theoryCount,Unit From RSC_IMS_MaterialStockReport Where bizdate='%s' and Store_Code='%s' and checkcount=1";
          this.sql = String.format(this.sql, new Object[] { bizDate, storeCode });

          logger.debug(this.sql);
          this.misConn = MisConn.getInstance().getConnection();
          this.stmt = this.misConn.prepareStatement(this.sql);
          this.result = this.stmt.executeQuery();

          while (this.result.next())
          {
            RSC_IMS_MaterialStockReport order = new RSC_IMS_MaterialStockReport();
            order.setMaterial_Code(this.result.getString("Material_Code"));
            order.setTheoryCount(Float.valueOf(this.result.getFloat("theoryCount")));
            order.setUnit(this.result.getString("Unit"));
            rst.add(order);
          }
          this.stmt.close();
          this.misConn.close();

          formatTheoryData(rst, bizDate, storeCode);
        }
      }
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
        this.misConn.close();
      }
      catch (Exception localException2)
      {
      }
    }
  }

  private List<String> getBizdates(String storeCode) {
    this.sql = "Select bizdate from RSC_IMS_MaterialStockReport Where Store_Code='" + storeCode + "' And NcSynStatus = 0 group by bizdate order by bizdate";
    logger.debug(this.sql);
    List dates = new ArrayList();
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.stmt = this.misConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();

      while (this.result.next())
      {
        dates.add(this.result.getString("bizdate"));
      }
    }
    catch (Exception e)
    {
      logger.error(this.sql);
      logger.debug(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
        this.misConn.close();
      } catch (Exception localException2) {
      }
    }
    return dates;
  }

  private void formatTheoryData(List<RSC_IMS_MaterialStockReport> dataSource, String bizDate, String storeCode)
  {
    List rst = new ArrayList();

    for (Iterator i = dataSource.iterator(); i.hasNext(); )
    {
      RSC_IMS_MaterialStockReport report = (RSC_IMS_MaterialStockReport)i.next();
      NcMIdDataaBody body = new NcMIdDataaBody();
      body.autono = storeCode + bizDate.replace("-", "") + report.getMaterial_Code();
      body.materialcode = report.getMaterial_Code();
      body.count = report.getTheoryCount().intValue();
      body.unit = report.getUnit();

      rst.add(body);
    }
    insertNcMidData(rst, bizDate, storeCode);
  }

  private void insertNcMidData(List<NcMIdDataaBody> list, String bizDate, String storeCode)
  {
    String sqlInsForm = "Insert into mid_invmain (formno,storecode,bizdate,ts) values('%s','%s','%s','%s')";
    String sqlInsBody = "Insert into mid_invsub (formno,autono,materialcode,unit,count,ts)values('%s','%s','%s','%s',%s,'%s')";

    String sqlDelForm = "Delete From mid_invmain Where formno='%s'";

    String sqlDelBody = "Delete From mid_invsub Where formno='%s'";
    try
    {
      String formNo = storeCode + bizDate.substring(0, 4) + bizDate.substring(5, 7) + bizDate.substring(8, 10) + "008";

      this.midConn = NcMidConn.getInstance().getConnection();
      this.midConn.setAutoCommit(false);

      this.sql = String.format(sqlDelBody, new Object[] { formNo });
      this.stmt = this.midConn.prepareStatement(this.sql);
      this.stmt.execute();
      this.stmt.close();

      this.sql = String.format(sqlDelForm, new Object[] { formNo });
      this.stmt = this.midConn.prepareStatement(this.sql);
      this.stmt.execute();
      this.stmt.close();

      this.sql = String.format(sqlInsForm, new Object[] { formNo, storeCode, bizDate, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(System.currentTimeMillis())) });
      logger.debug(this.sql);
      this.stmt = this.midConn.prepareStatement(this.sql);
      this.stmt.execute();
      this.stmt.close();

      for (Iterator i = list.iterator(); i.hasNext(); )
      {
        NcMIdDataaBody body = (NcMIdDataaBody)i.next();

        this.sql = String.format(sqlInsBody, new Object[] { formNo, body.autono, body.materialcode, body.unit, Integer.valueOf(body.count), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(System.currentTimeMillis())) });

        logger.debug(this.sql);
        this.stmt = this.midConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();
      }
      this.midConn.commit();
      this.okFormNos.add(formNo);
    }
    catch (Exception e)
    {
      try
      {
        this.midConn.rollback();
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
        this.midConn.close();
      }
      catch (Exception localException3)
      {
      }
    }
  }
}