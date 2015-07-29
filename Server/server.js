#!/bin/env node

var express    = require('express');
var fs         = require('fs');
var mongoose   = require('mongoose');
var bodyParser = require('body-parser');
var restRouter = require('./routes/api');

var Application = function() {

    //  Scope.
    var self = this;

    /**
     *  Initializes the server.
     */
    self.initialize = function() {
        self.setupEnvVariables();
        self.populateCache();
        self.setupTerminationHandlers();
        self.setupRoutes();
        self.connectMongo();
    };

    /**
     *  Start the server.
     */
    self.start = function() {
        self.app.listen(self.port, self.ip, function() {
            console.log('%s: Node server started on %s:%d ...',
                        Date(Date.now() ), self.ip, self.port);
        });
    };

    /**
     *  Obtain environment variables.
     */
    self.setupEnvVariables = function() {
        self.ip        = process.env.OPENSHIFT_NODEJS_IP;
        self.port      = process.env.OPENSHIFT_NODEJS_PORT || 8080;
        self.dbConnection = process.env.OPENSHIFT_MONGODB_DB_URL;

        if (typeof self.ip === "undefined") {
            console.warn('Undefined environment variable: OPENSHIFT_NODEJS_IP, using 127.0.0.1');
            self.ip = "127.0.0.1";
        };
        if (typeof self.dbConnection === "undefined") {
            console.warn('Undefined environment variable: OPENSHIFT_MONGODB_DB_URL, using mongodb://localhost:27017/');
            self.dbConnection = "mongodb://localhost:27017/";
        };

        self.dbConnection = self.dbConnection + "youtell"; //add database name to the connection string
    };


    /**
     *  Populate the cache.
     */
    self.populateCache = function() {
        if (typeof self.zcache === "undefined") {
            self.zcache = { 'index.html': '' }
        }

        //  Local cache for static content.
        self.zcache['index.html'] = fs.readFileSync('./index.html');
    };


    /**
     *  Retrieve entry (content) from cache.
     */
    self.cache_get = function(key) { return self.zcache[key]; };

    /**
     *  Setup termination handlers (for exit and a list of signals).
     */
    self.setupTerminationHandlers = function(){
        //  Process on exit and signals.
        process.on('exit', function() { self.terminateServer(); });

        // Removed 'SIGPIPE' from the list - bugz 852598.
        ['SIGHUP', 'SIGINT', 'SIGQUIT', 'SIGILL', 'SIGTRAP', 'SIGABRT',
         'SIGBUS', 'SIGFPE', 'SIGUSR1', 'SIGSEGV', 'SIGUSR2', 'SIGTERM'
        ].forEach(function(element, index, array) {
            process.on(element, function() { self.terminateServer(element); });
        });
    };

    /**
     *  terminator === the termination handler
     *  Terminate server on receipt of the specified signal.
     */
    self.terminateServer = function(sig){
        if (typeof sig === "string") {
           console.log(
                '%s: Received %s - terminating sample app ...',
                Date(Date.now()), 
                sig);
           process.exit(1);
        }
        console.log('%s: Node server stopped.', Date(Date.now()) );
    };


    /**
     *  Create the routes and register the handlers.
     */
    self.setupRoutes = function() {
        self.app = express();
        self.app.use(bodyParser.urlencoded({extended:true}));
        self.app.use(bodyParser.json());
        self.app.set("json spaces", 3);

        self.app.get('/', function(req, res) {
                res.setHeader('Content-Type', 'text/html');
                res.send(self.cache_get('index.html'));
            });
        self.app.use('/api', restRouter);
        self.app.use(function(err, req, res, next) {
            console.error(err.stack);
            res.status(500).send(err.message);
        });
    };

    /**
     *  Connect to Mongo database.
     */
    self.connectMongo = function(){
        mongoose.connect(self.dbConnection);
    }
};  

/**
 *  main():  Main code.
 */
var app = new Application();
app.initialize();
app.start();

