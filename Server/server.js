//Dependencies
var express = require('express');
var mongoose = require('mongoose');
var bodyParser = require('body-parser');

//Mongo
mongoose.connect('mongodb://localhost/posts');

//Express
var app = express();
app.use(bodyParser.urlencoded({extended:true}));
app.use(bodyParser.json());

//Routes
app.use('/api', require('./routes/api'));

//Start
app.listen(3000);
console.log('Api running on port 3000...');