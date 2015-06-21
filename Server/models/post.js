//Dependencies
var restful = require('node-restful');
var mongoose = restful.mongoose;

//Schema
var postSchema = new mongoose.Schema({
	author: String,
	title: String,
	content: String,
	createdAt: Date
});

//Return model
module.exports = restful.model('Post', postSchema);