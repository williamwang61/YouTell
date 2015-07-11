//Dependencies
var express = require('express');
var router = express.Router();

//Models
var post = require('../models/post');

//Routes
post.methods(['get','post','delete','put']);

post.register(router, '/posts');
console.log("haha");
//Export
module.exports = router;