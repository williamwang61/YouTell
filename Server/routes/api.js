//Dependencies
var express = require('express');
var router = express.Router();

//Models
var News = require('../models/news');

//Routes
News.methods(['get','post','delete','put']);
News.register(router, '/news');

//Export
module.exports = router;