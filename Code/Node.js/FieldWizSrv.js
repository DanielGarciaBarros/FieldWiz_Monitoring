// liste de variables utilisées dans le script
var express = require('express');
var fs = require('fs');
var multer = require('multer');
var bodyParser = require('body-parser');
var path = require('path');
var exec = require('child_process').exec;
var neo4j = require('neo4j-driver').v1;
var driver = neo4j.driver("bolt://localhost:7687", neo4j.auth.basic("***", "***"));
var session = driver.session();
var app = new express();

app.use(bodyParser.json());
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

// Recupération des statistiques d'un utilisateur
app.get('/GetStats/:username/', function(req, res){
	session
		.run( "MATCH (a:Session)<-[r:DO]-(p:Person{username:\'" + req.params.username + "\'}) "+
		" RETURN a.nameSession AS nameSession, a.startSession " +
		"AS startSession, a.endSession AS endSession, a.speedMax AS speedMax, a.speedAvg " +
		"AS speedAvg, a.dist AS dist, a.lat AS lat, a.lon AS lon")
		.then( function( result ) {
			var resultStats = [];
			for(i = 0; i < result.records.length; i++){		
				resultStats.push({
					"nameSession" : result.records[i].get("nameSession"),
					"startSession" : result.records[i].get("startSession"),
					"endSession" : result.records[i].get("endSession"),
					"speedMax" : result.records[i].get("speedMax"),
					"speedAvg" : result.records[i].get("speedAvg"),
					"dist" : result.records[i].get("dist"),
					"lat" : result.records[i].get("lat"),
					"lon" : result.records[i].get("lon")
				});
			}
			res.setHeader('Content-Type', 'application/json');
            res.send(JSON.stringify(resultStats, null, 3));
    		session.close();
    		driver.close();
  		});
});

// Contrôle l'authentification d'un utilisateur
app.post('/Users', function(req, res){
	username = req.body.username;
	password = req.body.password;
	var isConnected;
	session
		.run("MATCH (a:Person {username: \'" + username +"\', password: \'" + password +
		"\'}) RETURN a.username AS username")
		.then(function(result){
		if(result.records[0] != null){
			exec('mkdir Users/' + username, function(error, stdout, stderr) {	});
			isConnected = true;
		} else{
			isConnected = false;
		}
		res.send(isConnected);
	});
	
});

// Récupération des sessions d'un ficher .FWZ
app.post('/Sessions', multer({ dest: 'files'}).single('file'), function(req,res){
	username = req.body.username;
	cmd = 'octave --silent --eval \"sessions(\'files/' + req.file.filename + '\')\"';
	exec(cmd, function(error, stdout, stderr) {		
		var arraySessions = stdout.split("\n");
		session
			.run( "MATCH (a:Session)<-[r:DO]-(p:Person{username:\'" + username +"\'})" + 
			 " RETURN a.startSession AS startSession")
			.then( function( result ) {
				var datesSessionDB = [];
				for(i = 0; i < result.records.length; i++){
					datesSessionDB.push({
						"startSession" : result.records[i].get("startSession")
					});
				}
				var datesSessionFile = [];
				for(i = 0; i < arraySessions.length-1; i = i + 2){ // gestiion des session déjà dans la BD
					isIn = false;
					for(j = 0; j < datesSessionDB.length; j++){
						isIn |= !datesSessionDB[j].startSession.localeCompare(arraySessions[i]);
					}
					if(!isIn){
						datesSessionFile.push({
							"startSession" : arraySessions[i],
							"endSession" : arraySessions[i+1]
						});
					}
				}
			res.setHeader('Content-Type', 'application/json');
            res.send(JSON.stringify(datesSessionFile, null, 3));    
    		session.close();
    		driver.close();
  		});
	});
});

// Création d'une session
app.post('/', multer({ dest: 'files'}).single('file'), function(req,res){	
	username = req.body.username;
	nameSession = req.body.nameSession;
    startSession = req.body.hourStartSession;
    endSession = req.body.hourEndSession;  
	cmd = 'octave --silent --eval \"readFwz(\'files/' + req.file.filename + '\',\''+ 
		startSession+'\',\''+ endSession+'\',\''+ username + '\')\"';
	exec(cmd, function(error, stdout, stderr) {	
		resultStats = stdout;
		var array = resultStats.split("\n");
		speedMax = parseFloat(array[0]);
		speedAvg = parseFloat(array[1]);
		dist = parseFloat(array[2]);
		lat = parseFloat(array[3]);
		lon = parseFloat(array[4]);
		session
		.run( "MATCH (p:Person {username:\'"+ username +"\'})" + 
		"CREATE (a:Session {nameSession: \'"+nameSession +"\', startSession: \'"+
		startSession+"\'," + "endSession: \'"+endSession+"\', speedMax: "+speedMax +
		", speedAvg: "+speedAvg+", dist: "+dist+", lat: "+lat+", lon: "+lon+"})<-[r:DO]-(p)")
		.then( function( result ) {
    		session.close();
    		driver.close();
  		});
	});
	
	res.send(req.file.filename);
});

// Création d'un compte (2ème partie: envoie de l'avatar)
app.post('/NewAccount', multer({ dest: 'Users/'}).single('img'), function(req,res){
	username = req.body.username;
	password = req.body.password;
	firstname = req.body.firstname;
	name = req.body.name;
	pathAvatar = "Users/" + username + "/" + req.file.filename;
	exec('mkdir Users/' + username, function(error, stdout, stderr) {
		exec('mv Users/' + req.file.filename + ' Users/' + username, function(error, stdout, stderr) {});
	});
	session
		.run( "CREATE (a:Person {username: \'"+username +"\', password: \'"+
		password+"\'," + "firstname: \'"+firstname+"\', name: \'"+name +
		"\', pathAvatar: \'"+pathAvatar+"\'})")
		.then( function( result ) {
    		session.close();
    		driver.close();
  		});
});

// Récupération des infos de l'utilisateur
app.get('/Info_user/:username/', function(req,res){
	session
		.run("MATCH (a:Person {username: \'" + req.params.username +"\'}) RETURN a.firstname " +
		"AS firstname, a.name AS name")
		.then(function(result){
    		var resultStats = [];
			resultStats.push({
				"firstname" : result.records[0].get("firstname"),
				"name" : result.records[0].get("name")
			});
			res.setHeader('Content-Type', 'application/json');
            res.send(JSON.stringify(resultStats, null, 3));
    	});
});

// Récupération de l'avatar de l'utilisateur
app.get('/Avatar_user/:username/', function(req,res){
	session
		.run("MATCH (a:Person {username: \'" + req.params.username +"\'}) RETURN a.pathAvatar AS pathAvatar")
		.then(function(result){
    		var img = fs.readFileSync(result.records[0].get("pathAvatar"));
    	 	res.writeHead(200, {'Content-Type': 'image/bitmap' });
    		res.end(img, 'binary');
    	});
});


var port = 3000;
app.listen( port, function(){ console.log('listening on port '+port); } );