var app = angular.module('app', ['autocomplete']);

// the service that retrieves some movie title from an url
app.factory('NameRetriever', function($http, $q, $timeout){
  var NameRetriever = new Object();

  NameRetriever.getnames = function(i) {
    var namedata = $q.defer();
    var names = [""]

	$http.get('/complete/' + i)
			
			.success(function(data, status, headers, config) {
				// this callback will be called asynchronously
				// when the response is available
				names = data;
				console.log(data);
			}).error(function(data, status, headers, config) {
				// called asynchronously if an error occurs
				// or server returns response with an error status.
			});


    $timeout(function(){
      namedata.resolve(names);
    },500);

    return namedata.promise
  }

  return NameRetriever;
});

app.controller('MyCtrl', function($scope, NameRetriever){

  $scope.movies = NameRetriever.getnames("...");
  $scope.movies.then(function(data){
    $scope.movies = data;
  });

  $scope.getnames = function(){
    return $scope.movies;
  }

  $scope.doSomething = function(typedthings){
    console.log("Do something like reload data with this: " + typedthings );
    $scope.newmovies = NameRetriever.getnames(typedthings);
    $scope.newmovies.then(function(data){
      $scope.movies = data;
    });
  }

  $scope.doSomethingElse = function(suggestion){
    console.log("Suggestion selected: " + suggestion );
  }

});
