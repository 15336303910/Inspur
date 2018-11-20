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
	{header : '基站中文名称'},
	{header : '位置信息'},
	{header : '类型'},
	{header : '是否分布式基站'},
	{header : '是否一体化基站'},
	{header : '覆盖场景'},
	{header : '工作频率'},
	{header : '配置载频数'},
	{header : '开通载频数'},
	{header : 'TCH配置数'},
	{header : 'SDCCH配置数'},
	{header : '静态PDCH配置数'},
	{header : '归属BSC'},
	{header : '基站分级'},
	{header : '扇区数'},
	{header : '传输方式'},
	{header : '传输带宽'},
	{header : '额定功率'},
	{header : '是否环境监控'},
	{header : '是否有后备电源'},
	{header : '传输是否成环'},
	{header : '是否工程期站'},
	{header : '归属维护区域'},
	{header : '设备网管标识'},
	{header : '资源生命周期'},
	{header : '生命周期时间'},
	{header : '入网时间'},
	{header : '硬件版本'},
	{header : '软件版本'},
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
	{header : '设计用途'},
	{header : '备注'}
	]);

	var store = new Ext.data.Store({});// 提供数据输入
	
	var grid = new Ext.grid.GridPanel({
		title : "当前位置：移动无线网",
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