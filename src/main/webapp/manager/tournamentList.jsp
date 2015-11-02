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
				idManager :
<%=idManager%>
	,
				name : $("[name='name']").val(),
				rounds : $("[name='rounds']").val()
			}),
			success : function() {
				console.log("asdfsaf");
				window.location = window.location;
			},
			dataType : "json"
		});

	}
</script>
Criar torneio
<br>

<form action="#" id="torneio">
	Nome: <input type="text" name="name"><br> # de rounds: <input
		type="number" name="rounds"><br> <input type="hidden"
		name="manager" value="<%=idManager%>"> <input type="button"
		onclick="saveTour()" value="Criar">
</form>