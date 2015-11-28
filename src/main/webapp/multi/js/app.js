// 
// Defining Module
//
var spApp = angular.module("ATVPmultiApp", [ 'ATVPmultiApp.services', 'ui.bootstrap', 'ngRoute', 'ngResource', 'ngStorage' ]);
spApp.constant('atvpServer', 'http://1-dot-tenis-virtual-players.appspot.com').constant('atvpServerSecure', 'https://1-dot-tenis-virtual-players.appspot.com');

spApp.run(function($rootScope) {

	$rootScope.$on('loading:show', function() {

	});

	$rootScope.$on('loading:hide', function() {});
})
//
// Defining Routes
//
spApp.config(function($routeProvider, $httpProvider) {
	$httpProvider.defaults.transformRequest = function(obj) {
		var str = [];
		for ( var p in obj)
			str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
		return str.join("&");
	};
	$httpProvider.interceptors.push(function($rootScope, $q) {
		return {
			responseError : function(rejection) {
				$rootScope.$broadcast('loading:hide')

				return $q.reject(rejection);
			},
			requestError : function(rejection) {
				$rootScope.$broadcast('loading:hide')
				return $q.reject(rejection);
			},
			request : function(config) {
				$rootScope.$broadcast('loading:show')
				return config
			},
			response : function(response) {
				$rootScope.$broadcast('loading:hide')
				return response
			}
		}
	});
	$httpProvider.defaults.useXDomain = true;

	delete $httpProvider.defaults.headers.common['X-Requested-With'];

	$routeProvider.when('/login', {
		controller : 'LoginCtrl',
		templateUrl : 'templates/login.html'
	}).when('/matcheslogin', {
		controller : 'LoginCtrl',
		templateUrl : 'templates/matches.html'
	}).when('/profile', {
		controller : 'AppCtrl',
		templateUrl : 'templates/profile.html'
	}).when('/friends', {
		controller : 'FriendsCtrl',
		templateUrl : 'templates/friends.html'
	}).when('/matches', {
		controller : 'MatchesCtrl',
		templateUrl : 'templates/matches.html'
	}).when('/match/:mId', {
		controller : 'MatchCtrl',
		templateUrl : 'templates/match.html'
	})

	.when('/dash', {
		controller : 'AppCtrl',
		templateUrl : 'templates/dash.html'
	}).otherwise({
		controller : 'AppCtrl',
		templateUrl : 'templates/search.html'
	});

	openFB.init({
		appId : '979422375424132'
	});

});

spApp.controller('MatchesCtrl', [ '$scope', '$route', '$location', '$window', 'Match', 'loginService', function($scope, $route, $location, $window, Match, loginService) {

	$scope.weekCount = 0;
	$scope.view = function(m) {
		console.log("asdfasdf ", m)
		$window.location.href = '#match/' + m.id;
	}
	$scope.doRefresh = function() {
		$scope.matches = Match.query({
			idPlayer : loginService.getUser() != null ? loginService.getUser().id : null
		});
	}
	$scope.movePrevious = function() {
		$scope.weekCount++;
	};
	$scope.moveNext = function() {
		$scope.weekCount--;
	};
	$scope.showNewMatch = function() {

		return $location.path() == '/matches';
	}
	$scope.doRefresh();
} ]);

spApp.controller('MatchCtrl',  [ '$scope', '$route', '$location', '$window', 'Match', 'loginService', function($scope, $route, $location, $window, Match, loginService) {
	var changed = -1;
	
	$scope.error_update_set="";
// $scope.confirmSetRemove=function(n){
// var confirmPopup = $ionicPopup.confirm({
// title : 'Apagar Set',
// template : 'Deseja apagar este Set?'
// });
// confirmPopup.then(function(res) {
// if (res) {
// var success=function(resp){
// $scope.refreshMatch();
// $scope.updateSet.hide();
// };
// var error=function(resp){
// console.log(resp);
// $scope.error_update_set="Falhou ao tentar remover o set. Tente novamente.";
// };
// Match.removeSet($scope.match.sets[n],success,error);
// } else {
// }
// });
// }
//	
//	
// $ionicModal.fromTemplateUrl('templates/update_set.html', {
// scope : $scope
// }).then(function(modal) {
// $scope.updateSet = modal;
// });
// $scope.removeSet=function(){
// $scope.confirmSetRemove($scope.editingSet.number);
// };
// $scope.finishSetUpdates = function() {
// var success=function(resp){
// console.log(resp)
// $scope.match.sets[$scope.editingSet.number]=(resp);
// changed=-1;
// };
// var error=function(resp){
// console.log(resp)
//			
// };
// Match.updateSet($scope.editingSet,success,error);
// $scope.updateSet.hide();
// };
//	
	$scope.showSetUpdate=function(s){
		$scope.editingSet=s;
		$scope.updateSet.show();
	}
	
	$scope.wasChanged=function(n){
			return (n==changed);
	}
	$scope.persistSet=function(n){
		var success=function(resp){
			console.log(resp)
			$scope.match.sets[n]=(resp);
			changed=-1;
		};
		var error=function(resp){
			console.log(resp)
			
		};
		Match.updateSet($scope.match.sets[n],success,error);
	}
	$scope.setChanged=function(n){
		changed=n;
	}
	$scope.addSet = function(){
		Match.addSet({
			number:$scope.match.sets.length,
			playerOneGames:0,
			playerTwoGames:0,
			key: $route.current.params.mId
		},function(resp){
			console.log(resp)
			$scope.match.sets.push(resp);
			$scope.showSetUpdate(resp);
		});
	};
	

	$scope.deleteMatch = function() {
		var confirmPopup = $ionicPopup.confirm({
			title : 'Cancelar partida',
			template : 'Deseja realmente cancelar esta partida?'
		});
		confirmPopup.then(function(res) {
			if (res) {
				Match.delete({mId : $route.current.params.mId});
			} else {
			}
		});
		
		
	}
	
	
	$scope.showDeleteMatch = function() {
		
		if($location.path() == '/match'){
			if (loginService.getUser() != null && $scope.match!=null) {
				if($scope.match.ranking==null){
					if($scope.match.idPlayerOne==loginService.getUser().id || $scope.match.idPlayerTwo==loginService.getUser().id ){
						return true;
					}
				}else{
					if($scope.match.ranking.idManager==loginService.getUser().id ){
						return true;
					}
				}
			}
		}
		return false;
	};
	
	$scope.refreshMatch = function(){
		if (loginService.getUser() != null) {
			if ($route.current.params.mId) {
				$scope.match = Match.get({
					mId : $route.current.params.mId,
					idPlayer : loginService.getUser().id
				}
			);
				
			}
		}
	}
	console.log($route.current.params);
	$scope.$on('$routeChangeSuccess', function(){
		var path = $location.path();
		console.log(path);
		$scope.showDeleteMatch();
		

	});
	$scope.refreshMatch();
	
}]);
spApp.controller('FriendsCtrl', [ '$scope', '$route','$http', 'Friendship', 'loginService', '$timeout', '$uibModal', 'atvpServer', function($scope, $route,$http, Friendship, loginService, $timeout, $uibModal, atvpServer) {
	$scope.showFriendInvitation = function() {
		if (!$route.current || !$route.current.name) {
			return false;
		}
		if ($route.current.name == ('app.friends')) {
			return true;
		}
	};
	$scope.toInvite = {};
	$scope.inviteFriend = function() {
		var myPopup = $uibModal.open({
			template : '<input  placeholder="Nome" type="text" ng-model="toInvite.name">' + '<br><input  placeholder="E-mail" type="email" ng-model="toInvite.invited">',
			title : 'Insira o nome e email de seu adversário',
			subTitle : 'Enviaremos um e-mail convidando ele para se juntar ao ATVP',
			scope : $scope,
			buttons : [ {
				text : 'Cancelar',
				onTap : function(e) {

				}
			}, {
				text : '<b>OK</b>',
				type : 'button-positive',
				onTap : function(e) {
					if (!$scope.toInvite.invited) {
						e.preventDefault();
					} else {
						var req = {
							method : 'POST',
							url : atvpServer + '/rest/friendship?action=invite',
							params : {
								invited : $scope.toInvite.invited,
								name : $scope.toInvite.name,
								id : loginService.getUser().id
							},
							cache : false

						}

						$http(req)

						.success(function(data, status, headers, config) {
							console.log(data);

						}).error(function(data, status, headers, config) {
							// called asynchronously if an error occurs
							// or server returns response with an error
							// status.
							console.log("erro");
							var alertPopup = $ionicPopup.alert({
								title : 'Erro convidando adversário',
								template : 'Por favor tente novamente.'
							});

						});
					}
				}
			} ]
		});
		myPopup.then(function(res) {
			console.log('Tapped!', res);
		});
	};
	// $scope.showAcceptDeny = function(invitation) {
	//
	// // Show the action sheet
	// var hideSheet = $ionicActionSheet.show({
	// buttons : [ {
	// text : '<b>Aceitar</b>'
	// }
	//
	// ],
	// destructiveText : 'Recusar',
	// titleText : 'Convite de ' + invitation.inviter.name,
	// cancelText : 'Cancelar',
	// cancel : function() {
	// // add cancel code..
	// },
	// buttonClicked : function(index) {
	// Friendship.acceptInvitation({
	// id : invitation.id,
	// idPlayer : loginService.getUser() != null ? loginService.getUser().id :
	// null
	// });
	// $scope.doRefresh();
	// return true;
	// },
	// destructiveButtonClicked : function(index) {
	// Friendship.declineInvitation({
	// id : invitation.id,
	// idPlayer : loginService.getUser() != null ? loginService.getUser().id :
	// null
	// });
	// $scope.doRefresh();
	// return true;
	// }
	// });
	//
	// // For example's sake, hide the sheet after two seconds
	// $timeout(function() {
	// hideSheet();
	// }, 10000);
	//
	// };
	$scope.doRefresh = function() {
		$scope.friends = Friendship.query({
			id : loginService.getUser() != null ? loginService.getUser().id : null
		});
		$scope.invitations = Friendship.queryInvitations({
			email : loginService.getUser() != null ? loginService.getUser().email : null
		});

		$scope.inviteds = Friendship.queryInviteds({
			id : loginService.getUser() != null ? loginService.getUser().id : null
		});

	}
	$scope.doRefresh();
} ]);
spApp.controller('ProfileCtrl', [ '$scope', '$log', '$http', '$window', 'loginService', 'atvpServerSecure', function($scope, $log, $http, $window, loginService, statsService, atvpServerSecure) {

	var editing = false;
	$scope.isEditing = function() {
		return editing;
	}
	$scope.showEdit = function() {
		return $route.current.name == 'profile';
	};
	$scope.edit = function() {
		editing = true;
	};
	$scope.save = function() {
		editing = false;
	};
	$scope.notFBed = function() {
		return true;// $scope.playerData.fbId=='';
	}
	$scope.fbLogin = function() {
		openFB.login(function(response) {
			if (response.status === 'connected') {

				openFB.api({
					path : '/me',
					params : {
						fields : 'id,name,birthday,email,gender'
					},
					success : function(user) {
						$scope.$apply(function() {
							console.log("fbUser", user);
							var imageProfile = "http://graph.facebook.com/" + user.id + "/picture?width=270&height=270";
							$scope.playerData.imageProfile = imageProfile;
							userService.updateUser(loginService.getUser().id, imageProfile, user.id, function(arg1, arg2) {
								console.log(arg1, arg2)
							}, function(arg1, arg2) {
								console.log(arg1, arg2)
							});

						});

					},
					error : function(error) {
						console.log("Login com Facebook falhou");

					}
				});

			} else {
				console.log("falhou");
			}
		}, {
			scope : 'email'
		});
	}

} ]);
spApp.controller('LoginCtrl', function($scope, $log, $http, $window, loginService, statsService, atvpServerSecure) {
	$scope.loginData = {
		username : '',
		password : ''
	};
	// Perform the login action when the user submits the login form
	$scope.doLogin = function() {
		$scope.login_error = '';
		$scope.login_success = '';

		if ($scope.loginData.username != '' && $scope.loginData.password != '') {
			var headers = {

			};
			var req = {
				method : 'POST',
				url : atvpServerSecure + '/rest/login',
				headers : headers,
				params : {
					email : $scope.loginData.username,
					password : $scope.loginData.password
				},
				cache : false

			}

			$http(req)

			.success(function(data, status, headers, config) {
				$scope.login_error = 'OK...';

				$scope.playerData = data;
				$scope.playerData.stats = statsService.get({
					mId : data.id
				});
				loginService.setUser($scope.playerData);
				$window.location.href = '#dash';

			}).error(function(data, status, headers, config) {
				// called asynchronously if an error occurs
				// or server returns response with an error status.
				$scope.login_error = 'Email ou senha inválidos';

			});
		} else {
			$scope.login_error = 'Email ou senha inválidos';
		}

		// Simulate a login delay. Remove this and replace with your login
		// code if using a login system

	};

	$scope.doLoginFB = function() {

		openFB.login(function(response) {
			if (response.status === 'connected') {

				openFB.api({
					path : '/me',
					params : {
						fields : 'id,name,birthday,email,gender'
					},
					success : function(user) {
						$scope.$apply(function() {
							var headers = {

							};
							var req = {
								method : 'POST',
								url : atvpServerSecure + '/rest/login',
								headers : headers,
								params : {
									fb : true,
									email : user.email,
									name : user.name,
									fbId : user.id,
									birthday : user.birthday,
									gender : user.gender

								},
								cache : false

							}

							$http(req)

							.success(function(data, status, headers, config) {
								$scope.login_error = 'OK...';

								$scope.playerData = data;
								$scope.playerData.stats = statsService.get({
									mId : data.id
								});
								loginService.setUser($scope.playerData);
								$window.location.href = '#dash';

							}).error(function(data, status, headers, config) {
								// called asynchronously if an error occurs
								// or server returns response with an error
								// status.
								$scope.login_error = 'Erro Facebook';

							});

						});
					},
					error : function(error) {
						console.log("Login com Facebook falhou");

					}
				});

			} else {
				console.log("falhou");
			}
		}, {
			scope : 'email'
		});

	}
});

spApp.controller('DropdownCtrl', [ '$scope', '$log','$window', 'loginService', function($scope, $log,$window, loginService) {
	$scope.title = "Home";
	$scope.status = {
		isopen : false
	};

	$scope.toggled = function(open) {
		$log.log('Dropdown is now: ', open);
	};
	$scope.isLoggedIn = function() {
		return (loginService.getUser() != null);
	}
	$scope.toggleDropdown = function($event) {
		$event.preventDefault();
		$event.stopPropagation();
		$scope.status.isopen = !$scope.status.isopen;
	};
	$scope.logoff = function(){
		$scope.playerData =  {
				stats:{
					allTimes:{
						
					},
				currentMonth:{
						
					},
				currentYear:{
						
					}
				},
				imageProfile: "img/profile.png"
		};
		
		loginService.setUser(null);
		$window.location.href = '#login';
	};

} ]);
spApp.controller('AppCtrl', [ '$scope', '$window', '$http', 'loginService', 'statsService', function($scope, $window, $http, loginService, statsService) {
	$scope.signData = {};
	$scope.loginData = {};
	$scope.playerData = {
		stats : {
			allTimes : {

			},
			currentMonth : {

			},
			currentYear : {

			}
		},
		imageProfile : "img/profile.png"
	};

	$scope.isLoggedIn = function() {
		return (loginService.getUser() != null);
	}
	if (loginService.getUser() != null) {
		$scope.playerData = loginService.getUser();

		$scope.playerData.stats = statsService.get({
			mId : $scope.playerData.id
		});

	} else {
		$window.location.href = '#login';
	}

} ]);

spApp.filter('toWeekStr', function(dateService) {
	return function(weekCount, scope) {

		if (weekCount) {

			var dates = dateService.getStartEndDatesForPreviousWeek(weekCount);

			return "Semana do " + dates.end.getDate() + "/" + (dates.end.getMonth() + 1) + " à " + dates.start.getDate() + "/" + (dates.start.getMonth() + 1);
		} else {
			return "Última semana";
		}
	}
})

.filter('setCounter', function() {

	return function(match, playerPosition) {
		if (match) {
			console.log("asdfas", match)
			if (match.matchState == 'Completed') {
				var total = match.totalSetsPlayerOne;
				var half = "half1.png";
				if (playerPosition == 2) {
					total = match.totalSetsPlayerTwo;
					half = "half2.png";
				}
				if (total == 1) {
					return half;
				} else if (total >= 2) {
					return "full.png";
				}
			}
			return 

			

						

			

									

			

						

			

												

			

						

			

									

			

						

			

															

			

						

			

									

			

						

			

												

			

						

			

									

			

						

			

																		

			

						

			

									

			

						

			

												

			

						

			

									

			

						

			

															

			

						

			

									

			

						

			

												

			

						

			

									

			

						

			

																					

			

						

			

									

			

						

			

												

			

						

			

									

			

						

			

															

			

						

			

									

			

						

			

												

			

						

			

									

			

						

			

																		

			

						

			

									

			

						

			

												

			

						

			

									

			

						

			

															

			

						

			

									

			

						

			

												

			

						

			

									

			

						

			

		}
	}
	return '';
}

).filter('byWeek', function(dateService) {

	return function(items, scope) {
		if (items) {
			var dates = dateService.getStartEndDatesForPreviousWeek(scope.weekCount);
			var start = dates.start.getTime();
			var end = dates.end.getTime();
			console.log(dates);
			var weekMatches = [];
			scope.totalMatches = 0;
			for (var i = 0; i < items.length; i++) {
				var m = items[i];
				if (m.date > end && m.date < start) {
					weekMatches.push(m);
					scope.totalMatches++;
				} else if (m.date < end) {
					break;
				}
			}
			return weekMatches;
		}
	};
});
spApp.filter('percentage', [ '$filter', function($filter) {
	return function(input, decimals) {
		return $filter('number')(input * 100, decimals) + '%';
	};
} ]).filter('totalsetwon', [ '$filter', function($filter) {
	return function(input, periodrange) {

		if (input && input.$resolved == true && input[periodrange]) {
			return input[periodrange].totalSetWon + "/" + (input[periodrange].totalSetWon + input[periodrange].totalSetLost);

		} else {
			return "ND";
		}
	};
} ]).filter('matchcomp', [ '$filter', function($filter) {
	return function(input, periodrange) {
		if (input && input.$resolved == true && input[periodrange]) {
			switch (periodrange) {
				case "currentMonth":
					var monthAvg = input.currentMonth.totalMatches;
					var yearAvg = input.currentYear.totalMatches / (new Date().getMonth() + 1);
					if (monthAvg < yearAvg) {
						return "icon ion-arrow-down-c caiu";
					} else if (monthAvg > yearAvg) {
						return "icon ion-arrow-up-c subiu";
					} else {
						return "icon ion-minus-round manteve";
					}
				break;

				default:
				break;
			}

		} else {
			return "icon ";
		}

	}
} ])

.filter('setcomp', [ '$filter', function($filter) {
	return function(input, periodrange) {
		if (input && input.$resolved == true && input[periodrange]) {
			var totalSetPlayedInMonth = (input.currentMonth.totalSetWon + input.currentMonth.totalSetLost);
			var totalSetPlayedInYear = (input.currentYear.totalSetWon + input.currentYear.totalSetLost);
			var totalSetPlayedInCareer = (input.allTimes.totalSetWon + input.allTimes.totalSetLost);

			var allTimes = 0;
			if (totalSetPlayedInCareer > 0) {
				allTimes = input.allTimes.totalSetWon / totalSetPlayedInCareer;
			}

			var monthAvg = 0;
			if (totalSetPlayedInMonth > 0) {
				monthAvg = input.currentMonth.totalSetWon / totalSetPlayedInMonth;
			}
			var yearAvg = 0;
			if (totalSetPlayedInYear) {
				yearAvg = input.currentYear.totalSetWon / totalSetPlayedInYear;
			}
			switch (periodrange) {
				case "currentMonth":

					if (monthAvg < yearAvg || (totalSetPlayedInMonth == 0 && totalSetPlayedInYear > 0)) {
						return "icon ion-arrow-down-c caiu";
					} else if (monthAvg > yearAvg) {
						return "icon ion-arrow-up-c subiu";
					} else {
						return "icon ion-minus-round manteve";
					}
				break;

				case "currentYear":

					if (yearAvg < allTimes || (totalSetPlayedInYear == 0 && totalSetPlayedInCareer > 0)) {
						return "icon ion-arrow-down-c caiu";
					} else if (yearAvg > allTimes) {
						return "icon ion-arrow-up-c subiu";
					} else {
						return "icon ion-minus-round manteve";
					}
				break;

				default:
				break;
			}

		} else {
			return "icon ";
		}

	}
} ]).filter('gamescomp', [ '$filter', function($filter) {
	return function(input, periodrange) {
		if (input && input.$resolved == true && input[periodrange]) {
			var totalGamePlayedInYear = (input.currentYear.totalGamesWon + input.currentYear.totalGamesLost);
			var totalGamePlayedInMonth = (input.currentMonth.totalGamesWon + input.currentMonth.totalGamesLost);
			switch (periodrange) {
				case "currentMonth":
					var monthAvg = input.currentMonth.totalGamesWon / (input.currentMonth.totalGamesWon + input.currentMonth.totalGamesLost);
					var yearAvg = input.currentYear.totalGamesWon / (input.currentYear.totalGamesWon + input.currentYear.totalGamesLost);
					if (monthAvg < yearAvg || (totalGamePlayedInMonth == 0 && totalGamePlayedInYear > 0)) {
						return "icon ion-arrow-down-c caiu";
					} else if (monthAvg > yearAvg) {
						return "icon ion-arrow-up-c subiu";
					} else {
						return "icon ion-minus-round manteve";
					}
				break;

				case "currentYear":

					var yearAvg = input.currentYear.totalGamesWon / (input.currentYear.totalGamesWon + input.currentYear.totalGamesLost);
					var allTimes = input.allTimes.totalGamesWon / (input.allTimes.totalGamesWon + input.allTimes.totalGamesLost);
					if (yearAvg < allTimes) {
						return "icon ion-arrow-down-c caiu";
					} else if (yearAvg > allTimes) {
						return "icon ion-arrow-up-c subiu";
					} else {
						return "icon ion-minus-round manteve";
					}
				break;

				default:
				break;
			}

		} else {
			return "icon ";
		}

	}
} ]).filter('totalgameswon', [ '$filter', function($filter) {
	return function(input, periodrange) {

		if (input && input.$resolved == true && input[periodrange]) {
			return input[periodrange].totalGamesWon + "/" + (input[periodrange].totalGamesWon + input[periodrange].totalGamesLost);

		} else {
			return "ND";
		}
	};
} ])

.filter('totalperiod', [ '$filter', function($filter) {
	return function(input, objname, periodrange) {
		if (input && input.$resolved == true && input[periodrange]) {
			return input[periodrange][objname];
		} else {
			return "ND";
		}
	};
} ]);
