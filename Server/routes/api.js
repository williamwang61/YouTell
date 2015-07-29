//Dependencies
var express = require('express');
var router = express.Router();
var request = require('request');

//Models
var post = require('../models/post');

//Routes
post.methods(['get','post','put', 'delete']);

post.before('post', validate_user)
	.before('put', validate_user)
	.before('delete', validate_user);

function validate_user(req, res, next) {
	var accessToken = req.get("AccessToken");
	var userId = req.get("UserId");

	request("https://graph.facebook.com/me?fields=id&access_token=" + accessToken, 
		function (error, response, body) {
			if (error || response.statusCode != 200){
				return next(new Error("Failed to get Facebook authentication result."));
			}

			var result = JSON.parse(body);
			if(result.id == userId) next();
			else next(new Error("Unmatched user id and access token."));
		});
}

post.register(router, '/posts');

//Export
module.exports = router;