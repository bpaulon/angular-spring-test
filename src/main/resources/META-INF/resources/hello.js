
var clickButtonApp = angular.module('demo2', []);
clickButtonApp.controller('myCtrl', function($scope, $http) {
	$scope.inputValue = {
		secondName : "",
		firstName : "",
		id : ""
	};

	$scope.sendUserData = function() {

		$http.post('http://localhost:8080/sandbox/postit',
				JSON.stringify($scope.inputValue)).success(
				function(data, status, headers, config) {
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
});
