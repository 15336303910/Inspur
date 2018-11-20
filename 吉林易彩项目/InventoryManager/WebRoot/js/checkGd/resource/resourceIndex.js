var checkLoader = new Ext.tree.TreeLoader({
	dataUrl:"hcgdManageAction!getChecks.action",
	requestMethod:"post"
});
var editWindow = null;
var loadingMask = new Ext.LoadMask(Ext.getBody(),{
	msg:"正在执行..."
});
function closeWindow(){
	if(editWindow!=null){
		editWindow.close();
		editWindow = null;
	}
}
var grid = null;
Ext.onReady(function(){
	var sm = new Ext.grid.CheckboxSelectionModel({
		checkOnly:true
	});
	var cm = new Ext.grid.ColumnModel([sm,{
		header:'id',
		dataIndex:'id',
		hidden:true
	},{
		header:'英文标识',
		dataIndex:"resourceEnName"
	},{
		header:'上级资源类型',
		dataIndex:'parentType',
		sortable:false,
		renderer:function(v){
			if(v=='0'){
				return "";
			}else{
				return v;
			}
		}
	},{
		header:'资源类型',
		dataIndex:'resourceCnName',
		sortable:true
	},{
		header:'资源表（工单）',
		dataIndex:'rmsTableName',
		sortable:false
	},{
		header:'编号字段（工单）',
		dataIndex:'codeColumn',
		sortable:false
	},{
		header:'标识字段（工单）',
		dataIndex:'valueColumn',
		sortable:false
	},{
		header:'资源表（综资）',
		dataIndex:'resTableName',
		sortable:false
	},{
		header:'编号字段（综资）',
		dataIndex:'resCodeColumn',
		sortable:false
	},{
		header:'标识字段（综资）',
		dataIndex:'resValueColumn',
		sortable:false
	}]);
	var store = new Ext.data.Store({
		proxy:new Ext.data.HttpProxy({
			url:"resourceConfigAction!getResourcesList.action"
		}),
		reader:new Ext.data.JsonReader({
			totalProperty:"total",
			root:"items"
		},Ext.data.Record.create([{
			name:'id',
			type:'integer',
			mapping:'id',
			hidden:true
		},{
			name:'resourceEnName',
			type:'string',
			mapping:'resourceEnName'
		},{
			name:'resourceCnName',
			type:'string',
			mapping:'resourceCnName'
		},{
			name:'rmsTableName',
			type:'string',
			mapping:'rmsTableName'
		},{
			name:'isUsing',
			type:'string',
			mapping:'isUsing'
		},{
			name:'codeColumn',
			type:'string',
			mapping:'codeColumn'
		},{
			name:'valueColumn',
			type:'string',
			mapping:'valueColumn'
		},{
			name:'regionColumn',
			type:'string',
			mapping:'regionColumn'
		},{
			name:'relateColumn',
			type:'string',
			mapping:'relateColumn'
		},{
			name:'parentType',
			type:'string',
			mapping:'parentType'
		},{
			name:'resTableName',
			type:'string',
			mapping:'resTableName'
		},{
			name:'resCodeColumn',
			type:'string',
			mapping:'resCodeColumn'
		},{
			name:'resValueColumn',
			type:'string',
			mapping:'resValueColumn'
		}])),
		remoteSort:true
	});
	var pageSize = parseInt(Ext.getDom("pageSize").value);
	var limit = parseInt(Ext.getDom("limit").value);	
	grid = new Ext.grid.GridPanel({
		title:"当前位置：资源配置",
		region:'center',
		id:"configDataGrid",
		border:false,
		cm:cm,
		sm:sm,
		store:store,
		autoScroll:true,
		viewConfig:{
			sortAscText:'升序',
			sortDescText:'降序',
			columnsText:'可选列',
			forceFit:true,
			scrollOffset:-1
		},
		buttonAlign:"center",
		tbar:[{
			id:"helpBtn",
			icon:"imgs/bangzhu.png",
			cls:"x-btn-text-icon",
			handler:function(){}
		},{
			xtype:'tbseparator'
		},{
			xtype:'tbseparator'
		},{
			text:'配置资源',
			id:'addItemsBtn',
			icon:"imgs/fiber/link_add.png",
			cls:"x-btn-text-icon"
		},{
			xtype:'tbseparator'
		},{
			xtype:'tbseparator'
		},{
			text:'删除资源',
			id:'deleteResBtn',
			icon:"imgs/fiber/link_add.png",
			cls:"x-btn-text-icon"
		},{
			xtype:'tbseparator'
		},{
			xtype:'tbseparator'
		}],
		bbar:new Ext.PagingToolbar({
			store:store,
			pageSize:pageSize,
			beforePageText:"当前第",
			afterPageText:"页，共{0}页",
			lastText:"尾页",
			nextText:"下一页",
			prevText:"上一页",
			firstText:"首页",
			refreshText:"刷新页面",
			displayInfo:true,
			displayMsg:"显示第{0}-{1}条 ，共{2}条",
			emptyMsg:"没有记录"
		})
	});
	var loadMarsk = new Ext.LoadMask(document.body,{
		msg:'数据加载中，请稍候......',
		disabled:false,
		store:store
	});
	store.addListener('beforeload',function(){
		loadMarsk.show();
	});
	store.load({
		params:{
			start:0,
			limit:limit
		}
	});
	new Ext.Viewport({
		layout:'border',
		items:[
		    grid
		]
	});
	/*删除资源*/
	var deleteResBtn = Ext.getCmp("deleteResBtn");
	deleteResBtn.on('click',function(){
		var selModel = grid.getSelectionModel();
		var records = selModel.getSelections();	
		if(records.length==0){
			Ext.Msg.show({
				title:'提示',
				width:300,
				msg:'<img src="imgs/tip_warn.png" align="center" hspace="30"/>请至少选择一项资源进行删除!',
				buttons:{
					ok:"确定"
				}
			});
		}else{
			Ext.MessageBox.show({
				title:'警告',
				msg:'删除资源配置时将会同时删除配置的核查字段.',
				width:300,
				multiline:false,
				closable:false,
				buttons:{
					yes:'确定',
					no:'取消'
				},
				icon:Ext.MessageBox.QUESTION,
				fn:function(btn){
					if(btn=="yes"){
						var deletedCodes = "";
						for(var j=0;j<records.length;j++){
							var checkedCode = records[0].get("id");
							if(deletedCodes==""){
								deletedCodes = checkedCode;
							}else{
								deletedCodes+=","+checkedCode;
							}
						}					
						Ext.Ajax.request({
							url:'resourceConfigAction!deleteObjects.action',
							method:'post',
							params:{
								deletedCodes:deletedCodes
							},
							success:function(response){
								var json = Ext.util.JSON.decode(response.responseText);
								if(json.success == true){
									Ext.Msg.show({
										title:'提示',
										width:300,
										msg:'<img src="imgs/tip_success.png" align="center" hspace="30"/>'+json.deleteMsg,
										buttons:{
											ok:"确定"
										}
									});
								}else{
									Ext.Msg.show({
										title:'提示',
										width:300,
										msg:'<img src="imgs/tip_error.png" align="center" hspace="30"/>删除失败!',
										buttons:{
											ok:"确定"
										}
									});
								}
								grid.getStore().remove(records);
								grid.getStore().load({
									params:{
										start:0,
										limit:limit
									}
								});
								grid.getView().refresh();
							},
							failure:function(){
								Ext.Msg.show({
									title:'提示',
									width:300,
									msg:'<img src="imgs/tip_error.png" align="center" hspace="30"/>删除失败!',
									buttons:{
										ok:"确定"
									}
								});
							}
						});
					}
				}
			});
		}
	});
	/*编辑资源*/
	var addItemsBtn = Ext.getCmp("addItemsBtn");
	addItemsBtn.on('click',function(){
		var editPanel = new Ext.form.FormPanel({
			id:"thisEditForm",
			bodyStyle:"padding:5px;",
			frame:true,
			bodyBorder:false,
			labelAlign:"right",
			buttonAlign:'center',
			labelWidth:120,
			width:800,
			height:350,
			initComponent:function(config){
				Ext.QuickTips.init();
				this.items = [{
					xtype:'fieldset',
					title:'资源详情 ',
					collapsible:true,
					layout:'column',
					items:[{
						columnWidth:1,
						layout:'column',
						items:[{
							height:30,
							layout:'form',
							items:[{
								id:'resObject.id',
								name:"resObject.id",
								xtype:'hidden'
							},{
								id:'resObject.resourceEnName',
								name:"resObject.resourceEnName",
								xtype:'textfield',
								fieldLabel:"资源标识",
								allowBlank:false,
								blankText:'',
								emptyText:'',
								width:360
							}]
						}]
					},{
						columnWidth:1,
						layout:'column',
						items:[{
							height:30,
							layout:'form',
							items:[{
								id:'resObject.resourceCnName',
								name:"resObject.resourceCnName",
								xtype:'textfield',
								fieldLabel:"资源类型",
								allowBlank:false,
								blankText:'',
								emptyText:'',
								width:360
							}]
						}]
					},{
						columnWidth:.5,
						layout:'column',
						items:[{
							height:30,
							layout:'form',
							items:[{
								id:'resObject.rmsTableName',
								name:"resObject.rmsTableName",
								xtype:'textfield',
								fieldLabel:"工单表",
								allowBlank:false,
								blankText:'',
								emptyText:'',
								width:140
							}]
						}]
					},{
						columnWidth:.5,
						layout:'column',
						items:[{
							height:30,
							layout:'form',
							labelWidth:80,
							items:[{
								id:'resObject.resTableName',
								name:"resObject.resTableName",
								xtype:'textfield',
								fieldLabel:"资源表",
								allowBlank:false,
								blankText:'',
								emptyText:'',
								width:130
							}]
						}]
					},{
						columnWidth:.5,
						layout:'column',
						items:[{
							height:30,
							layout:'form',
							items:[{
								id:'resObject.codeColumn',
								name:"resObject.codeColumn",
								xtype:'textfield',
								fieldLabel:"工单编号字段",
								allowBlank:false,
								blankText:'',
								emptyText:"",
								width:140
							}]
						}]
					},{
						columnWidth:.5,
						layout:'column',
						items:[{
							height:30,
							layout:'form',
							labelWidth:80,
							items:[{
								id:'resObject.resCodeColumn',
								name:"resObject.resCodeColumn",
								xtype:'textfield',
								fieldLabel:"资源编号字段",
								allowBlank:false,
								blankText:'',
								emptyText:"",
								width:130
							}]
						}]
					},{
						columnWidth:.5,
						layout:'column',
						items:[{
							height:30,
							layout:'form',
							items:[{
								id:'resObject.valueColumn',
								name:"resObject.valueColumn",
								xtype:'textfield',
								fieldLabel:"工单标识字段",
								allowBlank:false,
								blankText:'',
								emptyText:'',
								width:140
							}]
						}]
					},{
						columnWidth:.5,
						layout:'column',
						items:[{
							height:30,
							layout:'form',
							labelWidth:80,
							items:[{
								id:'resObject.resValueColumn',
								name:"resObject.resValueColumn",
								xtype:'textfield',
								fieldLabel:"资源标识字段",
								allowBlank:false,
								blankText:'',
								emptyText:'',
								width:130
							}]
						}]
					},{
						columnWidth:.5,
						layout:'column',
						items:[{
							height:30,
							layout:'form',
							items:[{
								id:'resObject.relateColumn',
								name:"resObject.relateColumn",
								xtype:'textfield',
								fieldLabel:"工单关联字段",
								allowBlank:true,
								blankText:'',
								emptyText:'',
								width:140
							}]
						}]
					},{
						columnWidth:.5,
						layout:'column',
						items:[{
							height:30,
							layout:'form',
							labelWidth:80,
							items:[{
								id:'resObject.resRelateColumn',
								name:"resObject.resRelateColumn",
								xtype:'textfield',
								fieldLabel:"资源关联字段",
								allowBlank:true,
								blankText:'',
								emptyText:'',
								width:130
							}]
						}]
					},{
						columnWidth:1,
						layout:'column',
						items:[{
							height:30,
							layout:'form',
							items:[{
								id:'resObject.regionColumn',
								name:"resObject.regionColumn",
								xtype:'textfield',
								fieldLabel:"工单区县字段",
								allowBlank:true,
								blankText:'',
								emptyText:'',
								width:360
							}]
						}]
					},{
						columnWidth:1,
						layout:'column',
						items:[{
							height:30,
							layout:'form',
							items:[{
								id:'resObject.isUsing',
								name:"resObject.isUsing",
								xtype:'combo',
								typeAhead:true,
					        	triggerAction:'all',
								fieldLabel:"是否在用",
								emptyText:"",
								blankText:"",
								forceSelection:true,
					         	mode:'local',
					        	width:360,
					         	store:new Ext.data.SimpleStore({
					        		fields:['value','text'],
					        		data:[
					        		   ['Y','在用'],
					        		   ['N','停用']
					        		]
					        	}),
					         	valueField:'value', 
					       		displayField:'text',
					       		hiddenName:'resObject.isUsing',
					       		editable:false
							}]
						}]
					},{
						columnWidth:1,
						layout:'column',
						items:[{
							height:30,
							layout:'form',
							items:[{
								id:"resObject.parentType",
								name:"resObject.parentType",
								xtype:'combo',
								typeAhead:true,
					        	triggerAction:'all',
								fieldLabel:"归属资源",
								emptyText:'',
								blankText:'',
								allowBlank:true,
								forceSelection:false,
					         	mode:'remote',
					         	store:new Ext.data.JsonStore({
					    			url:"hcgdManageAction!getResourceTypes.action",
					    			fields:["resourceCode","resourceName"]
					    		}),
					        	width:360,
					         	valueField:'resourceCode', 
					       		displayField:'resourceName',
					       		hiddenName:'resObject.parentType',
					       		editable:false
							}]
						}]
					}]
				}];
				this.buttons = [{
					id:'addBtnSubmit',
					text:"保存",
					hidden:false,
					icon:"imgs/save_btn.png",
					cls:"x-btn-text-icon",
					tooltip:'保存核查内容',
					tooltipType:'qtip',
					handler:this.addBtnSubmit.createDelegate(this)
				},{
					xtype:'tbspacer'
				},{
					text:"返回",
					icon:"imgs/back.png",
					cls:"x-btn-text-icon",
					tooltip:'关闭此窗口',
					tooltipType:'qtip',
					handler:this.btnClose.createDelegate(this)
				}];
				com.increase.cen.poleline.EditWindow.superclass.initComponent.call(this);
			},
			/*保存*/
			addBtnSubmit:function(){
				if(!Ext.getCmp("thisEditForm").getForm().isValid()){
					return;
				}
				/*1.验证资源是否已经存在*/
				Ext.Ajax.request({
					url:"resourceConfigAction!validResExist.action",
					method:'post',
					params:{
						resourceEnName:Ext.getCmp("resObject.resourceEnName").getValue(),
						resourceChName:Ext.getCmp("resObject.resourceCnName").getValue(),
						resourceTable:Ext.getCmp("resObject.rmsTableName").getValue()
					},
					success:function(resp,opts){
						var resultObject = Ext.decode(resp.responseText);
						if(resultObject["success"]){
							/*2.验证资源配置是否正确*/
							Ext.Ajax.request({
								url:"resourceConfigAction!validResExpress.action",
								method:'post',
								params:{
									resourceTable:Ext.getCmp("resObject.rmsTableName").getValue(),
									codeColumn:Ext.getCmp("resObject.codeColumn").getValue(),
									valueColumn:Ext.getCmp("resObject.valueColumn").getValue(),
									regionColumn:Ext.getCmp("resObject.regionColumn").getValue()
								},
								success:function(resp,opts){
									var resultObject = Ext.decode(resp.responseText);
									if(resultObject["success"]){
										/*3.将配置入库*/
										var form = Ext.getCmp("thisEditForm").getForm();
										form.doAction("submit",{
											url:'resourceConfigAction!editModel.action',
											method:'post',
											submitEmptyText:false,
											success:function(form,action){
												Ext.Msg.show({
													title:'提示',
													width:300,
													msg:'<img src="imgs/tip_success.png" align="center" hspace="30" />配置资源表成功!',
													buttons:{
														ok:"确定"
													}
												});
												Ext.getCmp("thisEditForm").getForm().reset();
												closeWindow();
												Ext.getCmp('configDataGrid').getStore().load({
													params:{
														start:0,
														limit:limit
													}
												});
												Ext.getCmp('configDataGrid').getView().refresh();
											},
											failure:function(form,action){
												Ext.Msg.show({
													title:'提示',
													width:300,
													msg:'<img src="imgs/tip_error.png" align="center" hspace="30"/>编辑核查信息失败',
													buttons:{
														ok:"确定"
													}
												});
											}
										});
									}else{
										Ext.MessageBox.alert("提示","资源表配置有误.");
									}
								},
								failure:function(resp,opts){}
							});
						}else{
							Ext.MessageBox.alert("提示","资源已存在.");
						}
					},
					failure:function(resp,opts){}
				});
			},
			/*返回按钮*/
			btnClose:function(){
				Ext.getCmp("thisEditForm").getForm().reset();
				closeWindow();
			}
		});
	    editWindow = new Ext.Window({
	    	width:600,
	    	height:390,
	    	title:"编辑资源",
	        layout:'fit',
	        items:[
	            editPanel
	        ]
	    });
	    editWindow.show();
	});
});