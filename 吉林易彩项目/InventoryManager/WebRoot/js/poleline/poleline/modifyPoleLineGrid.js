Ext.namespace("com.increase.cen.poleline");
var limit = null;
com.increase.cen.poleline.ModifyPoleLine = Ext.extend(Ext.form.FormPanel, {
	bodyStyle : "padding:5px;",
	frame : true,
	bodyBorder : false,
	labelAlign : "right",
	buttonAlign : 'center',
	labelWidth : 120,
	width : 800,
	height : 300,
	initComponent : function(config) {
		// 获取一页的记录数
		limit = Ext.getDom("limit").value;
		// 启动悬停提示
		Ext.QuickTips.init();
		// 提示的方式
		Ext.form.Field.prototype.msgTarget = 'qtip';
		// 验证光缆信息是否存在
		var userExist = false;
		var idIsExist = true;
		// 光缆id
		var poleLineId = new Ext.form.TextField({
			id : "modifyPoleLineId",
			name : "polelineInfoBean.poleLineId",
			fieldLabel : "杆路ID",
			width : 300,
			hidden : true
		});
		var plname=new Ext.form.TextField({
			id : 'modifyPLName',
			name : "poleLineName",
			fieldLabel : "杆路名称",
			width : 300,
			hidden : true
		});
		var row1={
			layout:'column',
			items:[{
				height:30,
				layout:'form',
				items:[{
					id : 'modifyPoleLineName',
					name : "polelineInfoBean.poleLineName",
					xtype : 'textfield',
					fieldLabel : "杆路名称",
					allowBlank : false,
					blankText : '杆路名称不能为空',
					emptyText : "请输入杆路名称",
					width : 150
				}]
			},{
				layout:'form',
				items:[{
					id : 'modifyPoleLineCode',
					name : "polelineInfoBean.poleLineCode",
					xtype : 'textfield',
					fieldLabel : "杆路编码",
					emptyText : "请输入杆路编码",
					width : 150
				}]
			}]
		}
		this.levelStore = new Ext.data.SimpleStore({
    		fields : ['value', 'text'],
    		data : [['', ''],['1', '一干'], ['2', '二干'], ['3', '本地']]
    	})
    	var row2={
			layout:'column',
			items:[{
				height:30,
				layout:'form',
				items:[{
					id : "modifyPoleLineLevel",
					name : "polelineInfoBean.poleLineLevel",
					xtype : 'combo',
					typeAhead: true,
		        	triggerAction: 'all',
					fieldLabel : "杆路级别",
					emptyText : "请选择杆路级别",
					blankText : '杆路级别不能为空',
					forceSelection: true,
		         	mode: 'local',
		        	width : 150,
		         	store: this.levelStore,
		         	valueField: 'value',   //下拉框具体的值（例如值为SM，则显示的内容即为‘短信’）
		       		displayField: 'text',//下拉框显示内容
		       		hiddenName:'polelineInfoBean.poleLineLevel',//创建一个表单隐藏域来存储输入项的值
		       		editable:false//设置为false以阻止用户直接向该输入项输入文本
				}]
			},{
				layout:'form',
				items:[{
					id : 'modifyPoleLineLength',
					name : "polelineInfoBean.poleLineLength",
					xtype : 'textfield',
					fieldLabel : "杆路长度（米）",
					emptyText : "请输入杆路长度且只能为数字",
					width : 150,
					invalidText : '请输入正确的杆路长度！',
					validationEvent : 'keydown',
					validationDelay : 100,
					validator : function(thisText) {
						if (thisText == "") {
							return true;
						}
						var reg=/^[0-9]+\.{0,1}\d*$/;
						if(!reg.test(thisText)){
							Ext.getCmp('modifyPoleLineLength').markInvalid('填写格式不正确，请重新填写!');
							return false;
						}else{
							return true;
						}
					}
				}]
			}]
		}
		var row3={
			layout:'column',
			items:[{
				height : 30,
				layout : 'form', // 从上往下布局
				items : [{
					xtype : 'textfield',
					id : "modifyStartDeviceId",
					name : "polelineInfoBean.startDeviceId",
					width : 5,
					hidden : true
				}, {
					//xtype : 'compositefield',
					layout:'column',
					fieldLabel : '起始设施名称',
					items : [{
						id : 'modifyStartDeviceName',
						name : 'polelineInfoBean.startDeviceName',
						xtype : 'textfield',
						emptyText : "请选择起始设施",
						//fieldLabel : '起始设施名称',
						width : 150,
						readOnly : true
					}, {
						xtype : 'button',
						text : '选择插入',
						width : 50,
						style : 'margin-left:5px',
						handler : function() {
							showPoleGrid_a();
						}
					}, {
						xtype : 'button',
						text : '清空',
						width : 23,
						style : 'margin-left:5px',
						handler : function() {
							Ext.getCmp("modifyStartDeviceId").setValue("");
							Ext.getCmp("modifyStartDeviceName").setValue("");
						}
					}]
				}]
			},{
				height : 30,
				layout : 'form', // 从上往下布局
				items : [{
					xtype : 'textfield',
					id : "modifyEndDeviceId",
					name : "polelineInfoBean.endDeviceId",
					width : 5,
					hidden : true
				}, {
					//xtype : 'compositefield',
					layout:'column',
					fieldLabel : '终止设施名称',
					items : [{
						id : 'modifyEndDeviceName',
						name : 'polelineInfoBean.endDeviceName',
						xtype : 'textfield',
						emptyText : "请选择终止设施",
						//fieldLabel : '终止设施名称',
						width : 150,
						readOnly : true
					}, {
						xtype : 'button',
						text : '选择插入',
						width : 50,
						style : 'margin-left:5px',
						handler : function() {
							showPoleGrid_z();
						}
					}, {
						xtype : 'button',
						text : '清空',
						width : 25,
						style : 'margin-left:5px',
						handler : function() {
							Ext.getCmp("modifyEndDeviceId").setValue("");
							Ext.getCmp("modifyEndDeviceName").setValue("");
						}
					}]
				}]
			}]
		}
    	this.maintenanceStore = new Ext.data.SimpleStore({
    		fields : ['value', 'text'],
    		data : [['', ''],['1', '自维'], ['2', '代维']]
    	})
    	var row4={
			layout:'column',
			items:[{
				height:30,
				layout:'form',
				items:[{
					id : 'modifyPurchasedMaintenanceTime',
					name : "polelineInfoBean.purchasedMaintenanceTime",
					xtype : 'textfield',
					fieldLabel : "购买年限",
					emptyText : "请输入购买年限且只能为正整数",
					width : 150,
					invalidText : '请输入正确的购买年限！',
					validationEvent : 'keydown',
					validationDelay : 100,
					validator : function(thisText) {
						if (thisText == "") {
							return true;
						}
						var reg=/^[1-9]\d*$/;
						if(!reg.test(thisText)){
							Ext.getCmp('modifyPurchasedMaintenanceTime').markInvalid('填写格式不正确，请重新填写!');
							return false;
						}else{
							return true;
						}
					}
				}]
			},{
				layout:'form',
				items:[{
					id : "modifyMaintenanceModeEnumId",
					name : "polelineInfoBean.maintenanceModeEnumId",
					xtype : 'combo',
					typeAhead: true,
		        	triggerAction: 'all',
					fieldLabel : "维护方式",
					emptyText : "请选择维护方式",
					blankText : '维护方式不能为空',
					forceSelection: true,
		         	mode: 'local',
		        	width : 150,
		         	store: this.maintenanceStore,
		         	valueField: 'value',   //下拉框具体的值（例如值为SM，则显示的内容即为‘短信’）
		       		displayField: 'text',//下拉框显示内容
		       		hiddenName:'polelineInfoBean.maintenanceModeEnumId',//创建一个表单隐藏域来存储输入项的值
		       		editable:false//设置为false以阻止用户直接向该输入项输入文本
				}]
			}]
		}
    	var row5={
			layout:'column',
			items:[{
				height:30,
				layout:'form',
				items:[{
					xtype : 'textfield',
					id : 'modifyMaintenanceOrgId',
					name : "polelineInfoBean.maintenanceOrgId",
					fieldLabel : "维护单位",
					emptyText : "请输入维护单位",
					width : 150
				}]
			},{
				layout:'form',
				items:[{
					id : 'modifyThirdPartyMaintenanceOrg',
					name : "polelineInfoBean.thirdPartyMaintenanceOrg",
					xtype : 'textfield',
					fieldLabel : "第三方维护单位",
					emptyText : "请输入第三方维护单位",
					width : 150
				}]
			}]
		}
    	/*var maintenanceOrgId=new Ext.form.TextField({
			id : 'modifyMaintenanceOrgId',
			name : "polelineInfoBean.maintenanceOrgId",
			fieldLabel : "维护单位",
			emptyText : "请输入维护单位",
			width : 300
		});
		var thirdPartyMaintenanceOrg=new Ext.form.TextField({
			id : 'modifyThirdPartyMaintenanceOrg',
			name : "polelineInfoBean.thirdPartyMaintenanceOrg",
			fieldLabel : "第三方维护单位",
			emptyText : "请输入第三方维护单位",
			width : 300
		});*/
		this.maintenanceTypeStore = new Ext.data.SimpleStore({
    		fields : ['value', 'text'],
    		data : [['', ''],['1', '硬件维修'], ['2', '紧急备件支持'], ['3', '软件补丁版本支持'], ['4', '技术支持服务（电话咨询、电话支持、远程支持、现场支持、网络预警、重要通信保障）'], ['5', '无']]
    	})
    	var row6={
			layout:'column',
			items:[{
				height:30,
				layout:'form',
				items:[{
					id : 'modifyRenewalExpirationDate',
					name : 'polelineInfoBean.renewalExpirationDate',
					xtype : "datefield",
					format : 'Y-m-d',// 日期格式
					fieldLabel : '续保截止日期',
					emptyText : "请选择续保截止日期",
					width : 150
				}]
			},{
				layout:'form',
				labelWidth : 215,
				items:[{
					id : "modifyMaintenanceTypeEnumId",
					name : "polelineInfoBean.maintenanceTypeEnumId",
					xtype : 'combo',
					typeAhead: true,
		        	triggerAction: 'all',
					fieldLabel : "维保类型",
					emptyText : "请选择维保类型",
					blankText : '维保类型不能为空',
					forceSelection: true,
		         	mode: 'local',
		        	width : 150,
		         	store: this.maintenanceTypeStore,
		         	valueField: 'value',   //下拉框具体的值（例如值为SM，则显示的内容即为‘短信’）
		       		displayField: 'text',//下拉框显示内容
		       		hiddenName:'polelineInfoBean.maintenanceTypeEnumId',//创建一个表单隐藏域来存储输入项的值
		       		editable:false//设置为false以阻止用户直接向该输入项输入文本
				}]
			}]
		}
		/*var maintenanceTypeEnumId=new Ext.form.ComboBox({
    		id : "modifyMaintenanceTypeEnumId",
			name : "polelineInfoBean.maintenanceTypeEnumId",
			typeAhead: true,
        	triggerAction: 'all',
			fieldLabel : "维保类型",
			emptyText : "请选择维保类型",
			blankText : '维保类型不能为空',
			forceSelection: true,
         	mode: 'local',
        	width : 300,
         	store: this.maintenanceTypeStore,
         	valueField: 'value',   //下拉框具体的值（例如值为SM，则显示的内容即为‘短信’）
       		displayField: 'text',//下拉框显示内容
       		hiddenName:'polelineInfoBean.maintenanceTypeEnumId',//创建一个表单隐藏域来存储输入项的值
       		editable:false//设置为false以阻止用户直接向该输入项输入文本
    	});*/
    	var row7={
			layout:'column',
			items:[{
				height:30,
				layout:'form',
				items:[{
					id : 'modifyProjectName',
					name : "polelineInfoBean.projectName",
					xtype : 'textfield',
					fieldLabel : "项目名称",
					emptyText : "请输入项目名称",
					width : 150
				}]
			},{
				layout:'form',
				labelWidth : 215,
				items:[{
					id : 'modifyProjectCode',
					name : "polelineInfoBean.projectCode",
					xtype : 'textfield',
					fieldLabel : "项目编号",
					emptyText : "请输入项目编号",
					width : 150
				}]
			}]
		}
    	/*var projectName=new Ext.form.TextField({
			id : 'modifyProjectName',
			name : "polelineInfoBean.projectName",
			fieldLabel : "项目名称",
			emptyText : "请输入项目名称",
			width : 300
		});
		var projectCode=new Ext.form.TextField({
			id : 'modifyProjectCode',
			name : "polelineInfoBean.projectCode",
			fieldLabel : "项目编号",
			emptyText : "请输入项目编号",
			width : 300
		});*/
    	this.resourceLifePeriodEnumId = new Ext.data.SimpleStore({
    		fields : ['value', 'text'],
    		data : [['', ''],['1', '设计状态'], ['2', '工程建设期（入网带业务前）'], ['3', '工程可用期（已入网带业务）'], 
    		['4', '工程初验后试运行'], ['5', '工程终验后在网'], ['6', '退网状态']]
    	})
    	var row8={
			layout:'column',
			items:[{
				height:30,
				layout:'form',
				items:[{
					id : 'modifyProjectWarrantyExpireDate',
					name : 'polelineInfoBean.projectWarrantyExpireDate',
					xtype : "datefield",
					format : 'Y-m-d',// 日期格式
					fieldLabel : '保修截止日期',
					emptyText : "请选择保修截止日期",
					width : 150
				}]
			},{
				layout:'form',
				labelWidth : 215,
				items:[{
					id : "modifyResourceLifePeriodEnumId",
					name : "polelineInfoBean.resourceLifePeriodEnumId",
					xtype : 'combo',
					typeAhead: true,
		        	triggerAction: 'all',
					fieldLabel : "资源生命周期",
					emptyText : "请选择资源生命周期",
					blankText : '资源生命周期不能为空',
					forceSelection: true,
		         	mode: 'local',
		        	width : 150,
		         	store: this.resourceLifePeriodEnumId,
		         	valueField: 'value',   //下拉框具体的值（例如值为SM，则显示的内容即为‘短信’）
		       		displayField: 'text',//下拉框显示内容
		       		hiddenName:'polelineInfoBean.resourceLifePeriodEnumId',//创建一个表单隐藏域来存储输入项的值
		       		editable:false//设置为false以阻止用户直接向该输入项输入文本
				}]
			}]
		}
		/*var resourceLifePeriodEnumId=new Ext.form.ComboBox({
    		id : "modifyResourceLifePeriodEnumId",
			name : "polelineInfoBean.resourceLifePeriodEnumId",
			typeAhead: true,
        	triggerAction: 'all',
			fieldLabel : "资源生命周期",
			emptyText : "请选择资源生命周期",
			blankText : '资源生命周期不能为空',
			forceSelection: true,
         	mode: 'local',
        	width : 300,
         	store: this.resourceLifePeriodEnumId,
         	valueField: 'value',   //下拉框具体的值（例如值为SM，则显示的内容即为‘短信’）
       		displayField: 'text',//下拉框显示内容
       		hiddenName:'polelineInfoBean.resourceLifePeriodEnumId',//创建一个表单隐藏域来存储输入项的值
       		editable:false//设置为false以阻止用户直接向该输入项输入文本
    	});*/
    	var comboxWithTree = new Ext.form.ComboBox({
			store : new Ext.data.SimpleStore({
				fields : [],
				data : [[]]
			}),
			id : 'comboxWithTree',
			fieldLabel : '所属局站',
			emptyText : "请选择所属局站",
			editable : false,
			mode : 'local',
			triggerAction : 'all',
			width : 150,
			autoListWidth : true,
			autoListHeight : true,
			listWidth : 150,
			tpl : "<div id='tree'></div>",
			selectedClass : '',
			value : '',
			onSelect : Ext.emptyFn,
			onViewClick : function(doFocus) {
				var index = this.view.getSelectedIndexes()[0], 
				s = this.store, 
				r = s.getAt(index);
				if (r) {
					this.onSelect(r, index);
				} else if (s.getCount() === 0) {
					// this.collapse();
				}
				if (doFocus !== false) {
					// this.el.focus();
				}
			}
		});
		var opRoot = new Ext.tree.AsyncTreeNode({
			id : "rootNode",
			text : 'root',
			leaf : false
		});
		// 树容器
		var opTree = new Ext.tree.TreePanel({
			anchor : '95%',
			frame : false,
			width : 150,
			height : 200,
			animate : false,
			rootVisible : true,
			autoScroll : true,
			dataUrl : 'workorder!getDomianTree.action',
			root : opRoot,
			rootVisible : false
		});
		opTree.on('click', function(node, event) {
			comboxWithTree.setValue(node.attributes.text);
			Ext.getCmp("areano").setValue(node.attributes.domainCode);
			Ext.getCmp("areaname").setValue(node.attributes.text);
			comboxWithTree.collapse();
		});
		comboxWithTree.on('expand', function() {
			opTree.render('tree');
		});
		var areano = {
			name : 'polelineInfoBean.areano',
			id : 'areano',
			xtype : 'textfield',
			fieldLabel : '所属局站',
			hidden : true
		};
		var areaname = {
			name : 'polelineInfoBean.areaname',
			id : 'areaname',
			xtype : 'textfield',
			fieldLabel : '所属局站',
			hidden : true
		};
    	this.area = {
			layout : 'form', // 从左往右布局
			items : [
					areano,areaname, comboxWithTree
				]
		};
		var row9={
			layout:'column',
			items:[this.area]
		}
		this.items = [{
			xtype : 'fieldset',
			title : '杆路基本信息',
			collapsible : true,
			items : [poleLineId,row1,row2,row3,row4,row5,row6,row7,row8,row9
			/*poleLineId,poleLineName,poleLineCode,poleLineLevel,poleLineLength,startDeviceName,endDeviceName,
			purchasedMaintenanceTime,maintenanceModeEnumId,maintenanceOrgId,thirdPartyMaintenanceOrg,
			maintenanceTypeEnumId,projectName,projectCode,resourceLifePeriodEnumId*/]
		}];
		// 按钮
		this.buttons = [{
			text : "保存",
			icon : "imgs/save_btn.png",
			cls : "x-btn-text-icon",
			tooltip : '修改杆路信息',
			tooltipType : 'qtip',
			handler : this.btnSubmit.createDelegate(this)
		}, {
			xtype : 'tbspacer'
		}, {
			xtype : 'tbspacer'
		}, {
			xtype : 'tbspacer'
		}, {
			text : "返回",
			icon : "imgs/back.png",
			cls : "x-btn-text-icon",
			tooltip : '关闭此窗口',
			tooltipType : 'qtip',
			handler : this.btnClose.createDelegate(this)
		}];
		// 重写initComponent superclass.initComponent.call(this);来调用父类的初始化方法.
		com.increase.cen.poleline.ModifyPoleLine.superclass.initComponent.call(this);
	},
	// 提交事件
	btnSubmit : function submitForm() {
		if (!this.getForm().isValid()) {
			return;
		}
		var form=this.getForm();
		//验证电杆名称是否重复
		var oldPLName=Ext.getCmp("modifyPLName").getValue();
		var polelinename=Ext.getCmp("modifyPoleLineName").getValue();
		if(oldPLName==polelinename){
			form.doAction("submit", {
						url : 'poleline!doedit.action',
						submitEmptyText : false,
						method : 'post',
						success : function(form, action) {
							Ext.Msg.show({
								title : '提示',
								width : 300,
								msg : '<img src="imgs/tip_success.png" align="center" hspace="30"/>'
										+ '修改杆路信息成功!',
								buttons : {
									ok : "确定"
								}
							});
							// 窗口隐藏
							Ext.getCmp('modifyPoleLineWindow').close();
							Ext.getCmp("poleLineGrid").getStore().reload();
						},
						failure : function(form, action) {
							Ext.Msg.show({
								title : '提示',
								width : 300,
								msg : '<img src="imgs/tip_error.png" align="center" hspace="30"/>'
										+ '修改杆路信息失败!',
								buttons : {
									ok : "确定"
								}
							});
						}
					});
		}else{
		Ext.Ajax.request({
			url : 'poleline!checkpolelineJSON.action',
			method : 'post',
			params : {
				'poleLineName' : polelinename
			},
			success : function(response) {
				var json = Ext.util.JSON.decode(response.responseText);
				if (json.success == false) {
					Ext.Msg.show({
						title : '提示',
						width : 300,
						msg : '<img src="imgs/tip_error.png" align="center" hspace="30"/>'+'杆路名称重复请重新输入!',
						buttons : {
							ok : "确定"
						}
					});
				}else{
					form.doAction("submit", {
						url : 'poleline!doedit.action',
						submitEmptyText : false,
						method : 'post',
						success : function(form, action) {
							Ext.Msg.show({
								title : '提示',
								width : 300,
								msg : '<img src="imgs/tip_success.png" align="center" hspace="30"/>'
										+ '修改杆路信息成功!',
								buttons : {
									ok : "确定"
								}
							});
							// 窗口隐藏
							Ext.getCmp('modifyPoleLineWindow').close();
							Ext.getCmp("poleLineGrid").getStore().reload();
						},
						failure : function(form, action) {
							Ext.Msg.show({
								title : '提示',
								width : 300,
								msg : '<img src="imgs/tip_error.png" align="center" hspace="30"/>'
										+ '修改杆路信息失败!',
								buttons : {
									ok : "确定"
								}
							});
						}
					});
				}
			}
		});
		}
	},
	// 关闭
	btnClose : function() {
		// 重置表单
		Ext.getCmp("modifyPoleLine").getForm().reset();
		// 窗口隐藏
		Ext.getCmp('modifyPoleLineWindow').close();
		// 刷新表格
		Ext.getCmp("poleLineGrid").getStore().reload();
	}
});
Ext.reg("modifyPoleLine", com.increase.cen.poleline.ModifyPoleLine);