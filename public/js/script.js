var myPerfectWeatherApp = angular.module('myPerfectWeatherApp', ['ngRoute']);

// configure routes
myPerfectWeatherApp.config(['$routeProvider', function ($routeProvider) {
    $routeProvider.when('/', {
            templateUrl: 'assets/partials/home.html',
            controller: 'homeController'
        })
        .otherwise({
            redirectTo: '/error'
        });

}]);

myPerfectWeatherApp.isDefined = function(val){
    return !(angular.isUndefined(val) || val === null || val.trim === "")
}

myPerfectWeatherApp.controller('homeController', ['$scope', '$window', '$location', '$anchorScroll', '$http',
    function ($scope, $window, $location, $anchorScroll, $http) {

    var tempSlider = new Slider("#temp-slider", {
        tooltip: 'always', tooltip_position: 'top', id: "temp-slider", min: -40, max: 40, range: true, value: [-10, 30] });

    var popSlider = new Slider("#pop-slider", {
        tooltip: 'always', tooltip_position: 'top', id: "pop-slider", min: 0, max: 100, step: 10, range: true, value: [0, 100] });

    var windSlider = new Slider("#wind-slider", {
        tooltip: 'always', tooltip_position: 'top', id: "wind-slider", min: 0, max: 150, step: 1, range: true, value: [0, 80] });

    $scope.weatherForm = {};

    $scope.weatherForm.precipType = 'rain';

    $scope.weatherForm.submit = function (item, event) {

        $scope.weatherForm.minTemp = tempSlider.getValue()[0];
        $scope.weatherForm.maxTemp = tempSlider.getValue()[1];

        $scope.weatherForm.minPop = popSlider.getValue()[0];
        $scope.weatherForm.maxPop = popSlider.getValue()[1];

        $scope.weatherForm.minWspd = windSlider.getValue()[0];
        $scope.weatherForm.maxWspd = windSlider.getValue()[1];

        var responsePromise = $http.post("api/search", $scope.weatherForm)
        responsePromise.success(function (data, status, headers) {

            $scope.loaded = true;
            $scope.cities = data;

            var x = []
            angular.forEach($scope.cities.groups, function(item){
                var z = []
                angular.forEach(item.rows, function(item2){
                    z.push(item2.fields)

                });
                x.push(z)
            });

            $scope.y = x;

            //// Location of the hash we will scroll to.
            //$location.hash('results');
            //// Actually performing the scroll.
            //$anchorScroll();

        });
        responsePromise.error(function (data, status, headers) {

            alert("Submitting form failed! " + data + "; status: " + status);
        });
    };

    $scope.checkForFlights = function(fields){
        var origin = "";
        var dest = fields[0].airport;
        var out = "";
        //if out is in the past use tomorrow
        var back = "";
        for (i = 0; i < fields.length; i++) {
            if(fields[i].num == 2) out = fields[i].fcst_valid_local;
            if(fields[i].num == 11) back = fields[i].fcst_valid_local;
        }
        var uriMobi = "http://m.momondo.co.uk/?cur=EUR&o=" + origin + "&d=" + dest + "&dd=" + out + "&rd=" + back + "&a=1&ti=ECO&stops=any";
        $window.open(uriMobi, '_blank');
    };


}]);