Ext.onReady(function() {
	// 获取每页显示的记录数
	var pageSize = parseInt(Ext.getDom("pageSize").value);
	var limit = parseInt(Ext.getDom("limit").value);
	
	// 列模型
	var cm = new Ext.grid.ColumnModel([
	{header : "序号",width : 50,hideable : false,
		renderer : function(value, metadata, record, rowIndex, colIndex, store) {
			var start = store.lastOptions.params.start;
			if (isNaN(start)) {
				start = 0;
			}
			return start + rowIndex + 1;
		}
	}, 
	{header : '设备名称'}, 
	{header : '设备别名'},
	{header : '资产标签号'},
	{header : '交换网节点类型'},
	{header : '设备类型'},
	{header : '所在机房标识'},
	{header : '所属网络层次'},
	{header : '是否重保'},
	{header : '设计容量'},
	{header : '实装容量'},
	{header : '预警容量'},
	{header : '155M端口最大数量'},
	{header : '155M端口配置数量'},
	{header : '155M端口省际方向占用数量'},
	{header : '155M端口地市方向占用数量'},
	{header : '155M端口本地方向占用数量'},
	{header : '2M端口最大数量'},
	{header : '2M端口配置数量'},
	{header : '2M端口省际方向占用数量'},
	{header : '2M端口地市方向占用数量'},
	{header : '2M端口本地方向占用数量'},
	{header : '155M端口国际侧中继端口数'},
	{header : '155M端口国内侧中继端口数'},
	{header : '155M端口中国电信侧中继端口数'},
	{header : '155M端口中国移动侧中继端口数'},
	{header : '用户中继端口数'},
	{header : '交换机2M信令链路最大数量'},
	{header : '交换机2M信令链路配置数量'},
	{header : '交换机64K信令链路最大数量'},
	{header : '交换机64K信令链路配置数量'},
	{header : '覆盖范围'},
	{header : '硬件版本'},
	{header : '软件版本'},
	{header : '软件补丁版本'},
	{header : '设备网管标识'},
	{header : '维护状态'},
	{header : '割接标记'},
	{header : '资源生命周期'},
	{header : '生命周期时间'},
	{header : '设备厂家'},
	{header : '小生产厂商'},
	{header : '设备序列号'},
	{header : '设备规格型号'},
	{header : '入网时间'},
	{header : '信令点编码'},
	{header : '维护方式'},
	{header : '维护单位'},
	{header : '包机人'},
	{header : '第三方维护单位'},
	{header : '续保截止日期'},
	{header : '维保类型'},
	
	{header : '已购买维保累计年限'},
	{header : '工程项目编号'},
	{header : '工程项目名称'},
	{header : '工程保修截止日期'},
	{header : '是否授权管理'},
	{header : '授权管理单位'},
	{header : '出接口局'},
	{header : '入接口局'},
	{header : '网管IP地址'},
	{header : '计费采集IP地址'},
	{header : '所属母局'},
	{header : '原资源系统设备编号'},
	{header : '原资源系统设备名称'},
	
	{header : '备注'}
	]);

	var store = new Ext.data.Store({});// 提供数据输入
	
	var grid = new Ext.grid.GridPanel({
		title : "当前位置：固网交换网络资源",
		region : 'center',
		id : "stationGrid",
		border : false,
		cm : cm,// 列定义的model(必需)
		store : store,
		autoScroll : true,// 滚动条
		viewConfig : {
			sortAscText : '升序',
			sortDescText : '降序',
			columnsText : '可选列',
			forceFit : false,// Ture表示自动扩展或缩小列的宽度
			scrollOffset : -1
		},
		buttonAlign : "center",// 按钮居中
		tbar : [{
			id:"helpBtn",
			icon:"imgs/bangzhu.png",
			cls:"x-btn-text-icon",
			handler : function() {
//				help("pages/help/station.jsp");
			}
		},
		{
			xtype : 'tbseparator'
		},{
			id : 'refreshBtn',
			text : '全部刷新',
			icon : "imgs/all_refresh.png",
			cls : "x-btn-text-icon"
		}],
		bbar : new Ext.PagingToolbar({// 分页工具栏
			store : store,
			pageSize : pageSize,
			beforePageText : "当前第",
			afterPageText : "页，共{0}页",
			lastText : "尾页",
			nextText : "下一页",
			prevText : "上一页",
			firstText : "首页",
			refreshText : "刷新页面",
			displayInfo : true,// 是否显示displayMsg
			displayMsg : "显示第{0}-{1}条 ，共{2}条",
			emptyMsg : "没有记录"
		})
	});
	


	// viewport 布局
	new Ext.Viewport({
		layout : 'border',
		items : [grid]
	});
});