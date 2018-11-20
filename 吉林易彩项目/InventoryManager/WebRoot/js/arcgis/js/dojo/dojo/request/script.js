/*
	Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/request/script","module ./watch ./util ../_base/array ../_base/lang ../on ../dom ../dom-construct ../has ../_base/window".split(" "),function(n,p,h,q,r,s,t,u,l,v){function w(a,b){a.canDelete&&f._remove(a.id,b.options.frameDoc,!0)}function x(a){g&&g.length&&(q.forEach(g,function(a){f._remove(a.id,a.frameDoc);a.frameDoc=null}),g=[]);return a.options.jsonp?!a.data:!0}function y(a){return!!this.scriptLoaded}function z(a){return(a=a.options.checkString)&&eval("typeof("+a+') !\x3d\x3d "undefined"')}
function A(a,b){if(this.canDelete){var c=this.response.options;g.push({id:this.id,frameDoc:c.ioArgs?c.ioArgs.frameDoc:c.frameDoc});c.ioArgs&&(c.ioArgs.frameDoc=null);c.frameDoc=null}b?this.reject(b):this.resolve(a)}function f(a,b,c){var d=h.parseArgs(a,h.deepCopy({},b));a=d.url;b=d.options;var e=h.deferred(d,w,x,b.jsonp?null:b.checkString?z:y,A);r.mixin(e,{id:m+B++,canDelete:!1});b.jsonp&&(RegExp("[?\x26]"+b.jsonp+"\x3d").test(a)||(a+=(~a.indexOf("?")?"\x26":"?")+b.jsonp+"\x3d"+(b.frameDoc?"parent.":
"")+m+"_callbacks."+e.id),e.canDelete=!0,k[e.id]=function(a){d.data=a;e.handleResponse(d)});h.notify&&h.notify.emit("send",d,e.promise.cancel);if(!b.canAttach||b.canAttach(e)){var g=f._attach(e.id,a,b.frameDoc);if(!b.jsonp&&!b.checkString)var l=s(g,C,function(a){if("load"===a.type||D.test(g.readyState))l.remove(),e.scriptLoaded=a})}p(e);return c?e:e.promise}l.add("script-readystatechange",function(a,b){return"undefined"!==typeof b.createElement("script").onreadystatechange&&("undefined"===typeof a.opera||
"[object Opera]"!==a.opera.toString())});var m=n.id.replace(/[\/\.\-]/g,"_"),B=0,C=l("script-readystatechange")?"readystatechange":"load",D=/complete|loaded/,k=this[m+"_callbacks"]={},g=[];f.get=f;f._attach=function(a,b,c){c=c||v.doc;var d=c.createElement("script");d.type="text/javascript";d.src=b;d.id=a;d.async=!0;d.charset="utf-8";return c.getElementsByTagName("head")[0].appendChild(d)};f._remove=function(a,b,c){u.destroy(t.byId(a,b));k[a]&&(c?k[a]=function(){delete k[a]}:delete k[a])};f._callbacksProperty=
m+"_callbacks";return f});