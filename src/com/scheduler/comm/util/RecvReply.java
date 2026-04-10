package com.scheduler.comm.util;
/**
 * Title     : RecvReply.java
 * Copyright : CSPI, Inc.
 * @author   : CSPI
 * @version  : 1.0
 * Created On : 2012.06.11
 * Description : [Sync] Receive-Reply �ŷ� Sample Source �Դϴ�.
 * ======================================================================================================
 * Change History
 * NO       When         Who             What
 * ======================================================================================================
 */


public class RecvReply {
/*
	private static Logger logger = Logger.getLogger(RecvReply.class.getName());
	
	private static EaiProxy 		proxy		= null;
	
	// ����(Receive)
	private EaiHeaderMgr 	recvMgr		= null;
	private EaiHeaderBean 	recvBean	= null;
	
	// ����(Reply)
	private EaiHeaderMgr 	repMgr 		= null;
	private EaiHeaderBean 	repBean 	= null;
	
	public RecvReply() {
		System.setProperty("eai.home", "D:\\Workspace\\EMSClient");
        try {
            proxy = EaiFactory.getInstance().newProxy();
            
        } catch(Exception e) {
            e.printStackTrace();
        }
	}

		


	public static void main(String[] args) {
		Runnable runnable = new Runnable() {
			public void run() {
				
				
				// task to run goes here
				
				try {
					System.out.println("Hello !!");
					//RecvReply client = new RecvReply();
					//EaiMessage recvMsg = null;
					 receive();
					//client.parseReceiveMessage(recvMsg.getDataString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		ScheduledExecutorService service = Executors
				.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS);
		
				
				EaiMessage recvMsg = null;
//		while(true) {
			try {
				
				
				//client.parseReceiveMessage(recvMsg.getDataString());
				
			//	client.sendReply(recvMsg, client.setResponseMessage());
			} catch(Exception e) {
				e.printStackTrace();
				try {
                    Thread.sleep(30*1000);
                } catch(InterruptedException ie) {
                	ie.printStackTrace();
                }
//                continue;
//			}
		}
	}
	
	*//**
	 * �� �����ϱ�(Header + Body)
	 * @return	String	������
	 *//*
	
	static
	EaiMessage receive() throws Exception {
		EaiMessage recvMsg = proxy.recv("LOCAL.RCV.0");		// queue.cfg�� �?ť�� ���� : [RECV_QUEUE]
		
		return recvMsg;
	}
	
	*//**
	 * ������ �Ľ��ϱ�(Header + Body)
	 * @param	String	������
	 *//*
	void parseReceiveMessage(String sRecvMsg) throws Exception{
		//System.out.println("=====1====");
		recvMgr		= EaiFactory.getInstance().newHeaderMgr();
		//System.out.println("====2====="+sRecvMsg);
		recvBean	= getHeaderParseResponse(sRecvMsg); // ��û �� �Ľ�(REQUEST)
		
        System.out.println("getIfId()             " + recvBean.getIfId()         + "");
        System.out.println("getIfTrId()           " + recvBean.getIfTrId()       + "");
        System.out.println("getIfTargetSys()      " + recvBean.getIfTargetSys()  + "");
        
        
	}
	
	public static EaiHeaderBean getHeaderParseResponse(String sEaiMsg)
			throws Exception {
		//System.out.println("[Message]" + sEaiMsg);

		EaiHeaderBean bean = new EaiHeaderBean();
		try {
			JAXBContext context = JAXBContext
					.newInstance(new Class[] { EAIHeaderResponseVO.class });
			Unmarshaller ums = context.createUnmarshaller();
			StringReader reader = new StringReader(sEaiMsg);

			EAIHeaderResponseVO responseVO = (EAIHeaderResponseVO) ums.unmarshal(reader);
			List outHeadVO = responseVO.Log;

			String sIfId = null;
			String sIfTrId = null;
			String sIfTarSysCd = null;
			String sIfAdditionalInfo = null;
			String sIfResultCd = null;
			String sIfResultMsg = null;

			Iterator iter = outHeadVO.iterator();
			while (iter.hasNext()) {
				EAIHeaderVO headVOUmsData = (EAIHeaderVO) iter.next();
				sIfId = headVOUmsData.interfaceId;
				sIfTrId = headVOUmsData.ifTraceId;
				sIfTarSysCd = headVOUmsData.phase;
			}

			List outData = responseVO.Log;

			bean.setIfId((sIfId != null) ? sIfId.trim() : sIfId);
			bean.setIfTrId((sIfTrId != null) ? sIfTrId.trim() : sIfTrId);
			bean.setIfTargetSys((sIfTarSysCd != null) ? sIfTarSysCd.trim()
					: sIfTarSysCd);
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bean;
	}
	*//**
	 * ������ �����ϱ�(Header + Body)
	 * @return	byte[]	������
	 *//*
	byte[] setResponseMessage() throws Exception {
		repBean = new EaiHeaderBean();
		repBean.setIfId(recvBean.getIfId());
		repBean.setIfTrId(recvBean.getIfTrId());
		repBean.setIfTargetSys(recvBean.getIfTargetSys());
		repBean.setIfAddtionalInfo(recvBean.getIfAddtionalInfo());
		repBean.setIfResult("0000");
		repBean.setIfResultMsg("�����Դϴ�.");
		
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
		sb.append("<root>");
		sb.append("    <IF_RESULT>S</IF_RESULT>");
		sb.append("    <IF_MSG>�����Դϴ�.</IF_MSG>");
		sb.append("</root>");
		List<String> outData = new ArrayList<String>();
		outData.add(sb.toString());
		repBean.setData(outData);
		
		repMgr = EaiFactory.getInstance().newHeaderMgr();
		
		return repMgr.getDataResponse(repBean); // ���� �� (RESPONSE)
	}
	
	*//**
	 * ������ �۽��ϱ�(Header + Body)
	 * @param	EaiMessage	������
	 * @param	byte[]		������
	 *//*
	void sendReply(EaiMessage recvMsg, byte[] result) throws Exception {
		EaiMessage repMsg = EaiFactory.getInstance().newMessage();
		repMsg.setData((byte[])result);
		proxy.sendReply(recvMsg.getReplyId(), repMsg);
	}
	@Scheduled(fixedDelay = 60000)
	public void schedule(){
		
	}*/
}
