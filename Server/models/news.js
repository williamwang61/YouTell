//Dependencies
var restful = require('node-restful');
var mongoose = restful.mongoose;

//Schema
var newsSchema = new mongoose.Schema({
	author: String,
	content: String
});

//Return model
module.exports = restful.model('News', newsSchema);