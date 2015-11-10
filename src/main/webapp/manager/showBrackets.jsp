
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

<script src="/js/jquery.bracket.min.js"></script>

<link href="/js/jquery.bracket.min.css" rel="stylesheet" type="text/css" />
<!-- you don't need to use this -->
<script>
	var criaMatch = function(idSm, p1, p2) {
		$.ajax({
			type : "POST",
			url : "/endpoints/tournament/${param.id}/matches",
			data : {
				smId : idSm,
				p1 : p1,
				p2 : p2
			},
			success : function(data) {
				console.log(data);
			},
			dataType : "json"

		})
	};
	$.ajax({
		type : "GET",
		url : "/endpoints/tournament/${param.id}/matches",

		success : function(data) {
			var round = -1;
			var counter = 1;
			var teams = [];
			for (var i = data.length - 1; i >= 0; i--) {
				var schematch = data[i];

				if (round == -1) {
					round = schematch.round;
				}

				if (schematch.round == round) {
					var match = schematch.match;
					console.log(i, schematch)
					if (match == null) {
						teams.push([ "", "", schematch.id ]);
					} else {
						var one ="";
						if(match.playerOne){
							one=match.playerOne.name+" "+match.playerOne.email;
						}
						var two ="";
						if(match.playerTwo){
							two=match.playerTwo.name+" "+match.playerTwo.email;
						}
						teams.push([ one, two, schematch.id ]);
					}
				}
			}
			minimalData.teams = teams;
			$('#minimal .demo').bracket({
				init : minimalData,
				save : function(data) {
					console.log("save ", data)
					for (var j = 0; j < data.teams.length; j++) {
						console.log(j, data.teams[j])
						if (data.teams[j][0] != "" || data.teams[j][1] != "") {

							var p = getParticipant(data.teams[j][0]);
							if (p) {
								criaMatch(data.teams[j][2], p.participant.id, null);
							} else {
								console.log("nao achou ", data.teams[j][0])
							}

						}
					}
				}, /* without save() labels are disabled */
				decorator : {
					edit : acEditFn,
					render : acRenderFn
				}
			/* data to initialize the bracket with */})

		},
		dataType : "json"
	});
	var getParticipant = function(key1) {
		for (var i = 0; i < participants.length; i++) {
			var entry = participants[i];
			var key = entry.participant.name + " " + entry.participant.email;

			if (key == key1) {
				return entry;
			}

		}
	}
	var participants = [];
	$.ajax({
		type : "GET",
		url : "/endpoints/tournament/${param.id}/players",
		success : function(data) {
			participants = data;
			acData = [];
			for (var i = 0; i < participants.length; i++) {
				var entry = participants[i];
				var key = entry.participant.name + " " + entry.participant.email;

				acData.push(key);
				console.log(acData);

			}

		},
		dataType : "json"
	});

	var minimalData = {
		teams : [],
		results : []
	}
	var reverseData = {};
	/* Data for autocomplete */
	var acData = [ "kr:MC", "ca:HuK", "se:Naniwa", "pe:Fenix", "us:IdrA", "tw:Sen", "fi:Naama" ]

	/* If you call doneCb([value], true), the next edit will be automatically 
	   activated. This works only in the first round. */
	function acEditFn(container, data, doneCb) {
		var input = $('<input type="text">')
		input.val(data)
		input.autocomplete({
			source : acData
		})
		input.blur(function() {
			doneCb(input.val())
		})
		input.keyup(function(e) {
			if ((e.keyCode || e.which) === 13)
				input.blur()
		})
		container.html(input)
		input.focus()
	}

	function acRenderFn(container, data, score) {
		container.append(data)
		/*var fields = data.split(':')
		if (fields.length != 2) {
			container.append('--')
		} else {
			container.append('<img src="site/png/'+fields[0]+'.png"> ').append(fields[1])
		}*/
	}
</script>
</head>
<body>
	<div id="minimal">
		<div class="demo"></div>

	</div>


</body>