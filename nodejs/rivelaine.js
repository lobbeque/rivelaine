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

const argv = require('yargs')
    .demandOption(['mode'])
    .choices('mode', ['server','script'])
    .default('mode','server')
    .argv;

function getDom(s,end) {

    if (isUrl(s)) {

        console.log(s)

        // Source is an Url

        afterLoad(s,function(html){
            return getFragment(staticDom(html),end)
        });        

    } else if (fs.existsSync(s)) {

        // Source is a Html file

        fs.readFile(argv.source, 'utf8', function (err, html) {
            if (err)
                return console.log(err);
            return getFragment(staticDom(html),end);
        });        


    } else {

        // Source is a Dom tree

        return getFragment(staticDom(s),end)

    } 

}

if (argv.mode != "script") {
    
    // Run rivelaine as a node server listening to rest and socket requests

    app.configure();
    
    http.createServer(app).listen(config.rest.port, function() {
        console.log("Server listening on port " + config.rest.port);
    });

    app.get('/getFragment', function(req, res){

        // http://localhost:2200/getFragment?source=http%3A%2F%2Fqlobbe.net%2Fbio.html

        if (req.query.source == null) {
            res.status(500).send({ error: 'Please add a source=... param !' });
            return;
        }

        async.waterfall([
            function(end) {
                getDom(decodeURI(req.query.source),end)
            }
        ], function (err, result) {
            if (err)
                res.status(500).send({ error: 'Shit happens !' });

            res.send(result);
        });
    });

} else {

    // node rivelaine.js --mode=script --source="http://qlobbe.net/bio.html"

    // Run rivelaine as a node script ( for test & debug )

    if (argv.source == null) {
        console.log("Please add a --source=... param !");
        process.exit(1)
    }

    async.waterfall([
        function(end) {
            getDom(argv.source,end);
        }
    ], function (err, result) {
        if (err)
            res.status(500).send({ error: 'Shit happens !' });

        console.log(result)
    });    

}