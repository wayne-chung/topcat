(function() {
    'use strict';

    angular
        .module('angularApp')
        .factory('MetaDataManager', MetaDataManager);

    MetaDataManager.$inject = ['$translate'];

    function MetaDataManager($translate) {


        function MyException(message) {
            this.name = name;
            this.message = message;
        }
        MyException.prototype = new Error();
        MyException.prototype.constructor = MyException;


        var extractMetaData = function(tabDataArray, icatData) {

            var content = [];

            if (!Array.isArray(icatData)) {
                icatData = [icatData];
            }

            for (var l in icatData) {
                var icatDataCurrent = icatData[l];

                for (var counter in tabDataArray) {
                    var dataV = tabDataArray[counter];

                    if (typeof dataV.data !== 'undefined') {
                        var temp = content;
                        content = temp.concat(extractMetaData(dataV.data, icatDataCurrent[dataV.field]));
                    } else {
                        if (angular.isDefined(dataV.translateDisplayName)) {
                            content.push({
                                'title': $translate.instant(dataV.translateDisplayName),
                                'value': icatDataCurrent[dataV.field]
                            });
                        } else {
                            content.push({
                                'title': dataV.title,
                                'value': icatDataCurrent[dataV.field]
                            });
                        }
                    }
                }
            }
            return content;
        };

        return { // public API

            updateTabs: function(dataResults, tabs) {

                var tabsUpdated = [];

                for (var i in tabs) {
                    var icatData = dataResults;
                    var currentTab = tabs[i];
                    var tabTitle = '';
                    if (angular.isDefined(currentTab.translateDisplayName)) {
                        tabTitle = $translate.instant(currentTab.translateDisplayName);
                    } else {
                        tabTitle = currentTab.title;
                    }
                    var tabData = currentTab.data;
                    var tabContent = [];

                    if (currentTab.default === true) {
                        tabContent = extractMetaData(tabData, icatData);
                    } else {
                        tabContent = extractMetaData(tabData, icatData[0][currentTab.field]);
                    }
                    var temp = {
                        title: tabTitle,
                        content: tabContent
                    };
                    tabsUpdated.push(temp);
                }
                return tabsUpdated;
            },

            getTabQueryOptions: function(tabConfig) {

                var optionsList = {
                    'include': []
                };

                for (var index in tabConfig) {
                    var tab = tabConfig[index];

                    if (typeof tab.queryParams !== 'undefined') {
                        optionsList.include.push(tab.queryParams);
                    }
                }

                if (optionsList.include.length === 0) {
                    optionsList = {};
                }

                return optionsList;
            }
        };
    }
})();