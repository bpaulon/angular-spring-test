var app = angular.module('app', ['autocomplete']);

// the service that retrieves some movie title from an url
app.factory('MovieRetriever', function($http, $q, $timeout){
  var MovieRetriever = new Object();

  MovieRetriever.getmovies = function(i) {
    var moviedata = $q.defer();
    //var movies;

    var names = [""]

	$http.get('http://localhost:8080/sandbox/complete/' + i)
			
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
      moviedata.resolve(names);
    },500);

    return moviedata.promise
  }

  return MovieRetriever;
});

app.controller('MyCtrl', function($scope, MovieRetriever){

  $scope.movies = MovieRetriever.getmovies("...");
  $scope.movies.then(function(data){
    $scope.movies = data;
  });

  $scope.getmovies = function(){
    return $scope.movies;
  }

  $scope.doSomething = function(typedthings){
    console.log("Do something like reload data with this: " + typedthings );
    $scope.newmovies = MovieRetriever.getmovies(typedthings);
    $scope.newmovies.then(function(data){
      $scope.movies = data;
    });
  }

  $scope.doSomethingElse = function(suggestion){
    console.log("Suggestion selected: " + suggestion );
  }

});
