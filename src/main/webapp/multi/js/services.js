angular.module('ATVPmultiApp.services', [ 'ngResource', 'ngStorage' ])

.factory('OpponentDataService', function($q, $timeout,$resource,atvpServer) {
	return{		
		query : $resource(atvpServer + '/rest/player?action=loadfriends').query,
		searchOpponent:function(searchFilter,currentUserEmail,opponents) {	    	
			console.log("op",opponents)
	        var deferred = $q.defer();

		    var matches = opponents.filter( function(airline) {
		    	if(airline.email && airline.email!=currentUserEmail && airline.name && airline.name.toLowerCase().indexOf(searchFilter.toLowerCase()) !== -1 ) return true;
		    })

	        $timeout( function(){
	        
	           deferred.resolve( matches );

	        }, 100);

	        return deferred.promise;

	    }
	}
    
})
.factory('Friendship', function($http, $resource, atvpServer) {
	return{		
		query : $resource(atvpServer + '/rest/player?action=loadfriends',{},{get: 
	    {
	        cache: true,
	        method: 'GET'
	    }
	}).query,
		queryInvitations : $resource(atvpServer + '/rest/friendship?action=loadinvitations').query,
		queryInviteds : $resource(atvpServer + '/rest/friendship?action=loadinvitationssent').query,
		acceptInvitation : $resource(atvpServer + '/rest/friendship?action=acceptinvitation').query,
		declineInvitation : $resource(atvpServer + '/rest/friendship?action=declineinvitation').query,
		
	}
})
.factory('Match', function($http, $resource, atvpServer) {

	return {
		
		removeSet:function(data, successfct, errorfct) {
			var headers = {
				'Content-Type' : 'application/x-www-form-urlencoded'
			};
			data.action = 'deleteset';
			var req = {
				method : 'POST',
				url : atvpServer + '/rest/match',
				headers : headers,
				data : data,
				cache : false

			}

			$http(req).success(successfct).error(errorfct);
			
		},
		updateSet:function(data, successfct, errorfct) {
			console.log("todo update set",data)
			var headers = {
				'Content-Type' : 'application/x-www-form-urlencoded'
			};
			data.action = 'updateset';
			var req = {
				method : 'POST',
				url : atvpServer + '/rest/match',
				headers : headers,
				data : data,
				cache : false

			}

			$http(req).success(successfct).error(errorfct);
			
		},
		addSet :  function(data, successfct, errorfct) {

			var headers = {
				'Content-Type' : 'application/x-www-form-urlencoded'
			};
			data.action = 'addset';
			var req = {
				method : 'POST',
				url : atvpServer + '/rest/match',
				headers : headers,
				data : data,
				cache : false

			}

			$http(req).success(successfct).error(errorfct);
		}
,	
		get : $resource(atvpServer + '/rest/match?action=getdetails&key=:mId').get,
		query : $resource(atvpServer + '/rest/match?action=listdetails&idRanking=:rankingId&idPlayer=:idPlayer').query,
	//	delete : $resource(atvpServer + '/rest/match?action=delete&key=:mId').delete,
		save : function(match, successfct, errorfct) {

			var headers = {
				'Content-Type' : 'application/x-www-form-urlencoded'
			};
			match.action = 'persist';
			var req = {
				method : 'POST',
				url : atvpServer + '/rest/match',
				headers : headers,
				data : match,
				cache : false

			}

			$http(req).success(successfct).error(errorfct);
		}
	}

}).factory('statsService', function($http, $localStorage,$resource,atvpServer) {
	return $resource(atvpServer + '/rest/player?action=loadwithstats&id=:mId&'+$localStorage.httpCache,{}, 
			{get: 
			    {
			        cache: true,
			        method: 'GET'
			    }
			});
})
.factory('userService', function($http, $localStorage,$resource,atvpServer) {
	return {
		updateUser:function(userId,imageProfile,facebookId,successfct,errorfct){
			var headers = {
					'Content-Type' : 'application/x-www-form-urlencoded'
				};
				var data = {
					action:"persist",
					id:userId+"",					
					fbId:facebookId+''
				};
				var req = {
					method : 'POST',
					url : atvpServer + '/rest/player',
					headers : headers,
					data : data,
					cache : false

				}

			$http(req).success(successfct).error(errorfct);	
		}
	}
})
.factory('dateService', function() {
	return {
		getStartEndDatesForPreviousWeek:function(weekCount){
			var dailyMs= 1000*60*60*24;
			if(weekCount==0){
				
			}
			var firstReduction =  (weekCount-1)*7*dailyMs;
			
			var start = this.getFirstSundayBefore(new Date());
			start.setHours(11);
			start.setMinutes(59);
			if(weekCount==0){
				return {
					start:new Date(),
					end:start
				}
				
			}
			
			
			start.setTime(start.getTime() - firstReduction);			
			
			
			var end = new Date(start.getTime());
			end.setHours(0);
			end.setMinutes(0);
			end.setTime(end.getTime() - dailyMs);
			end =this.getFirstSundayBefore(end);
			return {start:start,end:end};
		},
		getFirstSundayBefore:function(refDate){
			var dailyMs= 1000*60*60*24;
			var start = new Date(refDate.getTime());			
			while(start.getDay()!=0){
				start.setTime(start.getTime() - dailyMs);
				console.log("start b",start);
			}
			return start;
		}
	}
})
.factory('loginService', function($http, $localStorage) {

	return {
		setUser : function(aUser) {
			$localStorage.user = aUser;
		},
		getUser : function() {
			var user= $localStorage.user!=null? $localStorage.user : null;
			if(user!=null && (user.imageProfile==null || user.imageProfile=="")){
				user.imageProfile="img/profile.png";
			}
			return user;
		},
		isLoggedIn : function() {
			return ($localStorage.user) ? $localStorage.user : false;
		}
	};
}).factory('Ranking', function($resource,$http, atvpServer) {

	return {
		get : $resource(atvpServer + '/rest/ranking?action=get&id=:mId&idManager=:idPlayer').get,
		save : function(data, successfct, errorfct) {

			var headers = {
				'Content-Type' : 'application/x-www-form-urlencoded'
			};
			data.action = 'persist';
			var req = {
				method : 'POST',
				url : atvpServer + '/rest/ranking',
				headers : headers,
				data : data,
				cache : false

			}

			$http(req).success(successfct).error(errorfct);
		},
		query : function(id) {
			return $resource(atvpServer + '/rest/ranking?action=list', {}, {
				query : {
					method : 'GET',
					params : {
						idManager : id
					},

					isArray : true
				}
			}).query();
		}
	}

});