var express = require('express');

var app = express();

function allowCrossDomain(request, response, next) {

	response.setHeader('Access-Control-Allow-Origin', '*');
	response.setHeader('Access-Control-Allow-Methods', 'GET,POST,OPTIONS');
	response.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, Content-Length, X-Requested-With');

	if ('OPTIONS' == request.method)
		response.send(204);
	else
		next();
};


app.configure = function () {
	app.use(allowCrossDomain);
};


app.sendJSONResponse = function(response, jsonData) {

	response.status(200);
	response.setHeader('Content-Type', 'text/javascript');

	response.end(JSON.stringify(jsonData));
};


app.sendBadRequest = function(response, msg) {

	response.status(400);
	response.setHeader('Content-Length', msg.length);
	response.end(msg);
};


app.sendError = function(response, msg) {

	response.status(500);
	response.setHeader('Content-Length', msg.length);
	response.end(msg);
};


module.exports.app = app;
