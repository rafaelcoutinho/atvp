<%@page import="java.util.Iterator"%>
<%@page import="com.google.appengine.api.datastore.Entity"%>
<%@page import="com.coutinho.atvp.db.DBFacade"%>
<%@page import="com.coutinho.atvp.entities.Tournament"%>
<%@page import="java.util.List"%>
<html lang="en">
<head>
<meta charset="utf-8">
<title>Gerenciar Torneio</title>
<link rel="stylesheet"
	href="//code.jquery.com/ui/1.11.2/themes/smoothness/jquery-ui.css">
<link rel="stylesheet"
	href="//cdn.datatables.net/1.10.4/css/jquery.dataTables.css">
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.2/jquery-ui.js"></script>
<script src="//cdn.datatables.net/1.10.4/js/jquery.dataTables.min.js"></script>

<%
	if (request.getParameter("idManager") == null) {
%>
Nao é gerente!
<%
	return;

	}
	Long idManager = Long.parseLong(request.getParameter("idManager"));
%>
<script>
	function saveTour() {
		console.log();
		$.ajax({
			type : "POST",
			contentType : 'application/json',
			url : "/endpoints/tournament",
			data : JSON.stringify({
				idManager : ${param.idManager},
				name : $("[name='name']").val(),
				rounds : $("[name='rounds']").val()
			}),
			success : function(data) {
				console.log("Criou torneio",data);
				$( "#tournamentName" ).val(data.name );
				$( "#tournamentId" ).val(data.id );
			},
			dataType : "json"
		});
		return false;

	}
	function addPlayer() {
		$.ajax({
			type : "POST",
			contentType : 'application/json',
			url : "/endpoints/tournament/"+$("[name='tournament']").val()+"/players/"+$("[name='player']").val(),
			
			success : function(data) {
				console.log("vai setar ",data);
				
				
			},
			dataType : "json"
		});
		return false;

	};
	function listPlayers() {
		$.ajax({
			type : "GET",
			contentType : 'application/json',
			url : "/rest/player?action=list",
			
			success : function(data) {
				console.log("p",data);
				$( "#players" ).autocomplete({
				      source: data,
				      minLength: 0,				      
				      focus: function( event, ui ) {
				        $( "#players" ).val( ui.item.name );
				        return false;
				      },
				      select: function( event, ui ) {
				          $( "#players" ).val( ui.item.name );
				          $( "#playersid" ).val( ui.item.id );			          
				          return false;
				        }
				    }).autocomplete( "instance" )._renderItem = function( ul, item ) {
				        return $( "<li>" )
				        .append( "<a>" + item.name + "</a>" )
				        .appendTo( ul );
				    };
				
			},
			dataType : "json"
		});
		return false;

	};
	function listTournments() {
		$.ajax({
			type : "GET",
			contentType : 'application/json',
			url : "/endpoints/tournament?id=${param.idManager}",
			
			success : function(data) {
				console.log("p",data);
				for ( var tourId in data) {
					var tour = data[tourId];
					$("#tournments").append("<a href='/manager/showBrackets.jsp?id="+tour.id+"'>"+tour.name+"</a><br>");
				}
				
			},
			dataType : "json"
		});
		return false;

	};
	$( document ).ready(function() {
	    console.log( "ready!" );
	    listPlayers();
	    listTournments();
	});
</script>
Criar torneio
<br>

<form action="#" id="torneio">
	Nome: <input type="text" name="name"><br> # de rounds: <input
		type="number" name="rounds"><br> <input type="hidden"
		name="manager" value="${param.idManager}"> <input
		type="button" onclick="saveTour()" value="Criar">
</form>
<div id="tournments"></div>


<form action="#" id="associa">
	<div class="ui-widget">
		<label for="players">Jogador: </label> <input id="players" type="text"
			name="playerName"> <input type="hidden" id="playersid"
			name="player"> <input type="text" id="tournamentName"
			disabled="disabled"> <input type="hidden" id="tournamentId"
			name="tournament"> <input type="button" onclick="addPlayer()"
			value="Participar">
	</div>
</form>