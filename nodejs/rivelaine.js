/*
 * Find web fragments in web pages
 */

const app           = require("./lib/js/app.js").app;
const config        = require("./lib/js/conf.js").config;
const isUrl         = require('is-url');
const fs            = require('fs');
const {getFragment} = require("./fragment.js");
const http          = require("http");
const {staticDom}   = require('fathom-web/utils');
const afterLoad     = require('after-load');
const _             = require('underscore');
const async         = require('async');
const cluster       = require('cluster');

const argv = require('yargs')
    .demandOption(['mode','type'])
    .choices('mode', ['server','script'])
    .choices('type', ['url','file','dom'])
    .default('mode','server')
    .default('type','url')
    .argv;

function getDom(s,end,type,addon=false) {

    if (type == "url") {

        // Source is an Url

        afterLoad(s,function(html){
            return getFragment(staticDom(html),end,addon)
        });        

    } else if (type == "file") {

        // Source is a Html file

        fs.readFile(argv.source, 'utf8', function (err, html) {
            if (err)
                return console.log(err);
            return getFragment(staticDom(html),end);
        });        


    } else if (type == "dom") {

        // Source is a Dom tree

        return getFragment(staticDom(s),end)

    } else {

        // Default

        return getFragment(staticDom(s),end)

    } 

}

if (cluster.isMaster) {

    console.log('== Master ' + process.pid + ' is up ==');

    if (argv.mode != "script") {

        // fork workers
      
        for (var i = 0; i < 2; i++) {

            cluster.fork()
        }

    } else {

        // node rivelaine.js --mode=script --source="http://qlobbe.net/bio.html" --type=url

        // Run rivelaine as a node script ( for test & debug )

        if (argv.source == null) {
            console.log("Please add a --source=... param !");
            process.exit(1)
        }

        if (argv.type == null) {
            console.log("Please add a --type=... param !");
            process.exit(1)
        }

        async.waterfall([
            function(end) {
                getDom(argv.source,end,type);
            }
        ], function (err, result) {
            if (err)
                res.status(500).send({ error: 'Shit happens !' });

            console.log(result)
        });

    }

}

if (cluster.isWorker && argv.mode != "script") {

    // Run rivelaine as a node server listening to rest and socket requests

    app.configure();
    
    http.createServer(app).listen(config.rest.port, function() {
        console.log("Server listening on port " + config.rest.port);
    });

    app.get('/getFragment', function(req, res){

       console.log("Got a request");

        // http://localhost:2200/getFragment?type=url&source=http%3A%2F%2Fqlobbe.net%2Fbio.html

        if (req.query.source == null) {
            res.status(500).send({ error: 'Please add a source=... param !' });
            return;
        }

        var addon = false;
        if (req.query.addon != null) {
            addon = (req.query.addon == 'true');
        }

        var type = "url";
        if (req.query.type != null) {
            type = req.query.type;
        }        

        console.log("And everithing's ok");

        console.log(req.query.source);

        async.waterfall([
            function(end) {
                getDom(req.query.source,end,type,addon)
            }
        ], function (err, result) {
            if (err)
                res.status(500).send({ error: 'Shit happens !' });

            res.send(result);
        });
    });
}
