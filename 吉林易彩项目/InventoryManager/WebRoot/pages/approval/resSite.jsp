<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>标石管理</title>
    <script type="text/javascript">
    	var id =  '<%=request.getParameter("id") %>';
		var type = '<%=request.getParameter("resType") %>';
		var grapType = '<%=request.getParameter("grapType") %>';
	</script>
	<link rel="stylesheet" type="text/css" href="js/ext/resources/css/ext-all.css">
	<link rel="stylesheet" type="text/css" href="css/userList.css" />
	<link rel="stylesheet" type="text/css" title="gray" href="css/xtheme-gray.css" />
	<link rel="stylesheet" type="text/css" href="js/ext/resources/css/multiselect.css" />
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/base.css" />
	<script type="text/javascript" src="js/ext/adapter/ext/ext-base.js"></script>
	<script type="text/javascript" src="js/ext/ext-all.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery/jquery.min.js" charset="utf-8"></script>
	<script type="text/javascript" charset="UTF-8" src="${pageContext.request.contextPath}/js/ext/FileUploadField.js"></script>
	<script type="text/javascript" charset="UTF-8" src="${pageContext.request.contextPath}/js/approval/resSite.js"></script>
	<script type="text/javascript" charset="UTF-8" src="${pageContext.request.contextPath}/js/common/common.js"></script>
	<script type="text/javascript">
		var context_path = '${pageContext.request.contextPath}';
		var siteName= "";
	</script>
  </head>
  
  <body>
  		<div id='leftDiv'></div>
		<div id='rightDiv'></div>
  </body>
</html>
