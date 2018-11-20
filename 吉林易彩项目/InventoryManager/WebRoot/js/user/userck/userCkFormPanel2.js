Ext.namespace("com.increase.cen.user");
var date = [];
com.increase.cen.user.UserCkForm = Ext.extend(Ext.form.FormPanel, {
	bodyStyle : "padding:5px;",
	frame : true,
	bodyBorder : false,
	labelAlign : "right",
	buttonAlign : 'center',
	labelWidth : 100,
	width : 500,
	height : 400,
	initComponent : function(config) {
		// 一页显示多少条数据
		limit = Ext.getDom("limit").value;
		// 启动悬停提示
		Ext.QuickTips.init();
		// 提示的方式
		Ext.form.Field.prototype.msgTarget = 'qtip';
		
		
		this.userid = new Ext.form.TextField({
			id : "userId",
			name : "userid",
			fieldLabel : "用户id",
			width : 200,
			hidden : true
		});
		this.selid = new Ext.form.TextField({
			id : "usercks",
			name : "usercks", 
			fieldLabel : "用户id",
			width : 200,
			hidden : true
		});
		var setting = {
    		 view: {
       		  selectedMulti: false
         	}
  		  }
 
		
		var userid = Ext.getDom("userid").value;
		this.tree = new Ext.tree.TreePanel({
			id : "userCkTree",
			width:430,
			height:300,
			border : false,
			autoScroll : true,
			checkModel: "cascade",
			frame : true,// 美化界面
			animate : true, // 动画效果
			enableDD : true,
			//enableDrag:true,
			//renderTo : 'tree',
			
			root : {
				id : "rootNode",
				text : '总地区',
				leaf : false
			},
			rootVisible : false,// false将隐藏root节点
			dataUrl : 'user!getUserCkTree2.action?userid='+userid ,
			listeners : {
				"load" : function(node) {
					date = [];
					initData(node,userid);
				},
				"checkchange":function(node, checked) {
					var handle;
					if(checked){
						date = addNode(node,date,userid);
						handle = "add";
					}else{
						date = renoveNode(node,date,userid);
						handle = "remove"
					}
					
		            node.attributes.checked = checked;
		            node.eachChild(function(child) {
		          		child.ui.toggleCheck(checked);
		            	child.attributes.checked = checked;
		            	child.fireEvent('checkchange', child,checked);
		          	})
				}
			}
		});
		
		this.tree.selModel=new Ext.tree.MultiSelectionModel();
		
//		this.tree.on("",function(e){
			
	//		this.selModel.getSelectedNodes()
			
		//	e.dropNode.attributes="";
		
	//	});
//	this.tree.on("beforenodedrop", function(e){
//		var b=false;
//	   var s=e.dropNode.attributes;
//	    var t = e.target.attributes; 
//	    if(t.beanType=="equt"){
//	     		return;   
//	        }else{
//	        	 Ext.Msg.show({
//					title : '提示',
//					width : 300,
//					msg : '<img  align="center" hspace="5"/>确定将设备"'+s.text+'"修改到地区"'+t.text+'"吗？',
//					buttons : {
//					ok : "确定",
//					no:"取消"
//					}
//				});				
//				return b;
//	        }
 //   	});
	
		
		this.tree.on("nodedrop", function(e){ 	
			var s=e.dropNode.attributes;
	        var t = e.target.attributes; 
	        if(t.beanType=="equt"){
	     		return;   
	        }else{
	        	var dropId;
	        	if(s.beanType=="equt"){
	        		dropId=s.id;
	        	}else{
	        		dropId=s.domainCode
	        	}
	        	Ext.Ajax.request({
						url : 'orderSetting!updateEqutDomain.action',
						method : 'post',
						params : {
							beanType:s.beanType,
							dropId:dropId,
							targetId:t.domainCode,
								},	
							success : function(response) {
									Ext.Msg.show({
										title : '提示',
										width : 300,
										msg : '<img  align="center" hspace="5"/>设备:"'+s.text+'"修改到地区"'+t.text+'"成功',
										buttons : {
											ok : "确定"
										}
									});		
							
						},
						failure : function() {
							alert(a);
							Ext.Msg.show({
										title : '提示',
										width : 300,
										msg : '<img  align="center" hspace="5"/>设备:"'+s.text+'"修改到地区"'+t.text+'"失败',
										buttons : {
											ok : "确定"
										}
									});
								}
						});					
		      	}
  		 });
		this.items = [{
			xtype : 'panel',
			bodyStyle : 'padding-left:78%;',
			items : []
		}, {
			xtype : 'fieldset',
			title : '设备列表',
			collapsible : true,
			items : [this.selid,this.userid,this.tree]
		}];

		// 按钮
		this.buttons = [{
			text : "返回",
			icon : "imgs/back.png",
			cls : "x-btn-text-icon",
			tooltip : '返回',
			tooltipType : 'qtip',
			handler : this.btnClose.createDelegate(this)
		}];
		

		// 重写initComponent superclass.initComponent.call(this);来调用父类的初始化方法.
		com.increase.cen.user.UserCkForm.superclass.initComponent.call(this);
	},
	// 保存按钮事件
	btnSubmit : function() {
		if (!this.getForm().isValid()) {
			return;
		}
		Ext.getCmp("usercks").setValue(Ext.encode(date));
		this.getForm().doAction("submit", {
			url : 'user!editUserCk.action',
			method : 'post',
			submitEmptyText : false,
			success : function(form, action) {
				Ext.Msg.show({
					title : '提示',
					width : 300,
					msg : '<img src="imgs/tip_success.png" align="center" hspace="30" />'
							+ '设置白名单信息成功!',
					buttons : {
						ok : "确定"
					}
				});
				Ext.getCmp('userCkWindow').close();// 窗口隐藏
				Ext.getCmp('userList').getStore().load({
					params : {
						start : 0,
						limit : limit,
						"user.username" : "",
						"user.realname" : "",
						"user.areano" : Ext.getDom("domainCode").value
					}
				});// 表格加载数据
				Ext.getCmp('userList').getView().refresh();// 表格刷新
			},
			failure : function(form, action) {
				Ext.Msg.show({
					title : '提示',
					width : 300,
					msg : '<img src="imgs/tip_error.png" align="center" hspace="30"/>'
							+ '设置白名单信息失败',
					buttons : {
						ok : "确定"
					}
				});
			}
		});
	},
	// 返回操作
	btnClose : function() {
		// 窗口隐藏
		Ext.getCmp('userCkWindow').close();
	}
	
});
Ext.reg("userCkForm", com.increase.cen.user.UserCkForm);


function addNode(node,date,userid){
	var d = {"userid":userid,"beanType":node.attributes.beanType,"eid":node.id};
	if(date.length==0){
		date.push(d);
	}else{
		var leng = date.length;
		var nohas = true;
		for(var i=0;i<leng;i++){
			var h = date[i]
			if(h.eid == d.eid){
				nohas = false;
			}
		}
		if(nohas){
			date.push(d);
		}
		
	}
	
	return date;
}

function renoveNode(node,date,userid){
	for(var i=0;i<date.length;i++){
		var d = date[i]
		if(d.eid == node.id){
			date.splice(i,1);
		}
	}
	var parentNode = node.parentNode;
	if(parentNode.id =='rootNode'){
		return date ;
	}
	renoveNode(parentNode,date,userid);
	return date;
}

function initData(node,userid){
	 node.hasChildNodes()
	 node.eachChild(function(child) {
		if(child.attributes.checked){
			var d = {"userid":userid,"beanType":child.attributes.beanType,"eid":child.id};
			date.push(d);
		}
	})
}

