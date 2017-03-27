package com.suoyasoft.boh.nc;

import com.suoyasoft.boh.nc.vo.MisDataType;
import com.suoyasoft.boh.nc.vo.MisMaterial;
import com.suoyasoft.boh.nc.vo.MisStore;
import com.suoyasoft.boh.utils.MisConn;
import com.suoyasoft.boh.utils.NcConn;
import com.suoyasoft.boh.utils.NcMidConn;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class DataSyn
{
  private static final String CURRENTCY = "002";
  private static final String MEASURE_UNIT = "050";
  private static final String CHECK_UNIT = "036";
  private static final String BALA_TYPE = "051";
  private static final String JOB_TYPE = "48";
  private static final String REWARD_PUN_PRO = "81";
  private static final String REWARD_PUN_MEASURES = "80";
  private static final int TYPE_ID_RESIGN = 24;
  private static final int EMPLOYEE_TYPE = 45;
  private static final String CUSTOMER_ID = "038";
  private static final String CUSTOMER_IDS = "085";
  private static final int REASON_ONE = 1;
  private static final int REASON_TWO = 3;
  private static final int REASON_THREE = 5;
  private Connection ncConn;
  private Connection misConn;
  private Connection midConn;
  private PreparedStatement stmt;
  private ResultSet result;
  private MisStore misStore;
  private MisDataType misDataType;
  private MisMaterial misMaterial;
  protected static Logger logger = Logger.getLogger(DataSyn.class.getName());

  public void synData()
  {
    logger.info("同步计量单位");
    synchronizeDataType(loadMeasureUnit());

    logger.info("同步计量单位（盘点单位）");
    synchronizeDataType(loadMeasureUnitCheck());

    logger.info("同步币种");
    synchronizeDataType(loadCurrentcy());

    logger.info("客户档案");
    synchronizeDataType(loadCustType());

    logger.info("同步结算方式");
    synchronizeDataType(loadBalaType());

    logger.info("同步餐厅数据");
    synchronizeStore(loadStore());

    logger.info("同步物料");
    synchronizeMaterial(loadMaterial());
  }

  private List<MisDataType> loadCustType()
  {
    String sql = "select custcode as DictCode,custname as TypeName ,custcode as TypeCode ,0 as Status ,disposed from nc_boh_cust";

    return loadDataType(sql, "085");
  }

  private List<MisDataType> loadDataType(String sql, String dataType)
  {
    logger.debug(sql);
    try
    {
      this.ncConn = NcConn.getInstance().getConnection();
      this.stmt = this.ncConn.prepareStatement(sql);
      this.result = this.stmt.executeQuery();

      List list = new ArrayList();
      while (this.result.next())
      {
        this.misDataType = new MisDataType();
        this.misDataType.setDictCode(dataType + this.result.getString("DictCode"));
        this.misDataType.setTypeID(Integer.parseInt(dataType));
        this.misDataType.setTypeCode(this.result.getString("TypeCode"));
        this.misDataType.setTypeName(this.result.getString("TypeName"));
        this.misDataType.setStatus(this.result.getInt("Status"));
        list.add(this.misDataType);
      }
      return list;
    }
    catch (Exception e)
    {
      logger.error(sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      } catch (Exception localException5) {
      }
      try {
        this.ncConn.close(); } catch (Exception localException6) {
      }
    }
    return null;
  }

  private List<MisDataType> loadMeasureUnit()
  {
    String sql = "select meascode as DictCode,measname as TypeName ,meascode as TypeCode ,0 as Status ,disposed from nc_boh_meas";

    return loadDataType(sql, "050");
  }

  private List<MisDataType> loadMeasureUnitCheck()
  {
    String sql = "select meascode as DictCode,measname as TypeName ,meascode as TypeCode ,0 as Status ,disposed from nc_boh_meas";

    return loadDataType(sql, "036");
  }

  private List<MisDataType> loadCurrentcy()
  {
    String sql = "select currcycode as DictCode,currcyname as TypeName ,currcycode as TypeCode ,0 as Status ,disposed from nc_boh_currency";

    return loadDataType(sql, "002");
  }

  private List<MisDataType> loadBalaType()
  {
    String sql = "select balatypecode as DictCode,balatypename as TypeName ,balatypecode as TypeCode ,0 as Status ,disposed from nc_boh_balatype";

    return loadDataType(sql, String.valueOf(Integer.parseInt("051")));
  }

  private void synchronizeDataType(List<MisDataType> list)
  {
    String sql = "";
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.misConn.setAutoCommit(false);

      MessageFormat mf = new MessageFormat("Merge Into Rsc_Sys_Data_Type R Using (select '1' from dual) N on (R.Dict_Code = ''{0}'') WHEN MATCHED THEN      UPDATE Set Type_Code = ''{1}'', Type_Name = ''{2}'', Status = ''{3}'' ,Join_ID=''{5}'' WHEN NOT MATCHED THEN       INSERT (Dict_Code, Type_Code, Type_Name, Status, Type_ID,Join_ID) Values(''{0}'', ''{1}'', ''{2}'', ''{3}'', ''{4}'',''{5}'')");

      for (Iterator i = list.iterator(); i.hasNext(); )
      {
        MisDataType data = (MisDataType)i.next();

        if ((data.getTypeID() == 24) && 
          (data.getTypeCode().length() == 1))
        {
          data.setJoinID("0");
          String innerSql = "select Dict_Code from Rsc_Sys_Data_Type where Type_Id=24 and legnth(Type_Code)=1 and Type_ID='%s'";
          innerSql = String.format(innerSql, new Object[] { data.getTypeCode() });
        }

        sql = mf.format(new String[] { 
          data.getDictCode(), 
          data.getTypeCode(), 
          data.getTypeName(), 
          (data.getStatus() == 1) ? "0" : "1", 
          String.valueOf(data.getTypeID()), 
          (data.getJoinID() == null) ? "0" : data.getJoinID() });

        logger.info(sql);
        this.stmt = this.misConn.prepareStatement(sql);
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
      logger.error(sql);
      logger.error(e.getMessage());
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
        this.misConn.close();
      }
      catch (Exception localException5)
      {
      }
    }
  }

  private List<MisDataType> loadEhrDefDoc()
  {
    String sql = "Select d.pk_defdoclist as DataType, d.doclistname as DataTypeName, d.pk_defdoc as DictCode, d.doccode as TypeCode, d.docname as TypeName, d.sealflag as Status From V_defdoc d Order by d.PK_DefDocList";

    logger.debug(sql);
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.stmt = this.midConn.prepareStatement(sql);
      this.result = this.stmt.executeQuery();

      List list = new ArrayList();
      while (this.result.next())
      {
        int typeid = formatTypeID(this.result.getString("DataType"));
        if (typeid == 0)
          continue;
        this.misDataType = new MisDataType();
        this.misDataType.setTypeID(typeid);
        this.misDataType.setDictCode(this.result.getString("DictCode"));
        this.misDataType.setTypeCode(this.result.getString("TypeCode"));
        this.misDataType.setTypeName(this.result.getString("TypeName"));
        String tmp = this.result.getString("Status");

        if ((tmp == null) || (tmp == "") || (tmp.equals("N")))
        {
          this.misDataType.setStatus(0);
        }
        else
        {
          this.misDataType.setStatus(1);
        }
        list.add(this.misDataType);
      }
      for (Iterator iter = list.iterator(); iter.hasNext(); )
      {
        MisDataType misDataType = (MisDataType)iter.next();

        if (misDataType.getTypeID() != 24) {
          continue;
        }
        if (misDataType.getTypeCode().length() == 1)
        {
          misDataType.setJoinID("0");
        }
        else if (misDataType.getTypeCode().length() == 3)
        {
          for (Iterator iter2 = list.iterator(); iter2.hasNext(); )
          {
            MisDataType misDataType2 = (MisDataType)iter2.next();
            if ((misDataType2.getTypeID() != 24) || 
              (misDataType2.getTypeCode().length() != 1) || (!(misDataType2.getTypeCode().equals(misDataType.getTypeCode().substring(0, 1)))))
              continue;
            misDataType.setJoinID(misDataType2.getDictCode());
          }
        }
        else
        {
          if (misDataType.getTypeCode().length() != 5)
            continue;
          for (Iterator iter3 = list.iterator(); iter3.hasNext(); )
          {
            MisDataType misDataType3 = (MisDataType)iter3.next();
            if ((misDataType3.getTypeID() != 24) || 
              (misDataType3.getTypeCode().length() != 3) || (!(misDataType3.getTypeCode().equals(misDataType.getTypeCode().substring(0, 3)))))
              continue;
            misDataType.setJoinID(misDataType3.getDictCode());
          }

        }

      }

      return list;
    }
    catch (Exception e)
    {
      logger.error(sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      } catch (Exception localException5) {
      }
      try {
        this.midConn.close(); } catch (Exception localException6) {
      }
    }
    return null;
  }

  private List<MisDataType> loadEhrVJob()
  {
    String sql = "Select d.pk_om_job as DictCode, jobcode as TypeCode, jobname as TypeName, '%s' as Type_ID ,'0' as Status From V_job d Order by d.pk_om_job";

    sql = String.format(sql, new Object[] { "48" });
    logger.debug(sql);
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.stmt = this.midConn.prepareStatement(sql);
      this.result = this.stmt.executeQuery();

      List list = new ArrayList();
      while (this.result.next())
      {
        this.misDataType = new MisDataType();
        this.misDataType.setDictCode(this.result.getString("DictCode"));
        this.misDataType.setTypeCode(this.result.getString("TypeCode"));
        this.misDataType.setTypeName(this.result.getString("TypeName"));
        this.misDataType.setTypeID(Integer.parseInt(this.result.getString("Type_ID")));
        this.misDataType.setStatus(this.result.getInt("Status"));
        list.add(this.misDataType);
      }
      return list;
    }
    catch (Exception e)
    {
      logger.error(sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      } catch (Exception localException5) {
      }
      try {
        this.midConn.close(); } catch (Exception localException6) {
      }
    }
    return null;
  }

  private List<MisDataType> loadEhrV_PSNCLASS()
  {
    String sql = "Select d.pk_psncl as DictCode, psnclasscode as TypeCode, psnclassname as TypeName, '%s' as Type_ID ,'0' as Status From V_PSNCLASS d Order by d.pk_psncl";

    sql = String.format(sql, new Object[] { Integer.valueOf(45) });
    logger.debug(sql);
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.stmt = this.midConn.prepareStatement(sql);
      this.result = this.stmt.executeQuery();

      List list = new ArrayList();
      while (this.result.next())
      {
        this.misDataType = new MisDataType();
        this.misDataType.setDictCode(this.result.getString("DictCode"));
        this.misDataType.setTypeCode(this.result.getString("TypeCode"));
        this.misDataType.setTypeName(this.result.getString("TypeName"));
        this.misDataType.setTypeID(Integer.parseInt(this.result.getString("Type_ID")));
        this.misDataType.setStatus(this.result.getInt("Status"));
        list.add(this.misDataType);
      }
      return list;
    }
    catch (Exception e)
    {
      logger.error(sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      } catch (Exception localException5) {
      }
      try {
        this.midConn.close(); } catch (Exception localException6) {
      }
    }
    return null;
  }

  private int formatTypeID(String dataType)
  {
    if (dataType.equals("0001A3100000000000EN")) return 60;
    if (dataType.equals("0001A3100000000000GU")) return 46;
    if (dataType.equals("0001A3100000000000HW")) return 47;
    if (dataType.equals("0001CM00000000000001")) return 55;
    if (dataType.equals("0001HRTM000000000002")) return 24;
    if (dataType.equals("0001PLY0000000000007")) return 57;
    if (dataType.equals("HI000000000000000003")) return 63;
    if (dataType.equals("HI000000000000000007")) return 18;
    if (dataType.equals("HI000000000000000011")) return 44;
    if (dataType.equals("HI000000000000000026")) return 19;
    if (dataType.equals("0001A31000000005QA9W")) return 58;
    if (dataType.equals("HI000000000000000028")) return 49;
    if (dataType.equals("0001A310000000000LOU")) return 73;
    if ((dataType.equals("0001A310000000000LF7")) || 
      (dataType.equals("HI000000000000000051")) || 
      (dataType.equals("HI000000000000000052"))) return 72;
    if (dataType.equals("HI0000000000000LYQ05")) return 56;
    if (dataType.equals("0001A31000000000039Y")) return 43;
    if (dataType.equals("0001A31000000005PT2R")) return 77;
    if (dataType.equals("0001A310000000000GR5")) return 59;
    return 0;
  }

  private void synchronizeMaterial(List<MisMaterial> list)
  {
    String sql = "";
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.misConn.setAutoCommit(false);

      MessageFormat mf = new MessageFormat("Merge Into Rsc_Material R Using (select '1' from dual) N on (R.Material_Code = ''{0}'') WHEN MATCHED THEN   UPDATE Set Material_Name = ''{1}'', Spec = ''{2}'', OrderUnit = ''{3}'', CheckUnit = {5},  CheckScale= {6}, Price = {7}, OrderScale = {8} WHEN NOT MATCHED THEN    INSERT (Material_Code, Material_Name, Spec, OrderUnit, Status, CheckUnit, CheckScale, Price, OrderScale, OrderType, Factor) Values(''{0}'', ''{1}'', ''{2}'', ''{3}'', ''{4}'', {5}, {6}, {7}, {8}, 1, 1)");

      for (Iterator i = list.iterator(); i.hasNext(); )
      {
        MisMaterial data = (MisMaterial)i.next();
        sql = mf.format(new String[] { 
          data.getMaterialCode(), 
          data.getMaterialName(), 
          data.getSpec(), 
          data.getOrderUnit(), 
          (data.getStatus() == 1) ? "0" : "1", 
          "(Select Dict_Code From RSc_Sys_Data_Type Where Type_ID = 36 And Type_Name = (Select Type_Name From RSc_Sys_Data_Type Where dict_Code = '" + data.getCheckUnit() + "') and rownum = 1)", 
          String.valueOf(data.getCheckScale()), 
          String.valueOf(data.getPrice()), 
          String.valueOf(data.getOrderScale()) });

        logger.info(sql);
        this.stmt = this.misConn.prepareStatement(sql);
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
      logger.error(sql);
      logger.error(e.getMessage());
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
        this.misConn.close();
      }
      catch (Exception localException5)
      {
      }
    }
  }

  private void synchronizeMaterialType(List<MisMaterial> list) {
    String sql = "";
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.misConn.setAutoCommit(false);

      MessageFormat mf = new MessageFormat("Merge Into Rsc_Material_Category R Using (select '1' from dual) N on (R.Category_Code = ''{0}'') WHEN MATCHED THEN      UPDATE Set Category_Name = ''{1}'' WHEN NOT MATCHED THEN       INSERT (Category_Code, Category_Name ,Status) Values(''{0}'', ''{1}'', ''{2}'')");

      for (Iterator i = list.iterator(); i.hasNext(); )
      {
        MisMaterial data = (MisMaterial)i.next();
        sql = mf.format(new String[] { 
          data.getMaterialCode(), 
          data.getMaterialName(), 
          (data.getStatus() == 1) ? "0" : "1" });

        logger.debug(sql);
        this.stmt = this.misConn.prepareStatement(sql);
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
      logger.error(sql);
      logger.error(e.getMessage());
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
        this.misConn.close();
      }
      catch (Exception localException5)
      {
      }
    }
  }

  private List<MisMaterial> loadMaterialType()
  {
    String sql = "select invclcode as materialCode,invclname as materialName,status from nc_boh_invcl";
    logger.debug(sql);
    try
    {
      this.ncConn = NcConn.getInstance().getConnection();
      this.stmt = this.ncConn.prepareStatement(sql);
      this.result = this.stmt.executeQuery();

      List list = new ArrayList();
      while (this.result.next())
      {
        this.misMaterial = new MisMaterial();
        this.misMaterial.setMaterialCode(this.result.getString("materialCode"));
        this.misMaterial.setMaterialName(this.result.getString("materialName"));
        this.misMaterial.setStatus(this.result.getInt("status"));
        list.add(this.misMaterial);
      }
      return list;
    }
    catch (Exception e)
    {
      logger.error(sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      } catch (Exception localException5) {
      }
      try {
        this.ncConn.close(); } catch (Exception localException6) {
      }
    }
    return null;
  }

  private List<MisMaterial> loadMaterial()
  {
    String sql = 
      "Select invCode, invName, spec, measName, auxMeas, nvl(measRate, 1) as measRate, nvl(outPrice, 0) as inPrice, nvl(bomrate,1) as bomrate, Status From nc_boh_inv ";

    logger.debug(sql);
    try
    {
      this.ncConn = NcConn.getInstance().getConnection();
      this.stmt = this.ncConn.prepareStatement(sql);
      this.result = this.stmt.executeQuery();

      List list = new ArrayList();
      while (this.result.next())
      {
        this.misMaterial = new MisMaterial();
        this.misMaterial.setMaterialCode(this.result.getString("invCode"));
        this.misMaterial.setMaterialName(this.result.getString("invName"));
        this.misMaterial.setSpec(this.result.getString("spec"));
        this.misMaterial.setOrderUnit("050" + this.result.getString("auxMeas"));
        this.misMaterial.setCheckUnit("036" + this.result.getString("measName"));

        this.misMaterial.setCheckScale(this.result.getFloat("measRate"));
        this.misMaterial.setOrderScale(this.result.getFloat("bomrate"));

        this.misMaterial.setPrice(this.result.getFloat("inPrice"));
        this.misMaterial.setStatus(this.result.getInt("status"));
        list.add(this.misMaterial);
      }
      return list;
    }
    catch (Exception e)
    {
      logger.error(sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      } catch (Exception localException5) {
      }
      try {
        this.ncConn.close(); } catch (Exception localException6) {
      }
    }
    return null;
  }

  public List<MisStore> loadStore()
  {
    String sql = "select deptcode,deptname,0 as Is24Hours,status,disposed from nc_boh_dept";
    logger.debug(sql);
    try
    {
      this.ncConn = NcConn.getInstance().getConnection();
      this.stmt = this.ncConn.prepareStatement(sql);
      this.result = this.stmt.executeQuery();

      List list = new ArrayList();
      while (this.result.next())
      {
        this.misStore = new MisStore();
        this.misStore.setStoreCode(this.result.getString("deptcode"));
        this.misStore.setStoreName(this.result.getString("deptname"));
        this.misStore.setIs24Hours(this.result.getString("Is24Hours"));
        this.misStore.setStatus(this.result.getInt("status"));

        list.add(this.misStore);
      }
      return list;
    }
    catch (Exception e)
    {
      logger.error(sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      } catch (Exception localException5) {
      }
      try {
        this.ncConn.close(); } catch (Exception localException6) {
      }
    }
    return null;
  }

  private void synchronizeStore(List<MisStore> list)
  {
    String sql = "";
    try
    {
      this.misConn = MisConn.getInstance().getConnection();
      this.misConn.setAutoCommit(false);

      MessageFormat mf = new MessageFormat("Merge Into Rsc_Store R Using (select '1' from dual) N on (R.Store_Code = ''{0}'') WHEN MATCHED THEN      UPDATE Set Store_Name = ''{1}'', Is24Hours = ''{2}''WHEN NOT MATCHED THEN       INSERT (Store_Code, Store_Name, Is24Hours) values(''{0}'', ''{1}'', ''{2}'')");

      for (Iterator i = list.iterator(); i.hasNext(); )
      {
        MisStore store = (MisStore)i.next();
        sql = mf.format(new String[] { store.getStoreCode(), store.getStoreName(), store.getIs24Hours(), String.valueOf(store.getStatus()) });
        logger.info(sql);
        this.stmt = this.misConn.prepareStatement(sql);
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
      logger.error(sql);
      logger.error(e.getMessage());
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
        this.misConn.close();
      }
      catch (Exception localException5)
      {
      }
    }
  }

  private List<MisDataType> loadEHRPun()
  {
    String sql = "Select d.参照主键 as DictCode, d.编号 as TypeCode, d.名称 as TypeName, '%s' as Type_ID ,d.封存标示 as Status From v_EncPun d Order by d.参照主键";

    sql = String.format(sql, new Object[] { "81" });
    logger.debug(sql);
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.stmt = this.midConn.prepareStatement(sql);
      this.result = this.stmt.executeQuery();

      List list = new ArrayList();
      while (this.result.next())
      {
        this.misDataType = new MisDataType();
        this.misDataType.setDictCode(this.result.getString("DictCode"));
        this.misDataType.setTypeCode(this.result.getString("TypeCode"));
        this.misDataType.setTypeName(this.result.getString("TypeName"));
        this.misDataType.setTypeID(Integer.parseInt(this.result.getString("Type_ID")));
        this.misDataType.setStatus(this.result.getInt("Status"));
        String tmp = this.result.getString("Status");
        if ((tmp == null) || (tmp == "") || (tmp.equals("N")))
        {
          this.misDataType.setStatus(0);
        }
        else
        {
          this.misDataType.setStatus(1);
        }
        list.add(this.misDataType);
      }
      return list;
    }
    catch (Exception e)
    {
      logger.error(sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      } catch (Exception localException5) {
      }
      try {
        this.midConn.close(); } catch (Exception localException6) {
      }
    }
    return null;
  }

  private List<MisDataType> loadEHRMeasures()
  {
    String sql = "Select d.参照主键 as DictCode, d.编号 as TypeCode, d.名称 as TypeName, '%s' as Type_ID ,d.封存标示 as Status From v_ChengFaFuzhu d Order by d.参照主键";

    sql = String.format(sql, new Object[] { "80" });
    logger.debug(sql);
    try
    {
      this.midConn = NcMidConn.getInstance().getConnection();
      this.stmt = this.midConn.prepareStatement(sql);
      this.result = this.stmt.executeQuery();

      List list = new ArrayList();
      while (this.result.next())
      {
        this.misDataType = new MisDataType();
        this.misDataType.setDictCode(this.result.getString("DictCode"));
        this.misDataType.setTypeCode(this.result.getString("TypeCode"));
        this.misDataType.setTypeName(this.result.getString("TypeName"));
        this.misDataType.setTypeID(Integer.parseInt(this.result.getString("Type_ID")));
        this.misDataType.setStatus(this.result.getInt("Status"));
        String tmp = this.result.getString("Status");
        if ((tmp == null) || (tmp == "") || (tmp.equals("N")))
        {
          this.misDataType.setStatus(0);
        }
        else
        {
          this.misDataType.setStatus(1);
        }
        list.add(this.misDataType);
      }

      return list;
    }
    catch (Exception e)
    {
      logger.error(sql);
      logger.error(e.getMessage());
    }
    finally
    {
      try
      {
        this.stmt.close();
      } catch (Exception localException5) {
      }
      try {
        this.midConn.close(); } catch (Exception localException6) {
      }
    }
    return null;
  }
}