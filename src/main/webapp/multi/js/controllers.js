angular.module('starter.controllers', [ 'starter.services', 'ngCordova' ]).config([ '$httpProvider', function($httpProvider) {

	$httpProvider.defaults.useXDomain = true;
	
	delete $httpProvider.defaults.headers.common['X-Requested-With'];

} ])


.controller('AppCtrl', function($scope, $ionicModal, $timeout, $http, $state, atvpServer,atvpServerSecure, loginService,statsService) {

	$scope.signData = {};
	$scope.loginData = {};
	
	//

	// Create the login modal that we will use later
	$ionicModal.fromTemplateUrl('templates/login.html', {
		scope : $scope
	}).then(function(modal) {
		$scope.modal = modal;
	});

	$scope.playerData = {
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
	
	

	$scope.convertJsonToForm = function(obj) {
		var str = [];
		for ( var p in obj)
			str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
		return str.join("&");
	}

	// Triggered in the login modal to close it
	$scope.closeLogin = function() {
		$scope.modal.hide();
	};
	$scope.resetPwd = function() {
		if($scope.loginData.username==null || $scope.loginData.username==""){
			$scope.login_error = 'Digite seu email!';
		}else{
		var req = {
				method : 'POST',
				url : atvpServerSecure + '/rest/player',
				params : {
					email : $scope.loginData.username,
					action:'rememberpwd'
				},
				cache : false

			}

			$http(req)

			.success(function(data, status, headers, config) {
				$scope.login_error = 'Senha enviada para seu email...';
				$state.go('app.dash');

			}).error(function(data, status, headers, config) {
				$scope.login_error = 'Erro';

			});
		}
		return true;
	};
	

	// Open the login modal
	$scope.login = function() {

		$scope.modal.show();
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
		$scope.login();
	};

	$scope.isLoggedIn = function() {
		return loginService.isLoggedIn();
	};
	$scope.doRefresh =function(){
		if (loginService.getUser() != null) {
			$scope.playerData = loginService.getUser();
			

			$scope.playerData.stats = statsService.get(
					{
						mId : $scope.playerData.id
					});
			

		}
	};
	
	$scope.doLoginFB = function(){

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
										fb:true,
										email : user.email,
										name:user.name,
										fbId:user.id,
										birthday:user.birthday,
										gender:user.gender
										
									},
									cache : false

								}

								$http(req)

								.success(function(data, status, headers, config) {
									$scope.login_error = 'OK...';
									
									$scope.playerData = data;
									$scope.playerData.stats = statsService.get(
											{
												mId : data.id
											});
									loginService.setUser($scope.playerData);
									$scope.closeLogin();
									$state.go('app.dash');

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
				$scope.playerData.stats = statsService.get(
						{
							mId : data.id
						});
				loginService.setUser($scope.playerData);
				$scope.closeLogin();
				$state.go('app.dash');

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
	$scope.doCreateUser = function() {
		$scope.signup_error = '';
		$scope.signup_success = '';

		if (!$scope.signData.email || $scope.signData.email.length < 3) {
			$scope.signup_error = 'Um E-mail válido é obrigatório';
		} else if (!$scope.signData.password || $scope.signData.password.length < 3) {
			$scope.signup_error = 'Senha é obrigatório e deve ter no mínimo 3 dígitos';
		} else {

			var req = {
				method : 'POST',
				headers : {
					'Content-Type' : 'application/x-www-form-urlencoded'
				},
				url : atvpServer + '/rest/signup',
				data : $scope.signData,
				transformRequest : $scope.convertJsonToForm,
				cache : false
			}

			$http(req)

			.success(function(data, status, headers, config) {
				$scope.signup_error = '';
				$scope.signup_success = 'OK...';
				
				$scope.playerData = data;
				$scope.playerData.stats = statsService.get(
						{
							mId : data.id
						});
				loginService.setUser($scope.playerData);
				$state.go('app.dash');

			}).error(function(data, status, headers, config) {
				if (data.error == 'existing_user') {
					$scope.signup_error = 'Usuário já existe na base. Tente se logar.';
				} else if (data.error == 'invalid_data') {
					$scope.signup_error = 'Dados inválidos';
				} else {
					$scope.signup_error = 'Erro ao cadastrar';
				}
			});
		}
	};
	if (loginService.getUser() != null) {
		$scope.playerData = loginService.getUser();
// console.log("status",$scope.playerData.stats)
// $scope.playerData.stats = $scope.playerData.stats;
		// tenta atualizar
		$scope.playerData.stats = statsService.get(
				{
					mId : $scope.playerData.id
				});
		$state.go('app.dash');

	}
}).controller('ProfileCtrl', function($scope, $stateParams, $state, loginService,userService) {
	var editing = false;
	$scope.isEditing=function(){
		return editing;
	}
	$scope.showEdit = function() {
		return $state.current.name == 'app.profile';
	};
	$scope.edit = function() {
		editing=true;
	};
	$scope.save = function() {
		editing=false;
	};
	$scope.notFBed = function(){
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
								console.log("fbUser",user);
								var imageProfile = "http://graph.facebook.com/" + user.id + "/picture?width=270&height=270";
								$scope.playerData.imageProfile = imageProfile;
								userService.updateUser(loginService.getUser().id,imageProfile, user.id,function(arg1,arg2){
									console.log(arg1,arg2)
								},
								function(arg1,arg2){
									console.log(arg1,arg2)
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
	

}).controller('RankingsCtrl', function($scope, $stateParams, $state, Ranking, loginService, $ionicModal) {
	if (loginService.getUser() != null) {
		$scope.rankings = Ranking.query(loginService.getUser().id);
	}
	
	$scope.showNewRanking = function() {
		if (!$state.current || !$state.current.name) {
			return false;
		}
		return $state.current.name == ('app.rankings') && loginService.getUser()!=null?loginService.getUser().licenses>0:false;
	};
	
	
	// Create the login modal that we will use later
	$ionicModal.fromTemplateUrl('templates/new_ranking.html', {
		scope : $scope
	}).then(function(modal) {
		$scope.modal = modal;
	});
	$scope.closeForm = function() {
		$scope.modal.hide();
	};
	
	$scope.clearData = function() {
		$scope.ranking = {
				idManager:loginService.getUser()!=null?loginService.getUser().id:null,
				created: Date.now(),
				title:'',
				description:''
				
			}
	};
	$scope.clearData();
	$scope.addNewRanking = function() {
		$scope.clearData();
		$scope.modal.show();
	};
	$scope.createRanking=function(){
		Ranking.save($scope.ranking, function(success) {

			$scope.closeForm();
			$scope.doRefresh();
		}, function(error) {

			console.log("ERROR: " + error.error);
			
				$scope.closeForm();
				var alertPopup = $ionicPopup.alert({
					title : 'Erro criando ranking',
					template : 'Houve um erro criando a ranking. Tente novamente.'
				});

			
		});
		
	}
	$scope.doRefresh = function() {
		var $httpDefaultCache = $cacheFactory.get('$http');
		
		$scope.rankings = Ranking.query(loginService.getUser()!=null?loginService.getUser().id:null);
	}
}).controller('RankingCtrl', function($scope, $ionicPopup, $ionicModal, $filter, $state, $stateParams, Ranking, Match, loginService, $cordovaContacts, OpponentDataService) {

	$scope.showNewMatch = function() {
		if (!$state.current || !$state.current.name) {
			return false;
		}
		if($state.current.name == ('app.ranking')){
			if($scope.ranking!=null){
			var isManager = loginService.getUser()!=null?loginService.getUser().id==$scope.ranking.idManager:false;
			console.log("is manager",isManager)
					return isManager;
			}else{
				return false;
			}		
			
		}else{
			return  $state.current.name == ('app.matches') ;
		}
	};

	$scope.clearData = function() {
		var clearDate = new Date();
		clearDate.setMilliseconds(0);
		clearDate.setSeconds(0);
		
		$scope.nmatch = {
			playerOneEmail : loginService.getUser()!=null?loginService.getUser().email:null,
			playerTwoEmail : '',
			playerTwoName : '',
			date : clearDate,
			idRanking : $stateParams.id,
			forceInvitation : false

		};
	};
	$scope.clearData();
	$scope.getContactList = function() {
		$cordovaContacts.find({
			filter : ''
		}).then(function(result) {
			$scope.contacts = result;
		}, function(error) {

			console.log("ERROR: " + error.error);
		});
	}


	$scope.data = {
		"players" : [],
		"search" : ''
	};
	$scope.inviteNew = function() {
		var myPopup = $ionicPopup.show({
			template : '<input  placeholder="Nome" type="text" ng-model="nmatch.playerTwoName"><br><input  placeholder="E-mail" type="email" ng-model="nmatch.playerTwoEmail">',
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
					if (!$scope.nmatch.playerTwoEmail) {
						
						e.preventDefault();
					} else {
						$scope.nmatch.forceInvitation = true;
						return $scope.nmatch.playerTwoEmail;
					}
				}
			} ]
		});
		myPopup.then(function(res) {
			console.log('Tapped!', res);
		});
	}
	$scope.selectPlayer = function(email,n) {
		$scope.searchDialog.hide();
		if (email == inviteTxt) {
			$scope.inviteNew();
			$scope.data = {
				"players" : [],
				"search" : ''
			};
			return;
		}
		var selectedPlayer;
		for ( var id in $scope.data.players) {
			var p = $scope.data.players[id];
			if (p.email == email) {
				selectedPlayer = p;
				break;
			}
		}
		$scope.nmatch.playerTwoName = selectedPlayer.name;
		$scope.nmatch.playerTwoEmail = email;
		$scope.data = {
			"players" : [],
			"search" : ''
		};
	}
	var inviteTxt = '+ convidar por email';
	$scope.search = function() {
		if($scope.data.search==''){
			$scope.data = {
					"players" : [],
					"search" : ''
				};
		}else{
		OpponentDataService.searchOpponent($scope.data.search,$scope.nmatch.playerOneEmail,$scope.data.players).then(function(matches) {
			if (matches.length == 0) {
				matches.push({
					name : null,
					email : inviteTxt
				});
			}
			$scope.data.players = matches;
		});};
	}
	$scope.createMatch = function() {
		// $scope.nmatch.date = Date.parse($scope.date);
		console.log($scope.nmatch.date)
		$scope.nmatch.date=$scope.nmatch.date.getTime();

		Match.save($scope.nmatch, function(success) {
			$scope.closeForm();
			$scope.doRefresh();
		}, function(error) {

			
			if ($scope.nmatch.forceInvitation == false && error.error == "entity_not_found") {
				var confirmPopup = $ionicPopup.confirm({
					title : 'Adversário inexistente',
					template : 'Este adversário não existe, deseja convidá-lo para o ATVP?'
				});
				confirmPopup.then(function(res) {
					if (res) {
						$scope.nmatch.forceInvitation = true;
						$scope.createMatch();
					} else {
						$scope.closeForm();
					}
				});
			} else {
				$scope.closeForm();
				var alertPopup = $ionicPopup.alert({
					title : 'Erro criando partida',
					template : 'Houve um erro criando a partida. Tente novamente.'
				});

			}
		});

	};

	$ionicModal.fromTemplateUrl('templates/select_opponent.html', {
		scope : $scope
	}).then(function(modal) {
		$scope.searchDialog = modal;
	});
	$scope.closeSearch = function() {
		$scope.searchDialog.hide();
	};
	$scope.openSearch = function() {		
		$scope.searchDialog.show();
	};
	
	$ionicModal.fromTemplateUrl('templates/new_match.html', {
		scope : $scope
	}).then(function(modal) {
		$scope.modal = modal;
	});
	
	$scope.closeForm = function() {
		$scope.modal.hide();
	};
	$scope.addNewMatch = function() {
		$scope.clearData();
		$scope.modal.show();
	};
	$scope.doRefresh = function() {
		if (loginService.getUser() != null) {
			$scope.data.players = OpponentDataService.query(
					{id:loginService.getUser().id}
			);
			if ($stateParams.id!=null) {
				$scope.ranking = Ranking.get({
					mId : $stateParams.id,
					idPlayer : loginService.getUser().id
				});
				$scope.currMatches = Match.query({
					rankingId : $stateParams.id
				});
			}
		}
	}
	$scope.doRefresh();
	

})
.controller('MatchCtrl', function($scope,  $state, $stateParams,$ionicModal, Match, loginService,$ionicPopup,$ionicViewService, $cordovaSocialSharing) {
	var changed = -1;
	 $scope.shareAnywhere = function() {
		 
		        openFB.api({
		        method: 'POST',
		        path: '/me/feed',
		        params: {
		            message: 'Testing Facebook APIs'
		        },
		        success: function() {
		                alert('OK');
		        },
		        error: function(){
		        	console.log("falhou")
		        }
		        });
		 
	 };
	 
	$scope.error_update_set="";
	$scope.confirmSetRemove=function(n){
		var confirmPopup = $ionicPopup.confirm({
			title : 'Apagar Set',
			template : 'Deseja apagar este Set?'
		});
		confirmPopup.then(function(res) {
			if (res) {
				var success=function(resp){
					$scope.refreshMatch();
					$scope.updateSet.hide();
				};
				var error=function(resp){
					console.log(resp);
					$scope.error_update_set="Falhou ao tentar remover o set. Tente novamente.";
				};
				Match.removeSet($scope.match.sets[n],success,error);
			} else {
			}
		});
	}
	
	
	$ionicModal.fromTemplateUrl('templates/update_set.html', {
		scope : $scope
	}).then(function(modal) {
		$scope.updateSet = modal;
	});
	$scope.removeSet=function(){
		$scope.confirmSetRemove($scope.editingSet.number);
	};
	$scope.finishSetUpdates = function() {
		var success=function(resp){
			console.log(resp)
			$scope.match.sets[$scope.editingSet.number]=(resp);
			changed=-1;
		};
		var error=function(resp){
			console.log(resp)
			
		};
		Match.updateSet($scope.editingSet,success,error);
		$scope.updateSet.hide();
	};
	
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
			key: $stateParams.mId
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
				Match.delete({mId : $stateParams.mId});						} else {
			}
		});
		
		
	}
	
	
	$scope.showDeleteMatch = function() {
		
		if($state.current.name == 'app.match'){
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
			if ($stateParams.mId) {
				$scope.match = Match.get({
					mId : $stateParams.mId,
					idPlayer : loginService.getUser().id
				}
			);
				
			}
		}
	}
	$scope.$on('$routeChangeSuccess', function(){
		var path = $location.path();
		console.log(path);
		$scope.showDeleteMatch();
		

	});
	$scope.refreshMatch();
	
})
.controller('FriendsCtrl', function($scope, $state, $stateParams,$http, Friendship,loginService,$ionicPopup,$ionicActionSheet,$timeout,atvpServer) {
	$scope.showFriendInvitation = function() {
		if (!$state.current || !$state.current.name) {
			return false;
		}
		if($state.current.name == ('app.friends')){
			return true;
		}
	};
	$scope.toInvite={};
	$scope.inviteFriend = function() {
		var myPopup = $ionicPopup.show({
			template : '<input  placeholder="Nome" type="text" ng-model="toInvite.name">'
				+'<br><input  placeholder="E-mail" type="email" ng-model="toInvite.invited">',
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
									id: loginService.getUser().id
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
	$scope.showAcceptDeny = function(invitation) {

		   // Show the action sheet
		   var hideSheet = $ionicActionSheet.show({
		     buttons: [
		       { text: '<b>Aceitar</b>' }
		       
		     ],
		     destructiveText: 'Recusar',
		     titleText: 'Convite de '+invitation.inviter.name,
		     cancelText: 'Cancelar',
		     cancel: function() {
		          // add cancel code..
		        },
		     buttonClicked: function(index) {
		    	 Friendship.acceptInvitation({id:invitation.id,idPlayer:loginService.getUser()!=null?loginService.getUser().id:null});
		    	 $scope.doRefresh();
		       return true;
		     },
		     destructiveButtonClicked: function(index) {
		    	 Friendship.declineInvitation({id:invitation.id,idPlayer:loginService.getUser()!=null?loginService.getUser().id:null});		    	 
		    	 $scope.doRefresh();
		       return true;
		     }
		   });

		   // For example's sake, hide the sheet after two seconds
		   $timeout(function() {
		     hideSheet();
		   }, 10000);

		 };
	$scope.doRefresh = function() {
		$scope.friends = Friendship.query({
			id : loginService.getUser()!=null?loginService.getUser().id:null
		});
		$scope.invitations = Friendship.queryInvitations({
			email : loginService.getUser()!=null?loginService.getUser().email:null
		});
		
		$scope.inviteds = Friendship.queryInviteds({
			id : loginService.getUser()!=null?loginService.getUser().id:null
		});
		
	}
	$scope.doRefresh();
})

.filter('toWeekStr', function(dateService) {
	return function(weekCount,scope) {
		
		if(weekCount){
			console.log("w",weekCount,"s",scope.weekCount)
			var dates = dateService.getStartEndDatesForPreviousWeek(weekCount);
			
			
			return "Semana do "+dates.end.getDate()+"/"+(dates.end.getMonth()+1)+" à "+dates.start.getDate()+"/"+(dates.start.getMonth()+1);
		}else{
			return "Última semana";
		}
	}
})


.filter('setCounter', function() {
  	
	  return function(match,playerPosition) {
	    if (match) {
	    	console.log("asdfas",match)
	    	if(match.matchState=='Completed'){	    		
		    		var total = match.totalSetsPlayerOne;
		    		var half = "half1.png";
		    		if(playerPosition==2){
		    			total = match.totalSetsPlayerTwo;
		    			half = "half2.png";
		    		}
		    		if(total==1){
		    			return half;
	    			}else if(total>=2){
	    				return "full.png";
	    			}	
    			}
	    		return 
	    	}
	    }
	    return '';
	  }

)
.filter('byWeek', function(dateService) {
  	
  return function(items,scope) {
    if (items) {
    	var dates = dateService.getStartEndDatesForPreviousWeek(scope.weekCount);
    	var start = dates.start.getTime();
    	var end = dates.end.getTime();
    	console.log(dates);
    	var weekMatches=[];	
		scope.totalMatches = 0;
		for (var i = 0; i < items.length; i++) {
			var m =items[i];
			if(m.date>end && m.date<start){
				weekMatches.push(m);	
				scope.totalMatches++;		
			}else if(m.date<end){
				break;
			}
		}
      return weekMatches;
    }
  };
})
.controller('MatchesCtrl', function($scope, $stateParams, Match,loginService) {
	$scope.weekCount = 0;
	$scope.doRefresh = function() {
		$scope.matches = Match.query({
			idPlayer : loginService.getUser()!=null?loginService.getUser().id:null
		});
	}
	$scope.movePrevious = function(){
		$scope.weekCount++;	
	};
	$scope.moveNext = function(){
		$scope.weekCount--;	
	};
	$scope.doRefresh();
}).filter('percentage', ['$filter', function ($filter) {
	  return function (input, decimals) {
		    return $filter('number')(input * 100, decimals) + '%';
		  };
		}])
.filter('totalsetwon', ['$filter', function ($filter) {
  return function (input,  periodrange) {
	 
	  if(input && input.$resolved==true && input[periodrange]){
	    return  input[periodrange].totalSetWon+"/"+ (input[periodrange].totalSetWon+ input[periodrange].totalSetLost); 
	    
	  }else{
		  return "ND";
	  }
  };
}])
.filter('matchcomp', ['$filter', function ($filter) {
	 return function (input,  periodrange) {
		 if(input && input.$resolved==true && input[periodrange]){
			 switch (periodrange) {
				case "currentMonth":
					var monthAvg = input.currentMonth.totalMatches;
					var yearAvg =  input.currentYear.totalMatches/(new Date().getMonth()+1);
					if(monthAvg<yearAvg){
						return "icon ion-arrow-down-c caiu";
					}else if(monthAvg>yearAvg){
						return "icon ion-arrow-up-c subiu";
					}else{
						return "icon ion-minus-round manteve";
					}
				break;
				
				
				default:
				break;
			}
			 
			 	 
			    
		  }else{
			  return "icon ";
		  }
		 
	 }
}])

.filter('setcomp', ['$filter', function ($filter) {
	 return function (input,  periodrange) {
		 if(input && input.$resolved==true && input[periodrange]){
			var totalSetPlayedInMonth = (input.currentMonth.totalSetWon+ input.currentMonth.totalSetLost);
			var totalSetPlayedInYear= (input.currentYear.totalSetWon + input.currentYear.totalSetLost);
			var totalSetPlayedInCareer= (input.allTimes.totalSetWon + input.allTimes.totalSetLost);
			
			var allTimes = 0;
			if(totalSetPlayedInCareer>0){
				allTimes=input.allTimes.totalSetWon/totalSetPlayedInCareer;
			}
			
			var monthAvg = 0;
			if(totalSetPlayedInMonth>0){
				monthAvg=input.currentMonth.totalSetWon/totalSetPlayedInMonth;
			}
			var yearAvg = 0;
			if(totalSetPlayedInYear){
				yearAvg=input.currentYear.totalSetWon/totalSetPlayedInYear;
			}
			 switch (periodrange) {
				case "currentMonth":
					
					if(monthAvg<yearAvg || (totalSetPlayedInMonth==0 && totalSetPlayedInYear>0)){
						return "icon ion-arrow-down-c caiu";
					}else if(monthAvg>yearAvg){
						return "icon ion-arrow-up-c subiu";
					}else{
						return "icon ion-minus-round manteve";
					}
				break;
				
				case "currentYear":
					
					
					if(yearAvg<allTimes || (totalSetPlayedInYear==0 && totalSetPlayedInCareer>0)){
						return "icon ion-arrow-down-c caiu";
					}else if(yearAvg>allTimes){
						return "icon ion-arrow-up-c subiu";
					}else{
						return "icon ion-minus-round manteve";
					}
				break;

				default:
				break;
			}
			 
			 	 
			    
		  }else{
			  return "icon ";
		  }
		 
	 }
}])
.filter('gamescomp', ['$filter', function ($filter) {
	 return function (input,  periodrange) {
		 if(input && input.$resolved==true && input[periodrange]){
			 var totalGamePlayedInYear= (input.currentYear.totalGamesWon + input.currentYear.totalGamesLost);
			 var totalGamePlayedInMonth= (input.currentMonth.totalGamesWon + input.currentMonth.totalGamesLost);
			 switch (periodrange) {
				case "currentMonth":
					var monthAvg = input.currentMonth.totalGamesWon/(input.currentMonth.totalGamesWon + input.currentMonth.totalGamesLost);
					var yearAvg = input.currentYear.totalGamesWon/(input.currentYear.totalGamesWon + input.currentYear.totalGamesLost);
					if(monthAvg<yearAvg || (totalGamePlayedInMonth==0 && totalGamePlayedInYear>0)){
						return "icon ion-arrow-down-c caiu";
					}else if(monthAvg>yearAvg){
						return "icon ion-arrow-up-c subiu";
					}else{
						return "icon ion-minus-round manteve";
					}
				break;
				
				case "currentYear":
					
					var yearAvg = input.currentYear.totalGamesWon/(input.currentYear.totalGamesWon+ input.currentYear.totalGamesLost);
					var allTimes = input.allTimes.totalGamesWon/(input.allTimes.totalGamesWon+ input.allTimes.totalGamesLost);
					if(yearAvg<allTimes){
						return "icon ion-arrow-down-c caiu";
					}else if(yearAvg>allTimes){
						return "icon ion-arrow-up-c subiu";
					}else{
						return "icon ion-minus-round manteve";
					}
				break;

				default:
				break;
			}
			 
			 	 
			    
		  }else{
			  return "icon ";
		  }
		 
	 }
}])
.filter('totalgameswon', ['$filter', function ($filter) {
  return function (input,  periodrange) {
	 
	  if(input && input.$resolved==true && input[periodrange]){
	    return  input[periodrange].totalGamesWon+"/"+ (input[periodrange].totalGamesWon+ input[periodrange].totalGamesLost); 
	    
	  }else{
		  return "ND";
	  }
  };
}])		
		
.filter('totalperiod', ['$filter', function ($filter) {
  return function (input, objname, periodrange) {
	  if(input && input.$resolved==true && input[periodrange]){
	    return input[periodrange][objname] ;
	  }else{
		  return "ND";
	  }
  };
}]);
