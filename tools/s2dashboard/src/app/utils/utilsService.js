(function () {
  'use strict';

  angular.module('s2DashboardFramework')
    .service('utilsService', UtilsService);

  /** @ngInject */
  function UtilsService($http, $log, __env, $rootScope) {
      var vm = this;

      //force angular render in order to fast refresh view of component. $scope is pass as argument for render only this element
      vm.forceRender = function($scope){
        if(!$scope.$$phase) {
          $scope.$applyAsync();
        }
      }

      //Access json by string dot path
      function multiIndex(obj,is,pos) {  // obj,['1','2','3'] -> ((obj['1'])['2'])['3']
        if(is.length && !(is[0] in obj)){
          return obj[is[is.length-1]];
        }
        return is.length ? multiIndex(obj[is[0]],is.slice(1),pos) : obj
      }

      vm.getJsonValueByJsonPath = function(obj,is,pos) {
        //special case for array access, return key is 0, 1
        var matchArray = is.match(/\[[0-9]\]*$/);
        if(matchArray){
          //Get de match in is [0] and get return field name
          return obj[pos];
        }
        return multiIndex(obj,is.split('.'))
      }

      //array transform to sorted and unique values
      vm.sort_unique = function(arr) {
        if (arr.length === 0) return arr;
        var sortFn;
        if(typeof arr[0] === "string"){//String sort
          sortFn = function (a, b) {
            if(a < b) return -1;
            if(a > b) return 1;
            return 0;
          }
        }
        else{//Number and date sort
          sortFn = function (a, b) {
            return a*1 - b*1;
          }
        }
        arr = arr.sort(sortFn);
        var ret = [arr[0]];
        for (var i = 1; i < arr.length; i++) { //Start loop at 1: arr[0] can never be a duplicate
          if (arr[i-1] !== arr[i]) {
            ret.push(arr[i]);
          }
        }
        return ret;
      }
  };
})();
