//Dependencies
var restful = require('node-restful');
var mongoose = restful.mongoose;

//Schema
var postSchema = new mongoose.Schema({
	author: String,
	author_id: String,
	title: String,
	content: String,
	createdAt: Date,
	location:{
		name: String,
		country: String
	}
});

//Return model
module.exports = restful.model('Post', postSchema);