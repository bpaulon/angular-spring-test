
//angular.module(['ui.bootstrap'])

angular.module('postTest', ['autocomplete']);
angular.module('postTest').controller('myController', ['$scope', '$http',function($scope, $http) {
	$scope.inputValue = {
		secondName : "",
		firstName : "",
		id : ""
	};

	$scope.sendUserData = function() {

		$http.post('http://localhost:8080/sandbox/user',
				JSON.stringify($scope.inputValue))
				.success(function(data, status, headers, config) {
					// this callback will be called asynchronously
					// when the response is available
					$scope.inputValue = data;
					$scope.message = 'Hello ' + data.firstName
							+ '! Your user ID is: ' + data.id;

					console.log(data);
				}).error(function(data, status, headers, config) {
					// called asynchronously if an error occurs
					// or server returns response with an error status.
				});

	}
}]);

//var autocompleteApp = angular.module('autoComplete', );
angular.module('postTest').controller('TypeaheadCtrl', ['$scope', '$http',function ($scope, $http, limitToFilter) {

	  //http://www.geobytes.com/free-ajax-cities-jsonp-api.htm

	  $scope.cities = function(cityName) {
	    return $http.jsonp("http://gd.geobytes.com/AutoCompleteCity?callback=JSON_CALLBACK &filter=US&q="+cityName).then(function(response){
	      return limitToFilter(response.data, 15);
	    });
	  };
	  
	}]);
