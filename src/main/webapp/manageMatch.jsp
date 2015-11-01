
<html lang="en">
<head>
<meta charset="utf-8">
<title>Gerenciar Set</title>
<link rel="stylesheet"
	href="//code.jquery.com/ui/1.11.2/themes/smoothness/jquery-ui.css">
<link rel="stylesheet"
	href="//cdn.datatables.net/1.10.4/css/jquery.dataTables.css">
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.2/jquery-ui.js"></script>
<script src="//cdn.datatables.net/1.10.4/js/jquery.dataTables.min.js"></script>

<style>
#draggable {
	width: 150px;
	height: 150px;
	padding: 0.5em;
}
</style>
<script>
	function saveSet(number){
		console.log(number);
		$.ajax({
			  type: "POST",
			  url: "/rest/match",
			  data: {
				  number:number,
				  action:"updateset",
				  idRanking:idRanking,
				  id:matchId,
				  playerOneGames:$("[name='playerOneGames_"+number+"']").val(),
				  playerTwoGames:$("[name='playerTwoGames_"+number+"']").val()
			  },
			  success: function(){
				  console.log("asdfsaf");
				  window.location=window.location;
			  },
			  dataType: "json"
			});
		
	}
	function deleteLastSet(){
		$.ajax({
			  type: "POST",
			  url: "/rest/match",
			  data: {
				  action:"deleteset",
				  idRanking:idRanking,
				  id:matchId
			  },
			  success: function(){
				  console.log("asdfsaf");
				  window.location=window.location;
			  },
			  dataType: "json"
			});
	}
	function addNewSet(){
		$.ajax({
			  type: "POST",
			  url: "/rest/match",
			  data: {
				  action:"addset",
				  idRanking:idRanking,
				  id:matchId
			  },
			  success: function(){
				  console.log("asdfsaf");
				  window.location=window.location;
			  },
			  dataType: "json"
			});
	}
	var matchId= <%=request.getParameter("id")%>;
	var idRanking= <%=request.getParameter("idRanking")%>;
	$(document).ready(function() {

		$.getJSON("/rest/player?action=list", function(data) {

			$.each(data, function(index, item) { // Iterates through a collection
				playerHash[item.id] = item;
			});
		});
		
		$.getJSON("/rest/match?action=getdetails&id="+matchId+"&idRanking="+idRanking,
										function(data) {
											console.log("match ", data);
											$('#example')
													.dataTable(
															{
																"data" : data.sets,

																"columns" : [
																		{
																			"data" : "number",
																			"render" : function(
																					data,
																					type,
																					full,
																					meta) {
																				return data + 1;
																			}

																		},
																		{
																			"data" : "playerOneGames",
																			"render" : function(
																					data,
																					type,
																					full,
																					meta) {
																				return "<input type='text' name='playerOneGames_"+full.number+"' value='"+data+"'>";
																			}
																		},
																		{
																			"data" : "playerTwoGames",
																			"render" : function(
																					data,
																					type,
																					full,
																					meta) {
																				return "<input type='text' name='playerTwoGames_"+full.number+"' value='"+data+"'>";
																			}
																		},
																		{
																			"data" : "id",
																			"render" : function(
																					data,
																					type,
																					full,
																					meta) {
																				return "<input type='button' value='Salvar' onclick='saveSet("
																						+ full.number
																						+ ")'>";
																			}
																		}

																]
															});
										});
					});

	var playerHash = {};
</script>
</head>
<body>
	<div>
		<h2>Partida</h2>
		<br>
		<div id="info"></div>
		<div id="players">
			<span id="p1"></span> vs <span id="p2"></span>
		</div>
		<br>
		<table id="example" class="display" cellspacing="0" width="100%">
			<thead>
				<tr>
					<th>Set</th>
					<th>Pontos p1</th>
					<th>Pontos p2</th>
					<th></th>
				</tr>
			</thead>

			<tbody>
			</tbody>
		</table>

	</div>
</body>
<input type='button' value='Apagar Ultimo' onclick='deleteLastSet()'/>
<input type='button' value='Adicionar Set' onclick='addNewSet()'/>
</html>