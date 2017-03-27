package com.suoyasoft.boh.servlet;

import com.suoyasoft.boh.nc.OrderSyn;
import com.suoyasoft.boh.nc.PurchaseSyn;
import com.suoyasoft.boh.nc.SaleOutOrderSyn;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class SynTask2 extends TimerTask
{
  protected static Logger logger = Logger.getLogger(SynTask2.class.getName());

  public void run()
  {
    logger.info("Task2=========��=ʼ=ͬ=��=========");

    logger.info("��ȡMIS�Ķ������˻��������浽NC ���۶���");
    new OrderSyn().synData();

    logger.info("��ȡNC�����۳��ⵥ�����浽MIS�Ľ����˻���ⵥ");
    new SaleOutOrderSyn().synData();

    logger.info("��ȡMIS��ȷ�Ͻ��������˻��������浽NC �ɹ�����");
    new PurchaseSyn().synData();

    logger.info("Task2=========ͬ=��=��=��=========");
  }
}