var express = require('express');
var bodyParser = require('body-parser');
var mongoose = require('mongoose');

var routes = require('./routes/index');
var lawyers = require('./routes/lawyers');
var users = require('./routes/users');
var interactions = require('./routes/interactions');

var app = express();

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));

var dbPath  = "mongodb://localhost:27017/userapp";
mongoose.connect(dbPath);

app.use('/', routes);
app.use('/lawyers', lawyers);
app.use('/users', users);
app.use('/interactions', interactions);

module.exports = app;
