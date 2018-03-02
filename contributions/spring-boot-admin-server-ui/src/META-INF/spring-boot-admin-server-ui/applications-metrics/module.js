webpackJsonp([7],{0:function(t,e,a){(function(t){"use strict";var e=a(1),i=e.module("sba-applications-metrics",["sba-applications"]);t.sbaModules.push(i.name),i.controller("metricsCtrl",a(114)),i.directive("sbaSimpleMetricBar",a(116)),i.directive("sbaRichMetricBar",a(115)),i.config(["$stateProvider",function(t){t.state("applications.metrics",{url:"/metrics",templateUrl:"applications-metrics/views/metrics.html",controller:"metricsCtrl"})}]),i.run(["ApplicationViews","$http","$sce",function(t,e,a){t.register({order:5,title:a.trustAsHtml('<i class="fa fa-bar-chart fa-fw"></i>Metrics'),state:"applications.metrics",show:function(t){return e.head("api/applications/"+t.id+"/metrics").then(function(){return!0}).catch(function(){return!1})}})}])}).call(e,function(){return this}())},114:function(t,e){"use strict";t.exports=["$scope","application",function(t,e){"ngInject";function a(t,e){for(var a=0;a<t.length;a++)if(t[a].name===e.name){for(var i in e)e.hasOwnProperty(i)&&(t[a][i]=e[i]);return}t.push(e)}t.showRichGauges=!1,t.metrics=[],e.getMetrics().then(function(e){function i(t,e,i){var c=n[t]||{max:0,values:[]};a(c.values,e),"undefined"!=typeof i&&i>c.max&&(c.max=i),n[t]=c}var n={},c=[{regex:/(gauge\..+)\.val/,callback:function(e,a,n){i("gauge",{name:a[1],value:n},n),t.showRichGauges=!0}},{regex:/(gauge\..+)\.avg/,callback:function(t,e,a){i("gauge",{name:e[1],avg:a.toFixed(2)})}},{regex:/(gauge\..+)\.min/,callback:function(t,e,a){i("gauge",{name:e[1],min:a})}},{regex:/(gauge\..+)\.max/,callback:function(t,e,a){i("gauge",{name:e[1],max:a},a)}},{regex:/(gauge\..+)\.count/,callback:function(t,e,a){i("gauge",{name:e[1],count:a})}},{regex:/(gauge\..+)\.alpha/,callback:function(){}},{regex:/(gauge\..+)/,callback:function(t,e,a){i("gauge",{name:e[1],value:a},a)}},{regex:/^([^.]+).*/,callback:function(t,e,a){i(e[1],{name:t,value:a},a)}}],r=e.data;for(var l in r)if(r.hasOwnProperty(l))for(var s=0;s<c.length;s++){var o=c[s].regex.exec(l);if(null!==o){c[s].callback(l,o,r[l]);break}}for(var u in n)n.hasOwnProperty(u)&&t.metrics.push({name:u,values:n[u].values,max:n[u].max||0})}).catch(function(e){t.error=e.data})}]},115:function(t,e,a){"use strict";t.exports=function(){return{restrict:"E",scope:{metric:"=forMetric",globalMax:"=?globalMax"},link:function(t){t.globalMax=t.globalMax||t.metric.max,t.minWidth=(t.metric.min/t.globalMax*100).toFixed(2),t.avgWidth=(t.metric.avg/t.globalMax*100).toFixed(2),t.valueWidth=(t.metric.value/t.globalMax*100).toFixed(2),t.maxWidth=(t.metric.max/t.globalMax*100).toFixed(2)},template:a(157)}}},116:function(t,e,a){"use strict";t.exports=function(){return{restrict:"E",scope:{metric:"=forMetric",globalMax:"=globalMax"},link:function(t){t.valueWidth=(t.metric.value/t.globalMax*100).toFixed(2)},template:a(158)}}},157:function(t,e){t.exports='<div>\n\t{{metric.name}} (count: {{metric.count}})\n\t<div ng-if="metric.count > 1" class="progress with-marks">\n\t\t<div class="mark-current" ng-style="{left: valueWidth + \'%\'}"></div>\n\t\t<div class="bar-offset" ng-style="{width: minWidth + \'%\'}"></div>\n\t\t<div class="bar bar-success" ng-style="{width: (avgWidth - minWidth) + \'%\'}"></div>\n\t\t<div class="bar bar-success" ng-style="{width: (maxWidth - avgWidth) + \'%\'}"></div>\n\t\t<div tooltip="value {{metric.value}}" class="value-mark" ng-style="{left: valueWidth + \'%\'}">{{metric.value}}</div>\n\t</div>\n\t<div ng-if="metric.count > 1" class="bar-scale">\n\t\t<div tooltip="min {{metric.min}}" class="pitch-line" ng-style="{left: minWidth + \'%\'}"><small class="muted">{{metric.min}}</small></div>\n\t\t<div tooltip="max {{metric.max}}" class="pitch-line" ng-style="{left: maxWidth + \'%\'}"><small class="muted">{{metric.max}}</small></div>\n\t\t<div tooltip="average {{metric.avg}}" class="pitch-line" ng-style="{left: avgWidth + \'%\'}">{{metric.avg}}</div>\n\t</div>\n\t<div ng-if="metric.count <= 1" class="progress" style="margin-bottom: 0px;">\n\t\t<div class="bar bar-success" ng-style="{width: valueWidth + \'%\'}" style="text-align:right; padding-right: 5px;">{{metric.value}}</div>\n\t</div>\n</div>'},158:function(t,e){t.exports='<div>\n\t{{metric.name}}\n\t<div class="progress" style="margin-bottom: 0px;">\n\t\t<div class="bar bar-success" ng-style="{width: valueWidth + \'%\'}" style="text-align:right; padding-right: 5px;">{{metric.value}}</div>\n\t</div>\n</div>'}});