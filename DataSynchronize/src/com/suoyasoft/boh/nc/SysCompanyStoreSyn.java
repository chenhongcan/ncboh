package com.suoyasoft.boh.nc;

import com.suoyasoft.boh.nc.vo.MisCompanyStore;
import com.suoyasoft.boh.nc.vo.NcCompanyStore;
import com.suoyasoft.boh.utils.MisConn;
import com.suoyasoft.boh.utils.NcMidConn;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class SysCompanyStoreSyn
{
  private Connection midConn;
  private Connection misConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private String sql = "";

  protected static Logger logger = Logger.getLogger(SaleOutOrderSyn.class.getName());

  public void synData()
  {
    try
    {
      List ncCompanyStoreList = loadNcCompanyStore();

      insertCompanyStore(ncCompanyStoreList);
    }
    catch (Exception e)
    {
      logger.debug(e.getMessage());
    }
  }

  private List<NcCompanyStore> loadNcCompanyStore()
  {
    List rst = new ArrayList();
    this.sql = "select pk_corp, unitcode,unitname,deptcode,deptname from  nc.XS_CTCORP where deptcode like '%s' and length(deptcode)=8";

    this.sql = String.format(this.sql, new Object[] { "CN%" });
    logger.debug(this.sql);
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.stmt = this.midConn.prepareStatement(this.sql);
      this.result = this.stmt.executeQuery();
      while (this.result.next())
      {
        NcCompanyStore companyStore = new NcCompanyStore();
        companyStore.pk_corp = this.result.getString("pk_corp");
        companyStore.deptname = this.result.getString("deptname");
        companyStore.deptcode = this.result.getString("deptcode");
        companyStore.unitcode = this.result.getString("unitcode");
        companyStore.unitname = this.result.getString("unitname");

        rst.add(companyStore);
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
    return null;
  }

  private List<MisCompanyStore> formatRSCCompanyStore(List<NcCompanyStore> ncList)
  {
    MisCompanyStore misCompanyStore = null;
    NcCompanyStore ncCompanyStore = null;
    ArrayList rst = new ArrayList();
    for (Iterator i = ncList.iterator(); i.hasNext(); )
    {
      ncCompanyStore = (NcCompanyStore)i.next();
      misCompanyStore = new MisCompanyStore();

      misCompanyStore.setCompanyID(ncCompanyStore.pk_corp);
      misCompanyStore.setCompanyName(ncCompanyStore.unitname);
      misCompanyStore.setStore_Code(ncCompanyStore.deptcode);

      rst.add(misCompanyStore);
    }
    return rst;
  }

  private void insertCompanyStore(List<NcCompanyStore> list)
  {
    String sqlIns = "Insert into XS_CTCORP(pk_corp,deptname,deptcode,unitcode,unitname) values('%s','%s','%s','%s','%s')";

    String sqlDel = "Delete From XS_CTCORP";
    try
    {
      NcCompanyStore companyStore = null;
      this.misConn = MisConn.getInstance().getConnection();
      this.misConn.setAutoCommit(false);

      this.sql = String.format(sqlDel, new Object[0]);
      this.stmt = this.misConn.prepareStatement(this.sql);
      logger.debug(this.sql);
      this.stmt.execute();
      this.stmt.close();
      for (Iterator i = list.iterator(); i.hasNext(); )
      {
        companyStore = (NcCompanyStore)i.next();

        this.sql = String.format(sqlIns, new Object[] { companyStore.pk_corp, companyStore.deptname, companyStore.deptcode, companyStore.unitcode, companyStore.unitname });
        logger.debug(this.sql);
        this.stmt = this.misConn.prepareStatement(this.sql);
        this.stmt.execute();
        this.stmt.close();
      }

      this.misConn.commit();
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