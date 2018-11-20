package manage.approval.web;
import interfaces.irmsInterface.interfaces.opticTran.service.impl.IirmsOpticTranService;
import interfaces.irmsInterface.interfaces.outLine.pojo.IrmsPoint;
import interfaces.irmsInterface.interfaces.outLine.service.impl.IirmsOutLineService;
import interfaces.irmsInterface.interfaces.station.service.impl.ISyncSiteDataService;
import interfaces.irmsInterface.interfaces.station.service.impl.IirmsStationService;
import interfaces.pdainterface.equt.service.PDAEqutInfoService;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import manage.approval.pojo.ApprovalReportPojo;
import manage.approval.pojo.ApprovalResRejectPojo;
import manage.approval.pojo.ApprovalTaskPojo;
import manage.approval.service.impl.IapprovalTaskService;
import manage.buriedPart.pojo.BuriedPartObj;
import manage.equt.pojo.EqutInfoBean;
import manage.equt.pojo.ODMInfoBean;
import manage.generator.pojo.GeneratorInfoBean;
import manage.generator.pojo.StationBaseInfoBean;
import manage.leadup.pojo.LeadupPojo;
import manage.opticalTerminal.pojo.OpticalTerminalObj;
import manage.pipe.pojo.PipeSegmentInfoBean;
import manage.pipe.pojo.WellInfoBean;
import manage.poleline.pojo.PoleInfoBean;
import manage.poleline.pojo.PolelineSegmentInfoBean;
import manage.route.pojo.FiberBoxInfoBean;
import manage.route.pojo.JointInfoBean;
import manage.stone.pojo.StoneInfoBean;
import manage.user.pojo.UserInfoBean;
import net.sf.ezmorph.bean.MorphDynaBean;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.metadata.SybaseCallMetaDataProvider;

import base.util.JsonUtil;
import base.util.TextUtil;
import base.web.PaginationAction;

import com.opensymphony.xwork2.ModelDriven;

import edu.emory.mathcs.backport.java.util.LinkedList;
public class ApprovalTaskAction extends PaginationAction implements ModelDriven{
	
	private int length = 15;
	private static final Logger log = Logger.getLogger(ApprovalTaskAction.class);
	private ApprovalTaskPojo object = new ApprovalTaskPojo();
	private IapprovalTaskService approvalService;
	private String jsonString;
	private java.io.File file;
	private String fileFileName;
    private PDAEqutInfoService pdaEqutInfoService;
	private IirmsStationService irmsStationService;
	private IirmsOutLineService irmsOutLineService;
	private IirmsOpticTranService irmsOpticTranService;
	private ISyncSiteDataService syncSiteDataService;
	
	/*
	 * 	分页查询
	 * 
	 * */
	public void getTaskList()throws Exception{
		UserInfoBean userInfoBean=(UserInfoBean)getRequest().getSession().getAttribute("userBean");
		if(!(userInfoBean.getUsername().equals("root"))){
			object.setCounty(userInfoBean.getAreaName());
		}
		String taskState = this.getRequest().getParameter("taskState");
		if(TextUtil.isNull(taskState) || taskState.equals("已派")){
			object.setTaskState(null);
			object.setFlowName("send");
			object.setExtendSql(" t.taskState != '归档' ");
		}
		if(TextUtil.isNotNull(taskState) && taskState.equals("处理中")) {
			object.setTaskState("处理中");
		}
		if(TextUtil.isNotNull(taskState) && taskState.equals("处理完成")) {
			object.setTaskState("处理完成");
			object.setFlowName("send");
		}
		this.object = approvalService.getTasks(this.object,userInfoBean.getUserId()+"");
		JSONObject thisObject = new JSONObject();
		thisObject.put("total",object.getTotal());
		thisObject.put("items",object.getItems());
		this.printString(thisObject.toString(), null);
	}
	
	/**
	 * 得到时候后的数据信息
	 * @throws Exception
	 */
	public void getAppAuditList() throws Exception{
		List<ApprovalTaskPojo> auditList = this.approvalService.getAppAuditList(object);
		int count = this.approvalService.getAppAuditCount(object);
		JSONObject thisObject = new JSONObject();
		thisObject.put("total",count);
		thisObject.put("items",auditList);
		this.printString(thisObject.toString(), null);
	}
	
	/**
	 * 统计报表
	 * @throws Exception
	 */
	public void getTaskReport()throws Exception{
		List<Map<String, Object>> list = this.approvalService.getReportTask(this.object);
		jsonString = JsonUtil.getJsonString4List(list);
		this.printString("{\"total\":\""+list.size()+"\",\"items\":"+jsonString+"}", null);
	}
	
	/**
	 * 得到所有的
	 * 日报统计报表
	 * @throws Exception
	 */
	public void getApprovalReport()throws Exception{
		List<ApprovalReportPojo> list = this.approvalService.getApprovalReport();
		jsonString = JsonUtil.getJsonString4List(list);
		this.printString("{\"total\":\""+list.size()+"\",\"items\":"+jsonString+"}", null);
	}
	
	/**
	 * 产权性质的处理报表
	 * @throws Exception
	 */
	public void getPropertyReport() throws Exception{
		List<Map<String, Object>> list = this.approvalService.getPropertyReport(this.object);
		jsonString = JsonUtil.getJsonString4List(list);
		this.printString("{\"total\":\""+list.size()+"\",\"items\":"+jsonString+"}", null);
	}
	
	
	/*
	 * 	审批工单
	 * 
	 * */
	public void saveCheckAudit()throws Exception{
		JdbcTemplate jdbcTemplate = approvalService.getTemplate();
		String newState = "已完成";
		if(this.getRequest().getParameter("auditResult")!=null && "驳回".equals(this.getRequest().getParameter("auditResult"))){
			newState = "已驳回";
		}
		jdbcTemplate.execute("update approval_task set taskState = '"+newState+"' where id = "+this.getRequest().getParameter("taskCode"));
		this.printString("{success:true}",null);
	}
	
	
	/**
	 * 导出数据模板
	 */
	public void expTaskReport() {
		try {
			this.approvalService.expTaskReport(this.object, this.getRequest(), getResponse());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 导出日报数据
	 */
	public void extApprovalReport() {
		try {
			this.approvalService.extApprovalReport(this.object,this.getRequest(),getResponse());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 导出产权性质管线
	 */
	public void expPropertyReport() {
		try {
			this.approvalService.expPropertyReport(this.object, this.getRequest(), getResponse());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 导出数据
	 */
	public void extTaskAuditTask() {
		try {
			this.approvalService.extTaskAuditList(this.object, this.getRequest(), getResponse());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 得到审批工
	 * 单的详情
	 */
	public void getApprovalTaskObj(){
		try{
			String id = this.getRequest().getParameter("id");
			object = this.approvalService.getApprovalObj(id);
			if(object.getResType().equals("well,pole,stone")) {
				object.setResType("电杆,标石,井,光交,光缆接头盒,光终端盒,光分纤箱,撑点");
			}
			if(object.getFlowName().equals("send")) {
				String sender = object.getSender();
				String approvaler = object.getApprovaler();
				object.setSender(approvaler);
				object.setApprovaler(sender);
			}
			jsonString = JsonUtil.beanToJson(object);
			this.printString(jsonString, null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 得到资源内容
	 */
	public void getResObj(){
		try{
			UserInfoBean userInfoBean=(UserInfoBean)getRequest().getSession().getAttribute("userBean");
			String id = this.getRequest().getParameter("resId");
			String resType = this.getRequest().getParameter("resType");
			
			if(TextUtil.isNull(resType)){
				resType = this.getRequest().getParameter("type");
			}
			//资源标志
			Map<String, Object> map= this.approvalService.getResMap(id, resType);
			String resStr = JsonUtil.getJsonString4Map(map);
			if(resStr.startsWith("[")){
				resStr = resStr.substring(1, resStr.length()-1);
			}
			//资源图片
			List<Map<String, Object>> imgList = this.approvalService.getResImag(id, resType);
			String imgStr = JsonUtil.getJsonString4List(imgList);
			//得到权限控制
			boolean flag = this.approvalService.getAuditRoll(userInfoBean.getUserId()+"");
			
			jsonString ="{\"resStr\":"+resStr+",\"imgStr\":"+imgStr+",\"roll\":"+flag+"}";
			this.printString(jsonString, null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void getHydrology() {
		try {
			List<Map<String, Object>> list = this.approvalService.getHydrology();
			jsonString = JsonUtil.getJsonString4List(list);
			this.printString(jsonString, null);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 得到
	 */
	public void getSendGis() {
		try {
			ApprovalTaskPojo pojo= this.approvalService.getApprovalObj(object.getId()+"");
			List<Map<String, Object>> list = this.approvalService.getResMapArea(pojo);
			String points = "";
			String lines ="";
			if(TextUtil.isNotNull(list)) {
				points +="[";
				lines +="[";
				for(int i=0;i<list.size();i++) {
					Map<String, Object> map = list.get(i);
					points +="{\"latitude\":\""+map.get("startLat")+"\",\"longitude\":\""+map.get("startLon")+"\"},";
					lines +="{\"startLat\":\""+map.get("startLat")+"\",\"startLon\":\""+map.get("startLon")+"\","
							+ "\"endLat\":\""+map.get("endLat")+"\",\"endLon\":\""+map.get("endLon")+"\""
							+ "},";
					
				}
				if(points.endsWith(",")) {
					points = points.substring(0, points.length()-1);
				}
				if(lines.endsWith(",")) {
					lines = lines.substring(0,lines.length()-1);
				}
				points +="]";
				lines +="]";
			}
			if(TextUtil.isNull(points)) {
				points ="[]";
				lines ="[]";
			}
			//资源数据
			String resStr = "{\"info\":"+approvalService.getCollectStr(pojo)+"}";
			String rejectStr = this.approvalService.getReject(pojo.getId()+"");
			jsonString ="{\"points\":"+points+",\"lines\":"+lines+",\"resStr\":"+resStr+",\"reject\":"+rejectStr+"}";
			this.printString(jsonString, null);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 得到相关辐射信息
	 */
	public void getRadiate() {
		try {
			String resType = this.getRequest().getParameter("resType");
			String resId  = this.getRequest().getParameter("resId");
			String str = this.approvalService.getRadiate(resType, resId);
			this.printString(str, null);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	public void getResImage(){
		try{
			String id = this.getRequest().getParameter("resId");
			String type = this.getRequest().getParameter("type");
			//资源图片
			List<Map<String, Object>> imgList = this.approvalService.getResImag(id, type);
			String imgStr = JsonUtil.getJsonString4List(imgList);
			jsonString ="{\"imgStr\":"+imgStr+"}";
			this.printString(jsonString, null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 得到资树
	 */
	public void getSiteTree(){
		try{
			String id = this.getRequest().getParameter("siteId");
			String type = this.getRequest().getParameter("type");
			if(type.equals("station")){
				StationBaseInfoBean site = (StationBaseInfoBean) this.approvalService.getResObject(id, type);
				jsonString =JsonUtil.getJsonString4JavaPOJO(site);
			}
			this.printString(jsonString, null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 得到站点拓扑
	 */
	public void getSiteTopo(){
		try{
			String id = this.getRequest().getParameter("siteId");
			jsonString = this.approvalService.getSiteTopo(id);
			if(jsonString.startsWith("[")){
				jsonString = jsonString.substring(1, jsonString.length()-1);
			}
			this.printString(jsonString, null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 得到光交箱拓扑
	 */
	public void getOpticalTopo() {
		try {
			String eid = this.getRequest().getParameter("eid");
			jsonString = this.approvalService.getOpticalTopo(eid);
			if(jsonString.startsWith("[")){
				jsonString = jsonString.substring(1, jsonString.length()-1);
			}
			this.printString(jsonString, null);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 得到机房拓扑
	 */
	public void getGeneratorTopo(){
		try{
			String id = this.getRequest().getParameter("siteId");
			jsonString = this.approvalService.getResStr(id,"station");
			this.printString(jsonString, null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 得到机架拓扑
	 */
	public void getEqutTopo(){
		try{
			String id = this.getRequest().getParameter("generatorId");
			jsonString = this.approvalService.getResStr(id, "generator");
			this.printString(jsonString, null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 得到机架中的详细信息
	 */
	public void getRackTopo(){
		try{
			String eid = this.getRequest().getParameter("rackId");
			if(!(eid.startsWith("EQU")) && !(eid.startsWith("EIU"))){
				eid = "EQU_"+eid;
			}
			jsonString = this.approvalService.getResStr(eid, "rack");
			this.printString(jsonString, null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 得到工单的站点信息
	 */
	public void getTaskSite(){
		try{
			String taskId = this.getRequest().getParameter("taskId");
			String result = this.approvalService.getTaskSite(taskId);
			if(TextUtil.isNotNull(result)){
				this.printString(result, null);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 得到端子或
	 * 端口拓扑
	 */
	public void getPointTopo(){
		try{
			String id = this.getRequest().getParameter("id");
			String type = this.getRequest().getParameter("type");
			jsonString = this.approvalService.getPointJson(id, type);
			this.printString(jsonString, null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 得到资源树
	 */
	public void getResTree(){
		try{
			String parentId = this.getRequest().getParameter("parentId");
			String parentType = this.getRequest().getParameter("parentType");
			jsonString = this.approvalService.getResStr(parentId, parentType);
			this.printString("\""+jsonString+"\"", null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 得到提示框要显示的内容
	 */
	public void getResContent(){
		try{
			String resId = this.getRequest().getParameter("resId");
			String resType = this.getRequest().getParameter("resType");
			Map<String, Object> map = this.approvalService.getResMap(resId, resType);
			jsonString = JsonUtil.getJsonString4Map(map);
			if(jsonString.startsWith("[{\"")){
				jsonString = jsonString.substring(3, jsonString.length()-3);
			}
			this.printString("\""+jsonString+"\"", null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 同步提交数据
	 */
	public void sendResObj(){
		try{
			UserInfoBean userInfoBean=(UserInfoBean)getRequest().getSession().getAttribute("userBean");
			String id = "";
			if(this.getRequest().getParameter("id") != null ) {
				id = this.getRequest().getParameter("id");
			}else {
				id = this.getRequest().getParameter("resId");
			}
			String type = this.getRequest().getParameter("type");
			String imName = this.getRequest().getParameter("resName");
			
			Map<String, Object> map = this.approvalService.getResMap(id, type);
			String resId = map.get("imId")+"";
			String resName = map.get("资源名称")+"";
			String upTime = map.get("修改时间")+"";
			String resNum = map.get("综资标识")+"";
			if(resNum.equals("null")){
				resNum = null;
			}
			
			String del = map.get("del")+"";
			String doType = "";
			
			String result = "";
			if(TextUtil.isNull(resNum) && del.equals("0")){
				doType = "add";
			}
			if(TextUtil.isNotNull(resNum) && TextUtil.isNotNull(upTime)){
				doType = "update";
			}
			if(TextUtil.isNotNull(resNum) && del.equals("1")){
				doType = "del";
			}
			
			//标石操作
			if(type.equals("stone")){
				StoneInfoBean stone = (StoneInfoBean) this.approvalService.getResObject(id,type);
				stone.setDataQualitier(userInfoBean.getRealname());
				if(doType.equals("add")){
					result = this.irmsOutLineService.addStone(stone);
				}
				if(doType.equals("update")){
					this.irmsOutLineService.moveStone(stone);
					result  ="success";
				}
				if(doType.equals("del")){
					this.irmsOutLineService.delStone(stone);
					result = "success";
				}
			}
			//人手井
			if(type.equals("well")){
				WellInfoBean well = (WellInfoBean) this.approvalService.getResObject(id, type);
				well.setDataQualityPrincipal(userInfoBean.getRealname());
				if(well.getRegion().contains("null")) {
					result ="error_未获取位置信息!";
				}else {
					//管井名称
					if(!(well.getWellName().equals(imName))) {
						well.setWellName(imName);
					}
					if(doType.equals("add")){
						result = this.irmsOutLineService.addWell(well);
						if(this.irmsOutLineService.isNumeric(result)) {
							//如果返回的为数字信息
							List<Map<String, Object>> segList = this.approvalService.getPipeByWell(well.getWellId()+"");
							if(TextUtil.isNotNull(segList)) {
								for(Map<String, Object> segMap : segList) {
									String pipeId = segMap.get("pipeId")+"";
									PipeSegmentInfoBean pipe = (PipeSegmentInfoBean) this.approvalService.getResObject(pipeId, "pipe");
									//如果是井为管道起始井
									if(well.getWellId().equals(pipe.getStartDeviceId())) {
										pipe.setPipeSegmentName(well.getWellName()+"-"+pipe.getEndDeviceName()+"管道段");
										pipe.setStartDeviceName(well.getWellName());
									}
									//如果井为管道末端
									if(well.getWellId().equals(pipe.getEndDeviceId())) {
										pipe.setEndDeviceName(well.getWellName());
										pipe.setPipeSegmentName(pipe.getStartDeviceName()+"-"+well.getWellName()+"管道段");
									}
									pipe.setDataQualityPrincipal(userInfoBean.getRealname());
									this.irmsOutLineService.addPipeSeg(pipe);
								}
								
							}
						}
					}
					if(doType.equals("update")){
						this.irmsOutLineService.moveWell(well);
						result = "success";
					}
					if(doType.equals("del")){
						this.irmsOutLineService.delWell(well);
						result = "success";
					}
				}
			}
			//电杆
			if(type.equals("pole")){
				PoleInfoBean pole = (PoleInfoBean) this.approvalService.getResObject(id, type);
				if(doType.equals("add")){
					if(!(pole.getPoleName().equals(imName))) {
						pole.setPoleName(imName);
					}
					pole.setDataQualityPrincipal(userInfoBean.getRealname());
					result = this.irmsOutLineService.addPole(pole);
					if(this.irmsOutLineService.isNumeric(result)) {
						 List<Map<String, Object>> segList = this.approvalService.getPlineByPole(pole.getPoleId()+"");
						 if(TextUtil.isNotNull(segList)) {
							 for(Map<String, Object> segMap : segList) {
								 String pipeId = segMap.get("pipeId")+"";
								 PolelineSegmentInfoBean poleSeg = (PolelineSegmentInfoBean) this.approvalService.getResObject(pipeId, "poleLine");
								 if(TextUtil.isNull(poleSeg.getMaintenanceAreaName())) {
									 poleSeg.setMaintenanceAreaName(pole.getRegion());
								 }
								 if(poleSeg.getMaintenanceAreaName().contains("null")) {
									 poleSeg.setMaintenanceAreaName(pole.getRegion());
								 }
								 poleSeg.setDataQualityPrincipal(userInfoBean.getRealname());
								 this.irmsOutLineService.addPoleLine(poleSeg);
							 }
						 }
					}
				}
				if(doType.equals("update")){
					this.irmsOutLineService.movePole(pole);
					result = "success";
				}
				if(doType.equals("del")){
					this.irmsOutLineService.delPole(pole);
					result = "success";
				}
			}
			//直埋
			if(type.equals("buried")){
				BuriedPartObj buried = (BuriedPartObj) this.approvalService.getResObject(id, type);
				//增加直埋段
				if(doType.equals("add")){
					result = this.irmsOutLineService.addBuried(buried);
				}
				if(doType.equals("update")){
					result = this.irmsOutLineService.upBuried(buried);
				}
			}
			//杆路
			if(type.equals("poleLine")){
				PolelineSegmentInfoBean poleLine = (PolelineSegmentInfoBean) this.approvalService.getResObject(id, type);
				if(doType.equals("add")){
					result = this.irmsOutLineService.addPoleLine(poleLine);
				}
				//修改杆路段
				if(doType.equals("update")){
					result = this.irmsOutLineService.upPoleLine(poleLine);
				}
			}
			//引上段
			if(type.equals("leadup")){
				LeadupPojo leadUp = (LeadupPojo) this.approvalService.getResObject(id, type);
				if(doType.equals("add")){
					result = this.irmsOutLineService.addLeadUp(leadUp);
				}
			}
			//管道
			if(type.equals("pipe")){
				PipeSegmentInfoBean pipe = (PipeSegmentInfoBean) this.approvalService.getResObject(id, type);
				if(doType.equals("add")){
					result = this.irmsOutLineService.addPipeSeg(pipe);
				}
				//修改管道段
				if(doType.equals("update")){
					result = this.irmsOutLineService.upPipeSeg(pipe);
				}
			}
			//站点
			if(type.equals("station")){
				StationBaseInfoBean site = (StationBaseInfoBean) this.approvalService.getResObject(id, type);
				if(doType.equals("update")){
					this.irmsStationService.movStation(site);
					new MagSite(site).start();
					result = "success";
				}
			}
			//机房
			if(type.equals("generator")){
				GeneratorInfoBean gener = (GeneratorInfoBean) this.approvalService.getResObject(id, type);
				new AddEqut(gener).start();
				result= "success";
			}
			//机架
			if(type.equals("rack")){
				EqutInfoBean equt = (EqutInfoBean) this.approvalService.getResObject(id,type);
				if(TextUtil.isNull(equt.getResNum())){
					if(equt.getDel().equals("0")) {
						this.irmsStationService.insertOdf(equt);
						new AddOdm(equt).start();
					}
				}else {
					//删除数据
					if(equt.getDel().equals(1)) {
						this.irmsStationService.delRack(equt);
					}
					//修改数据
					if(equt.getDel().equals(0)) {
						this.irmsStationService.updateRack(equt);
						new AddOdm(equt).start();
					}
				}
				result = "success";
			}
			//光交箱
			if(type.equals("optical")){
				EqutInfoBean equt = (EqutInfoBean) this.approvalService.getResObject(id,type);
				if(doType.equals("add") || TextUtil.isNull(equt.getResNum())) {
					if(TextUtil.isNull(equt.getResNum())) {
						this.irmsOpticTranService.addOptiTranBox(equt);
					}
				}else {
					this.irmsOpticTranService.moveOptiTranBox(equt);
				}
				result = "success";
			}
			this.approvalService.delRejectRes(userInfoBean.getUsername(), type, id);
			this.printString(result, null);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * 记录驳回点 
	 */
	public void rejectRes(){
		try{
			UserInfoBean userInfoBean=(UserInfoBean)getRequest().getSession().getAttribute("userBean");
			String resId = this.getRequest().getParameter("resId");
			String resType = this.getRequest().getParameter("resType");
			String taskId = this.getRequest().getParameter("taskId");
			String resName = this.getRequest().getParameter("resName");
			String rejectStr =  this.getRequest().getParameter("rejectStr");
			String parties = this.getRequest().getParameter("parties");
			
			this.approvalService.delRejectRes(userInfoBean.getUsername(), resType, resId);
			
			String content = parties+"您好,易采提醒管线采集中您采集的:"
						   +resName+";被资源管理员驳回";
			this.approvalService.sendSmsByUser(parties, content);
			ApprovalResRejectPojo resReject = new ApprovalResRejectPojo();
			resReject.setResId(resId);
			resReject.setResType(resType);
			resReject.setTaskId(taskId);
			resReject.setResName(resName);
			resReject.setRejectStr(rejectStr);
			resReject.setRejectUser(userInfoBean.getUsername());
			int id = this.approvalService.addResReject(resReject);
			if(id >-1){
				this.printString("success", null);
			}else{
				this.printString("fail", null);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 得到资源列表
	 * @throws Exception
	 */
	public void getApprovalGrid() throws Exception{
		try{
			String id = this.getRequest().getParameter("id");
			String resType = this.getRequest().getParameter("resType");
			object = this.approvalService.getApprovalObj(id);
			if(this.getRequest().getParameter("start") == null){
				object.setStart(0);
			}else{
				object.setStart(Integer.parseInt(this.getRequest().getParameter("start")+""));
			}
			object.setLimit(length);
			List<Map<String, Object>> list = this.approvalService.getResGrid(object, resType);
			for(Map<String, Object> map : list){
				String resNum = map.get("resNum")+"";
				String deletedFlag = map.get("deletedFlag")+"";
				String updateTime = map.get("updateTime")+"";
				//判断资源
				String type = "";
				if((resNum.equals("null") || resNum.equals("")) && deletedFlag.equals("0")){
					type ="新增";
				}
				if((!(resNum.equals("null")) && !(resNum.equals(""))) && deletedFlag.equals("0") && !(updateTime.equals("null"))){
					type ="修改";
				}
				if((!(resNum.equals("null")) && !(resNum.equals(""))) && deletedFlag.equals("1") && !(updateTime.equals("null"))){
					type ="删除";
				}
				if(TextUtil.isNull(type)){
					type = "已同步";
				}
				map.put("resState", type);
			}
			int count = this.approvalService.getResCount(object, resType);
			StringBuffer result = new StringBuffer();
			JsonConfig cfg = new JsonConfig(); 
			cfg.setExcludes(new String[]{"handler","hibernateLazyInitializer"});
			result.append("{totalCount:\"" + count + "\",");
			result.append("root:").append(JsonUtil.getJsonString4List(list, cfg));
			jsonString=result.append("}").toString();
			this.printString(jsonString, null);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * 	获取任务包含的资源列表
	 * 
	 * */
	public void getResourceList()throws Exception{
		JdbcTemplate jdbcTemplate = approvalService.getTemplate();
		String gdCode = this.getRequest().getParameter("itemCode");
		List<Map<String,Object>> taskObjects = jdbcTemplate.queryForList("SELECT * FROM approval_task WHERE id = "+gdCode);
		/*获取任务对象*/
		Map<String,Object> taskObject = taskObjects.get(0);		
		String regionString = "";		
		if(taskObject.get("city")!=null && !"".equals(taskObject.get("city").toString())){
			regionString += taskObject.get("city").toString()+"_";
		}		
		if(taskObject.get("county")!=null && !"".equals(taskObject.get("county").toString())){
			regionString += taskObject.get("county").toString()+"_";
		}	
		String[] resTypes = taskObjects.get(0).get("resType").toString().split(",");
		
		String resSql = "";		
		if(resTypes.length>0){
			for(int j=0;j<resTypes.length;j++){
				if(j!=0){
					resSql+=" union ";
				}
				String resType = resTypes[j];
				Map<String,Object> resObject = jdbcTemplate.queryForMap("SELECT * FROM config_resource_task WHERE res_cn_name = '"+resType+"'");
				resSql+="select ";
					resSql+="'"+resType+"' as resType,";
					resSql+=resObject.get("id_column").toString()+" as resCode,";
					resSql+=resObject.get("name_column").toString()+" as resName,";
					resSql+=resObject.get("longitude_column").toString()+" as longitude,";
					resSql+=resObject.get("latitude_column").toString()+" as latitude,";
					resSql+=resObject.get("deleteFlag_column").toString()+" as deletedFlag,";
					resSql+=resObject.get("resNum_column").toString()+" as resNum ";
				resSql+=" from "+resObject.get("res_table_name").toString();		
				resSql+=" where "+resObject.get("operator_person_column").toString()+" = '"+taskObject.get("approvaler").toString()+"'";
				/*增加地域条件*/
				if(!"".equals(regionString)){
					resSql+=" and "+resObject.get("res_region_column").toString()+" like '%"+regionString+"%'";
				}				
			}
		}		
		List<Map<String,Object>> resources = jdbcTemplate.queryForList(resSql);		
		for(int i=0;i<resources.size();i++){
			if(resources.get(i).get("deletedFlag")!=null && 1==Integer.parseInt(resources.get(i).get("deletedFlag").toString())){
				resources.get(i).put("resState","已删除");
			}else{
				if(resources.get(i).get("resNum")!=null){
					resources.get(i).put("resState","修改");
				}else{
					resources.get(i).put("resState","新增");
				}
			}
		}
		JSONObject thisObject = new JSONObject();
		thisObject.put("total",resources.size());
		thisObject.put("items",resources);
		this.printString(thisObject.toString(), null);
	}
	
	
	/**
	 * 重置地图
	 */
	public void resetMap(){
		try{
			String id = this.getRequest().getParameter("id");
			String type = this.getRequest().getParameter("type");
			ApprovalTaskPojo pojo= this.approvalService.getApprovalObj(id);
			jsonString = "{info:";
			jsonString += this.approvalService.getMapStr(pojo, type);
			jsonString +="}";
			String result = "{success:true,message:\""+jsonString+"\"}";
			this.printString(jsonString, null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 提交审批
	 */
	public void subApprove(){
		try{
			String id = super.getRequest().getParameter("id");
			String approvalAdvice = super.getRequest().getParameter("approvalAdvice");
			ApprovalTaskPojo object = approvalService.getApprovalObj(id);
			UserInfoBean userInfoBean=(UserInfoBean)getRequest().getSession().getAttribute("userBean");
			object.setAuditer(userInfoBean.getRealname());
			object.setAuditId(userInfoBean.getUserId()+"");
			object.setTaskState("归档");
			object.setApprovalAdvice(approvalAdvice);
			this.approvalService.upApprovalObj(object);
			jsonString = "{success:true,message:\"请稍后查看，数据已转交后台处理!\"}";
			this.printString(jsonString, null);
			/*if(properties.getValueByKey("toIrms").equals("false")){
				new AllotRes(id).start();
			}*/
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 驳回审批
	 */
	public void rejectApprove(){
		try{
			String id = super.getRequest().getParameter("id");
			ApprovalTaskPojo object = approvalService.getApprovalObj(id);
			UserInfoBean userInfoBean=(UserInfoBean)getRequest().getSession().getAttribute("userBean");
			/*object.setApprovaler(userInfoBean.getUsername());*/
			object.setAuditer(userInfoBean.getRealname());
			object.setAuditId(userInfoBean.getUserId()+"");
			object.setTaskState("驳回");
			//new RejectResThread(id,userInfoBean.getUsername()).start();
			this.approvalService.upApprovalObj(object);
			String content ="您好:工单"+object.getTaskTitle()
						   +"已驳回,请在审批大厅中处理相关资源!";
			this.approvalService.sendSmsByUser(object.getSender(), content);
			jsonString = "{success:true,message:\"数据驳回!\"}";
			this.printString(jsonString, null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 提交审批工单
	 */
	public void examApprove() {
		try {
			UserInfoBean userInfoBean=(UserInfoBean)getRequest().getSession().getAttribute("userBean");
			boolean flag = this.approvalService.getAuditRoll(userInfoBean.getUserId()+"");
			if(flag){
				String id = this.getRequest().getParameter("taskId");
				ApprovalTaskPojo object = approvalService.getApprovalObj(id);
				String taskState = object.getTaskState();
				object.setAuditer(userInfoBean.getRealname());
				object.setAuditId(userInfoBean.getUserId()+"");
				ApprovalResRejectPojo rejectObj = new ApprovalResRejectPojo();
				rejectObj.setTaskId(id);
				List<ApprovalResRejectPojo> rejectList = this.approvalService.getResReject(rejectObj);
				if(TextUtil.isNotNull(rejectList)) {
					object.setTaskState("驳回");
				}else {
					object.setTaskState("归档");
					this.approvalService.delIRMStask(id,"over");
				}
				approvalService.upApprovalObj(object);
			}
			this.printString(String.valueOf(flag), null);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 驳回资源
	 * @author chenqp
	 *
	 */
	class RejectResThread extends Thread{
		private String id;
		private String userName;
		public RejectResThread(String id,String userName) {
			this.id = id;
			this.userName = userName;
		}
		@Override
		public void run() {
			ApprovalTaskPojo pojo= approvalService.getApprovalObj(id);
			String resStr = approvalService.getMapStr(pojo, "now");
			List<Map<String, Object>> list = JsonUtil.getList4Json(resStr, Map.class);
			List<ApprovalResRejectPojo> resList = new LinkedList();
			for(Map<String, Object> map: list) {
				ApprovalResRejectPojo line = new ApprovalResRejectPojo();
				line.setTaskId(id);
				line.setRejectUser(userName);
				line.setRejectStr("用户批量驳回!");
				line.setResName(map.get("name")+"");
				line.setResId(map.get("id")+"");
				line.setResType(map.get("type")+"");
				if(TextUtil.isNotNull(line.getResName()) && TextUtil.isNotNull(line.getResId())) {
					resList.add(line);
				}
				if(map.get("start")!=null) {
					MorphDynaBean start =(MorphDynaBean) map.get("start");
					if(start.get("name") !=null && TextUtil.isNotNull(start.get("name")+"")) {
						ApprovalResRejectPojo res = new ApprovalResRejectPojo();
						res.setTaskId(id);
						res.setRejectUser(userName);
						res.setRejectStr("用户批量驳回!");
						res.setResName(start.get("name")+"");
						res.setResId(start.get("id")+"");
						res.setResType(start.get("type")+"");
						if(TextUtil.isNotNull(res.getResName()) && TextUtil.isNotNull(res.getResId())) {
							resList.add(res);
						}
					}
				}
				if(map.get("end")!=null) {
					MorphDynaBean end =(MorphDynaBean) map.get("end");
					if(end.get("name") !=null && TextUtil.isNotNull(end.get("name")+"")) {
						ApprovalResRejectPojo res = new ApprovalResRejectPojo();
						res.setTaskId(id);
						res.setRejectUser(userName);
						res.setRejectStr("用户批量驳回!");
						res.setResName(end.get("name")+"");
						res.setResId(end.get("id")+"");
						res.setResType(end.get("type")+"");
						if(TextUtil.isNotNull(res.getResName()) && TextUtil.isNotNull(res.getResId())) {
							resList.add(res);
						}
					}
				}
			}
			if(TextUtil.isNotNull(resList)) {
				approvalService.batchRejectResList(resList);
			}
			super.run();
		}
	}
	
	/**
	 * 管理站点内资源
	 * @author chenqp
	 *
	 */
	class MagSite extends Thread{
		private StationBaseInfoBean station;
		public MagSite(StationBaseInfoBean station){
			this.station = station;
		}
		@Override
		public void run() {
			GeneratorInfoBean gener = new GeneratorInfoBean();
			gener.setAreano(station.getStationBaseId()+"");
			List<GeneratorInfoBean> list = approvalService.getGenerInfo(gener);
			for(GeneratorInfoBean obj : list){
				new AddEqut(obj).run();
			}
			super.run();
		}
	}
	
	
	/**
	 * 增加机房下的机架
	 * @author chenqp
	 *
	 */
	class AddEqut extends Thread{
		private GeneratorInfoBean gener;
		public AddEqut(GeneratorInfoBean gener){
			this.gener = gener;
		}
		@Override
		public void run() {
			EqutInfoBean equt = new EqutInfoBean();
			equt.setGid(gener.getGeneratorId()+"");
			List<EqutInfoBean> list = approvalService.getEqutinfo(equt);
			for(EqutInfoBean obj : list){
				//新增机架
				if(TextUtil.isNull(obj.getResNum()) && !(obj.getJijialeixing().equals(2))){
					irmsStationService.insertOdf(obj);
				}
				new AddOdm(obj).run();
			}
			super.run();
		}
	}
	
	/**
	 * 新增机架下面的ODM模块
	 * @author chenqp
	 *
	 */
	class AddOdm extends Thread{
		private EqutInfoBean equt;
		public AddOdm(EqutInfoBean equt){
			this.equt = equt;
		}
		@Override
		public void run() {
			ODMInfoBean obj = new ODMInfoBean();
			obj.setEid(equt.getEid());
			List<ODMInfoBean> list = approvalService.getOdmInfo(obj);
			for(ODMInfoBean odm : list){
				//新增odm
				if(TextUtil.isNull(odm.getResNum())){
					irmsStationService.insertOdm(odm);
				}
				if(TextUtil.isNotNull(odm.getResNum()) && odm.getDeleteFlag().equals("1")) {
					irmsStationService.delOdm(odm);
				}
			}
			super.run();
		}
	}
	
	/**
	 * 分派线程池的线程
	 * @author chenqp
	 *
	 */
	class AllotRes extends Thread{
		private String id;
		public AllotRes(String id ){
			this.id = id;
		}
		@Override
		public void run() {
			ApprovalTaskPojo object = approvalService.getApprovalObj(id);
			if(object.getResType().equals("well,pole,stone")) {
				object.setResType("电杆,标石,井,光交,光缆接头盒,光终端盒,光分纤箱,撑点");
			}
			List<Map<String, Object>> list = approvalService.getResGrid(object, object.getResType());
			ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 50, 200, TimeUnit.MILLISECONDS,
	                new ArrayBlockingQueue(5));
			List<IrmsPoint> segList = new LinkedList();
			for(int i=0;i<list.size();i++){
				Map<String, Object> map = list.get(i);
				String resType = map.get("resEnType")+"";
				IrmsPoint irmsObj = approvalService.getIrmsPoint(map);
				String resNum = map.get("resNum")+"";
				if(resNum.equals("null")){
					resNum = "";
				}
				String deletedFlag = map.get("deletedFlag")+"";
				if(deletedFlag.equals("null")){
					deletedFlag = "";
				}
				String updateTime = map.get("updateTime")+"";
				if(updateTime.equals("null")){
					updateTime = "";
				}
				//判断资源
				String type = "";
				if(TextUtil.isNull(resNum) && deletedFlag.equals("0")){
					type ="add";
				}
				if(TextUtil.isNotNull(resNum) && deletedFlag.equals("0") && TextUtil.isNotNull(updateTime)){
					type ="update";
				}
				if(TextUtil.isNotNull(resNum) && deletedFlag.equals("1")){
					type = "delete";
				}
				//外线的新增修改
				if(resType.equals("renshoujing") || resType.equals("diangan") || resType.equals("biaoshi") || resType.equals("chengdian")){
					//这是新增的数据
					if(type .equals("add")){
						AddRes addObj = new AddRes(irmsObj);
						executor.execute(addObj);
					}
					//这是修改经纬读数据
					if(type.equals("update")){
						MoveRes moveObj = new MoveRes(irmsObj);
						executor.execute(moveObj);
					}
					if(type.equals("delete")){
						DelRes delObj = new DelRes(irmsObj);
						executor.execute(delObj);
					}
					segList.add(irmsObj);
				}
				//光交箱
				if(resType.equals("guangjiaojiexiang")){
					//新增光交箱
					if(type .equals("add")){
						AddOptiTranBox addOptic = new AddOptiTranBox(map);
						executor.execute(addOptic);
					}
					//修改光交箱数据
					if(type.equals("update")){
						MoveOptiTranBox moveObj = new MoveOptiTranBox(map);
						executor.execute(moveObj);
					}
				}
				//光终端盒
				if(resType.equals("guangzhongduanhe")){
					//新增光终端盒
					if(type .equals("add")){
						AddOptTerm addOpt = new AddOptTerm(irmsObj);
						executor.execute(addOpt);
					}
					//修改光终端盒
					if(type.equals("update")){
						MoveOptTerm moveObj = new MoveOptTerm(irmsObj);
						executor.execute(moveObj);
					}
				}
				//分纤箱
				if(resType.equals("guangfenxianxiang")){
					//新增分纤箱
					if(type .equals("add")){
						AddFiberBox addOpt = new AddFiberBox(irmsObj);
						executor.execute(addOpt);
					}
					//修改分纤箱
					if(type.equals("update")){
						AddFiberBox moveObj = new AddFiberBox(irmsObj);
						executor.execute(moveObj);
					}
				}
				//接头盒
				if(resType.equals("guangjietouhe")){
					if(type .equals("add")){
						AddJoint addOpt = new AddJoint(irmsObj);
						executor.execute(addOpt);
					}
				}
	        }
	        executor.shutdown();
			if(TextUtil.isNotNull(segList)){
				new AddSeg(segList).run();
			}
		}
	}
	
	/**
	 * 端资源操作
	 * @author chenqp
	 *
	 */
	class AddSeg implements Runnable{
		private List<IrmsPoint> list;
		public AddSeg(List<IrmsPoint> list){
			this.list = list;
		}
		@Override
		public void run() {
			List<IrmsPoint> poleSegList = new LinkedList();
			List<IrmsPoint> pipeSegList = new LinkedList();
			List<IrmsPoint> buriedList = new LinkedList();
			for(IrmsPoint obj : list){
				if(obj.getType().equals("diangan")){
					poleSegList.add(obj);
				}
				if(obj.getType().equals("renshoujing")){
					pipeSegList.add(obj);
				}
				if(obj.getType().equals("biaoshi")){
					buriedList.add(obj);
				}
			}
			//杆路数据
			if(TextUtil.isNotNull(poleSegList)){
				List<PolelineSegmentInfoBean> segList = approvalService.getPoleSegBypole(poleSegList);
				for(PolelineSegmentInfoBean poleSeg : segList){
					if(TextUtil.isNull(poleSeg.getResNum())){
						irmsOutLineService.addPoleLine(poleSeg);
					}
				}
			}
			//直埋数据
			if(TextUtil.isNotNull(buriedList)){
				List<BuriedPartObj> segList = approvalService.getBuriedPartByStone(buriedList);
				for(BuriedPartObj buriedSeg : segList){
					if(TextUtil.isNull(buriedSeg.getResNum())){
						irmsOutLineService.addBuried(buriedSeg);
					}
				}
			}
			//管井数据
			if(TextUtil.isNotNull(pipeSegList)){
				List<PipeSegmentInfoBean> segList = approvalService.getPipeSegBywell(pipeSegList);
				for(PipeSegmentInfoBean pipeSeg : segList){
					if(TextUtil.isNull(pipeSeg.getResNum())){
						irmsOutLineService.addPipeSeg(pipeSeg);
					}
				}
			}
			
		}
	}
	/***
	 * 进行增加资源
	 * @author chenqp
	 *
	 */
	class AddRes implements Runnable{
		private IrmsPoint obj;
		public AddRes(IrmsPoint obj){
			this.obj= obj;
		}
		@Override
		public void run() {
			irmsOutLineService.addPoint(obj);
		}
	}
	
	/**
	 * 移动点数据
	 * @author chenqp
	 *
	 */
	class MoveRes implements Runnable{
		private IrmsPoint obj;
		public MoveRes(IrmsPoint obj){
			this.obj = obj;
		}
		@Override
		public void run() {
			irmsOutLineService.movePoint(obj);
		}
	}
	
	class DelRes implements Runnable{
		private IrmsPoint obj;
		public DelRes(IrmsPoint obj){
			this.obj = obj;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			irmsOutLineService.delPoint(obj);
		}
	}
	
	
	/**
	 * 新增光交箱
	 * @author chenqp
	 *
	 */
	class AddOptiTranBox implements Runnable{
		private Map<String, Object> map;
		public AddOptiTranBox(Map<String, Object> map){
			this.map = map;
		}
		@Override
		public void run() {
			EqutInfoBean equt = new EqutInfoBean();
			equt.setId(Integer.parseInt(map.get("resCode")+""));
			equt.setEaddr(map.get("region")+"");
			equt.setLat(map.get("latitude")+"");
			equt.setLon(map.get("longitude")+"");
			equt.setEname(map.get("resName")+"");
			equt.setDataQualityPrincipal(map.get("qualitor")+"");
			equt.setParties(map.get("maintainor")+"");
			equt.setGjxmianshu(Integer.parseInt(map.get("gjxmianshu")+""));
			irmsOpticTranService.addOptiTranBox(equt);
		}
	}
	
	/**
	 * 移动光交箱
	 * @author chenqp
	 *
	 */
	class MoveOptiTranBox implements Runnable{
		private Map<String, Object> map;
		public MoveOptiTranBox(Map<String, Object> map){
			this.map = map;
		}
		@Override
		public void run() {
			EqutInfoBean equt = new EqutInfoBean();
			equt.setId(Integer.parseInt(map.get("resCode")+""));
			equt.setEaddr(map.get("region")+"");
			equt.setLat(map.get("latitude")+"");
			equt.setLon(map.get("longitude")+"");
			equt.setEname(map.get("resName")+"");
			equt.setDataQualityPrincipal(map.get("qualitor")+"");
			equt.setParties(map.get("maintainor")+"");
			equt.setGjxmianshu(Integer.parseInt(map.get("gjxmianshu")+""));
			equt.setResNum(map.get("resNum")+"");
			irmsOpticTranService.moveOptiTranBox(equt);
		}
	}
	
	
	/**
	 * 增加光终端盒
	 * @author chenqp
	 *
	 */
	class AddOptTerm implements Runnable{
		private IrmsPoint obj;
		public AddOptTerm(IrmsPoint obj){
			this.obj = obj;
		}
		@Override
		public void run() {
			OpticalTerminalObj opt= new OpticalTerminalObj();
			opt.setId(Integer.parseInt(obj.getImId()));
			opt.setTerminalAddres(obj.getArea());
			opt.setLatitude(obj.getLatitude());
			opt.setLongitude(obj.getLongitude());
			opt.setTerminalName(obj.getResName());
			opt.setDataQualitier(obj.getQualitor());
			opt.setMaintainer(obj.getMaintainor());
			irmsOpticTranService.addOptTerm(opt);
		}
	}
	
	
	/**
	 * 移动光终端盒
	 * @author chenqp
	 *
	 */
	class MoveOptTerm implements Runnable{
		private IrmsPoint obj;
		public MoveOptTerm(IrmsPoint obj){
			this.obj =obj;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			OpticalTerminalObj opt= new OpticalTerminalObj();
			opt.setId(Integer.parseInt(obj.getImId()));
			opt.setTerminalAddres(obj.getArea());
			opt.setLatitude(obj.getLatitude());
			opt.setLongitude(obj.getLongitude());
			opt.setTerminalName(obj.getResName());
			opt.setDataQualitier(obj.getQualitor());
			opt.setMaintainer(obj.getMaintainor());
			opt.setResNum(obj.getResNum());
			irmsOpticTranService.moveOptTerm(opt);
		}
	}
	
	/**
	 * 分纤箱
	 * @author chenqp
	 *
	 */
	class AddFiberBox implements Runnable{
		private IrmsPoint obj ;
		public AddFiberBox(IrmsPoint obj){
			this.obj = obj;
		}
		@Override
		public void run() {
			FiberBoxInfoBean fiber = new FiberBoxInfoBean();
			fiber.setId(Integer.parseInt(obj.getImId()));
			fiber.setMaintainArea(obj.getArea());
			fiber.setLatitude(obj.getLatitude());
			fiber.setLongitude(obj.getLongitude());
			fiber.setFiberboxName(obj.getResName());
			fiber.setDataQualitier(obj.getQualitor());
			fiber.setMaintainer(obj.getMaintainor());
			irmsOpticTranService.addFiberBox(fiber);
		}
	}
	
	
	/**
	 * 修改分纤箱
	 * @author chenqp
	 *
	 */
	class MoveFiberBox implements Runnable{
		private IrmsPoint obj;
		public MoveFiberBox(IrmsPoint obj){
			this.obj = obj;
		}
		@Override
		public void run() {
			FiberBoxInfoBean fiber = new FiberBoxInfoBean();
			fiber.setId(Integer.parseInt(obj.getImId()));
			fiber.setMaintainArea(obj.getArea());
			fiber.setLatitude(obj.getLatitude());
			fiber.setLongitude(obj.getLongitude());
			fiber.setFiberboxName(obj.getResName());
			fiber.setDataQualitier(obj.getQualitor());
			fiber.setMaintainer(obj.getMaintainor());
			fiber.setResNum(obj.getResNum());
			irmsOpticTranService.moveFiberBox(fiber);
		}
	}
	
	
	/**
	 * 增加接口盒
	 * @author chenqp
	 *
	 */
	class AddJoint implements Runnable{
		private IrmsPoint obj;
		public AddJoint(IrmsPoint obj){
			this.obj = obj;
		}
		@Override
		public void run() {
			JointInfoBean joint = new JointInfoBean();
			joint.setJointId(Integer.parseInt(obj.getImId()));
			joint.setJointName(obj.getResName());
			joint.setDataQualityPrincipal(obj.getQualitor());
			joint.setParties(obj.getMaintainor());
			irmsOpticTranService.addOpticJoint(joint);
		}
	}
	
	
	/**
	 * 移动接头盒
	 * @author chenqp
	 *
	 */
	class MoveJoint implements Runnable{
		private IrmsPoint obj;
		public MoveJoint(IrmsPoint obj){
			this.obj = obj;
		}
		@Override
		public void run() {
			
		}
	}
	
	/**
	 * 删除工单
	 */
	public void delTask() {
		try {
			String id = this.getRequest().getParameter("id");
			ApprovalTaskPojo pojo = this.approvalService.getApprovalObj(id);
			UserInfoBean userInfoBean=(UserInfoBean)getRequest().getSession().getAttribute("userBean");
			
			if(!(pojo.getTaskState().equals("已派发"))) {
				jsonString ="{success:false,errors:'工单已领请勿删除'}";
			}else {
				if(pojo.getSendId().equals(userInfoBean.getUserId()+"")) {
					//执行删除
					pojo.setDeleteFlag("1");
					this.approvalService.upApprovalObj(pojo);
					this.approvalService.delIRMStask(pojo.getId()+"","del");
					jsonString ="{success:true,msg:'删除成功'}";
				}else {
					jsonString ="{success:false,errors:'无删除权限'}";
				}
			}
			
			this.printString(jsonString, null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 提示消息
	 */
	public void getWarnMsg() {
		try {
			UserInfoBean userInfoBean=(UserInfoBean)getRequest().getSession().getAttribute("userBean");
			Map<String, String> map = this.approvalService.getWarnMsg(userInfoBean.getUsername());
			jsonString = JsonUtil.getJsonString4Map(map);
			this.printString(jsonString, null);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * 得到站點數據
	 */
	public void getSeachSite() {
		try {
			object.setLimit(this.length);
			if(TextUtil.isNull(object.getStart())){
				object.setStart(0);
			}
			
			String siteName = this.getRequest().getParameter("siteName");
			if(TextUtil.isNotNull(siteName) && !(siteName.equals("null"))) {
				List<Map<String, Object>> siteList = this.approvalService.getSeachSite(siteName, object.getStart(), object.getStart()+this.length);
				StringBuffer result = new StringBuffer();
				result.append("{totalCount:\"" + siteList.size() + "\",");
				result.append("root:").append(JsonUtil.getJsonString4List(siteList));
				jsonString=result.append("}").toString();
			}
			this.printString(jsonString, null);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 得到站點數據
	 * @return
	 */
	public String getSiteApproval() {
		try {
			String id = this.getRequest().getParameter("id");
			this.object = this.approvalService.getApprovalObj(id);
			String sql = "select resId,generId,generNum,generName"
					+ " from approval_res where taskId ='"+object.getId()+"'";
			List<Map<String, Object>> resList = this.approvalService.getListBySql(sql);
			if(TextUtil.isNotNull(resList)) {
				Map<String, Object> resMap = resList.get(0);
				object.setResId(resMap.get("generId")+"");
				this.getRequest().setAttribute("generatId", resMap.get("generId")+"");
			}
			this.getRequest().setAttribute("jsonStr", JsonUtil.getJsonString4JavaPOJO(object));
		}catch(Exception e) {
			e.printStackTrace();
		}
		return "siteApproval";
	}
	
	
	/**
	 * 得到幾架的數據
	 */
	public void getRackStr() {
		try {
			List<Map<String, Object>> list = this.approvalService.getEqut(this.getRequest().getParameter("gid"),
					this.getRequest().getParameter("approval"));
			
			StringBuffer result = new StringBuffer();
			JSONObject thisObject = new JSONObject();
			thisObject.put("total",list.size());
			thisObject.put("items",list);
			this.printString(thisObject.toString(), null);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 得到端子ODM数据
	 */
	public void odmResList() {
		try {
			String eid = this.getRequest().getParameter("eid");
			String sql = "select o.odmName,p.pos,p.ID as id,"
					+ " DATE_FORMAT(o.creationDate,'%Y-%m-%d %H:%i:%S') as creationDate,"
					+ " DATE_FORMAT(o.lastUpdateDate,'%Y-%m-%d %H:%i:%S') as lastUpdateDate,"
					+ " p.fiberName,p.oppsite,p.jumpOptical "
					+ " from job_odm o left "
					+ " join job_pointinfo p on  o.resNum = p.odmId where o.eid='"+eid+"' and p.pos is not null "
					+ " union "
					+ " select o.odmName,p.pos,p.ID as id,"
					+ " DATE_FORMAT(o.creationDate,'%Y-%m-%d %H:%i:%S') as creationDate,"
					+ " DATE_FORMAT(o.lastUpdateDate,'%Y-%m-%d %H:%i:%S') as lastUpdateDate,"
					+ " p.fiberName,p.oppsite,p.jumpOptical "
					+ " from job_odm o left "
					+ " join job_pointinfo p on o.odmId = p.odmId where o.eid='"+eid+"' and p.pos is not null";
			List<Map<String, Object>> list = this.approvalService.getListBySql(sql);
			for(Map<String, Object> map : list) {
				String odmName = map.get("odmName")+"";
				String pos = map.get("pos")+"";
				if(pos.contains(odmName)) {
					pos = pos.replace(odmName+"-", "");
					map.put("pos", pos);
				}
			}
			JSONObject thisObject = new JSONObject();
			thisObject.put("total",list.size());
			thisObject.put("items",list);
			this.printString(thisObject.toString(), null);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public Long getFlowId() {
		// 当前时间
		long currentTime = System.currentTimeMillis();
		// 生成随机数
		int randD = (int) (Math.random() * 1000);
		// 相加得到流水号
		long flowId = currentTime + randD;
		return new Long(flowId);
	}
	
	public void printString(String string, String contentType) throws Exception {
		if(TextUtil.isNotNull(string)) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(string);
			string = m.replaceAll("");
		}
		this.getResponse().setHeader("Cache-Control", "no-cache");
		this.getResponse().setContentType(contentType);
		this.getResponse().setCharacterEncoding("UTF-8");
		PrintWriter pw = this.getResponse().getWriter();
		pw.write(string);
		pw.close();
	}
	
	public IirmsOutLineService getIrmsOutLineService() {
		return irmsOutLineService;
	}
	public void setIrmsOutLineService(IirmsOutLineService irmsOutLineService) {
		this.irmsOutLineService = irmsOutLineService;
	}
	public IapprovalTaskService getApprovalService() {
		return approvalService;
	}
	public void setApprovalService(IapprovalTaskService approvalService) {
		this.approvalService = approvalService;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public ApprovalTaskPojo getObject() {
		return object;
	}
	public void setObject(ApprovalTaskPojo object) {
		this.object = object;
	}
	public String getJsonString() {
		return jsonString;
	}
	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}
	public java.io.File getFile() {
		return file;
	}
	public void setFile(java.io.File file) {
		this.file = file;
	}
	public String getFileFileName() {
		return fileFileName;
	}
	public void setFileFileName(String fileFileName) {
		this.fileFileName = fileFileName;
	}
	public static Logger getLog() {
		return log;
	}
	@Override
	public Object getModel(){
		return object;
	}
	public PDAEqutInfoService getPdaEqutInfoService() {
		return pdaEqutInfoService;
	}
	public void setPdaEqutInfoService(PDAEqutInfoService pdaEqutInfoService) {
		this.pdaEqutInfoService = pdaEqutInfoService;
	}
	public IirmsOpticTranService getIrmsOpticTranService() {
		return irmsOpticTranService;
	}
	public void setIrmsOpticTranService(IirmsOpticTranService irmsOpticTranService) {
		this.irmsOpticTranService = irmsOpticTranService;
	}
	public IirmsStationService getIrmsStationService() {
		return irmsStationService;
	}
	public void setIrmsStationService(IirmsStationService irmsStationService) {
		this.irmsStationService = irmsStationService;
	}
	public ISyncSiteDataService getSyncSiteDataService() {
		return syncSiteDataService;
	}
	public void setSyncSiteDataService(ISyncSiteDataService syncSiteDataService) {
		this.syncSiteDataService = syncSiteDataService;
	}
}
