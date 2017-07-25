
const {rule, ruleset, dom, out, and, atMost, conserveScore, max, note, props, score, type, typeIn} = require('fathom-web');
const {staticDom,linkDensity,inlineLength,inlineTextLength,domSort,bestCluster} = require('fathom-web/utils');
const {clusters}  = require('fathom-web/clusters.js');
const {Fnode}  = require('fathom-web/fnode.js');
const afterLoad   = require('after-load');
const fs          = require('fs');
const _           = require('underscore');
const argv        = require('yargs')
                    .demandOption(['mode','source'])
                    .choices('mode', ['url', 'file', 'html'])
                    .describe('source', 'url | path to html file | html tree')
                    .argv;

const {fragDistance,fragClusters} = require("./lib/js/utils.js");

var pattern = {
    'title'   : /h1|h2|h3|title|titre/i,
    'author'  : /(byline|author|writtenby|p-author|pseudo|avatar|auteur)/i,
    'date'    : /(date|time$)/i,
    'text'    : /(content|annonce|and|article|body|column|main|shadow|discussion|post|forum|comment|commentaire|bloc)/i,
    'crap'    : /(sidebar|transparent|back_all|gpt|interstitielContainerNew|masque_fe_bottom|navbare|menu|playlist|nav|play|login|footer|bare-icon|handle|zapping|bt$|recommended|most|community|ads|nav|category|bar|popular|button|posting|playlist|disqus|extra|similar|preview|header|legends|related|remark|agegate|toolbar|outil|banner|update|combx|footer|foot|menu|modal|rss|head|shoutbox|sidebar|skyscraper|sponsor|ad-break|pagination|pager|popup)/i,
    'expNode' : "[class*=fb-like-box],[class*=repondre],[class*=citer],[class*=follow],[class*=corps_posting],[class*=share-panel],[class*=share],[class*=facebook],[class*=twitter],[class*=googleplus],[class*=blogger],[class*=youtube],[class*=skyblog],[class*=tumblr],[class*=myspace],[class*=reddit],[class*=vkontakte],[class*=odnoklassniki],[class*=pinterest],[class*=linkedin],[class*=livejournal],[class*=hi5],[class*=create-comment],[class*=create-post],[class*=create-message],[class*=like],[class*=dislike],[class*=rss],[class*=reagir],[class*=imprimer],[class*=partage],[class*=ajouter],[class*=print],[class*=react],[class*=email],[class*=save],[class*=sharetool],[class*=signup],[class*=login],[class*=reply],[class*=comment-reply],[class*=commenter]"
}

function getDomFromHtml(html) {
    return staticDom(html)
}

function getDomFromUrl(url) {
    afterLoad(url,function(html){
        return getDomFromHtml(html);
    });
}

function getTitle(domTree) {

    var title = domTree.title;
    var tmp   = title;

    if (tmp.match(/[\|\-]/i)) {
        tmp = title.replace(/(.*)[\|\-] .*/i,'$1');

        if (tmp.split(" ").length < 3)
            tmp = title.replace(/[^\|\-]*[\|\-](.*)/i,'$1')

    } else if (tmp.match(/: /i)) {

        var h12s = domTree.querySelectorAll("h1, h2");

        function isInnerTextEqual(h12s, title, isIt) {
            if (h12s[0] == null) {
                isIt
            } else {
                isInnerTextEqual(_.rest(h12s), title, isIt || (h12[0].textContent == title))        
            }
        }

        if (!isInnerTextEqual(h12s,title,false)) {
            tmp = title.substring(title.lastIndexOf(':') + 1)

            if (tmp.split(" ").length < 3)
                tmp = title.substring(title.indexOf(':') + 1)
            
        }


    } else if (tmp.length > 150 || tmp.length < 15) {

        var h1s = domTree.querySelectorAll("h1");

        if(h1s.length == 1)
            tmp = h1s[0].textContent;
        
    }

    tmp = tmp.trim()
    
    if (tmp.split(" ").length <= 4)
        tmp = title

    return tmp
} 

function getEleId(ele) {
    return ele.tagName + ' ' + ele.id + ' ' + ele.className;
}

function hasnodeType(eleId,t) {
    return eleId.match(pattern[t])
}

function hasHref(ele) {
    return ele.href != null;
}

function scoreByText(n) {
    var length = inlineTextLength(n.element) * 2;
    if (length == 'undefined' || isNaN(length) )
        length = 1
    return { score:length, note: {inlineLength: length} };
}

function scoreBynodeType(n) {
    var ele = n.element;
    var id = getEleId(ele)
    var score = n.scoreFor('node');
    var nodeType  = 'other'; 
    if (hasnodeType(id,'title')) {
        score ++;
        nodeType = 'title';
    } else if (hasnodeType(id,'author')) {
        score ++;
        nodeType = 'author';
    } else if (hasnodeType(id,'date')) {
        score ++;
        nodeType = 'date';
    } else if (hasnodeType(id,'text')) {
        score ++;
        nodeType = 'text';
    }
    ele.setAttribute("nodeType",nodeType);
    return {score : score, type: 'cntNode', note : id};    
}

function scoreByDensity(n) {
    var score = n.scoreFor('node')
    var density = linkDensity(n,n.noteFor('node').inlineLength)
    if (isNaN(density))
        density = 1
    return {score : score + (1 - density) * 1.5};
}

function scoreByCrap(n) {
    var id = getEleId(n.element)
    var score = 1
    if (id.match(pattern['crap']) != null)
        score = -1000;
    return {score : score};
}

function getChildren(n,rest) {
    return _.filter(rest, function(r){
        return n.element.contains(r.element);
    });
}

function hasMajorChildren(n,rest) {
    var children = getChildren(n,rest);
    return _.reduce( 
        _.map(children, function(c){
            return isMajorFrag(c);
        }), function(mem,is) {
            return mem || is;
        },false
    );
}

function isMajorFrag(n) {
    return n.element.getAttribute("nodeType") == 'date' || n.element.getAttribute("nodeType") == 'author' || n.element.getAttribute("nodeType") == 'title';
}

function hasTextNode(n) {
    var child = n.element.firstChild;
    var el = child.nextSibling;
    var children = []
    children.push(child);
    while(el){
        children.push(el);
        el = el.nextSibling;
    }
    return _.reduce(children,function(m,c){
        return m || (c.nodeName == '#text' && c.textContent.length > 50);
    },false)
}


function getFrag(domTree) {

    // Extract fragments from nodes

    const rules = ruleset(
        /*
         * Content nodes
         */
        // select element by tag & avoid crap class
        rule(dom('p,div,li,code,blockquote,pre,h1,h2,h3,h4,h5,h6,b,i,adress,#text'),props(scoreByText).type('node')),
        // - 1000 if crap 
        rule(type('node'),props(scoreByCrap).typeIn('node')),
        // +1 if href
        rule(type('node'),score(n => hasHref(n.element) ? n.scoreFor('node') + 1 : n.scoreFor('node'))),
        // scale it by inverse of link density
        rule(type('node'), props(scoreByDensity).typeIn('node')),
        // +1 if match class & assign class Type 
        rule(type('node'),props(scoreBynodeType).typeIn('cntNode')),
        // return cntNodes
        rule(type('cntNode'),out('cntNodes').allThrough(domSort)),
        /*
         * Expression nodes
         */
        rule(dom(pattern['expNode']),type('expNode')),
        rule(type('expNode'),out('expNodes').allThrough(domSort))      
    );

    var facts  = rules.against(domTree);
    var cntNodes = facts.get('cntNodes');
    var expNodes = facts.get('expNodes');

    // _.each(expNodes, function(n){
    //     console.log("========= ")
    //     console.log(getEleId(n.element))
    //     console.log(n.element.textContent + '\n');
    // }) 

    expNodes = _.filter(expNodes, function(n){
        n.element.setAttribute("fragType","expLocal");
        var i = expNodes.indexOf(n)
        return !(expNodes[i + 1] != null && n.element.contains(expNodes[i + 1].element));
    });    

    // remove cntNodes with score < 0 and avoid clones 

    var savedParent; 

    cntNodes = _.filter(cntNodes, function(n){

        var keep = true;
        var i = cntNodes.indexOf(n)

        // save parent & remove cloned children
        if (savedParent != null && savedParent.element.contains(n.element))
            keep = false;

        // remove frag < 0
        if (keep && n.scoreFor('cntNode') < 0) {
            keep = false;
            if (cntNodes[i + 1] != null && !hasMajorChildren(n,cntNodes.slice(i + 1)))
                savedParent = n;
        }
            
        // remove wrap up frag
        if (keep && cntNodes[i +1] != null && n.element.textContent == cntNodes[i +1].element.textContent)
            keep = false;
            
        // save or remove parent based on nodeType rules
        if (keep && cntNodes[i + 1] != null && n.element.contains(cntNodes[i + 1].element)) {

            if (!isMajorFrag(n) && hasMajorChildren(n,cntNodes.slice(i + 1)) && !hasTextNode(n)) {
                // remove parent and may keep children
                keep = false;
            } else {
                // save parent     
                savedParent = n;
            }
        }


        // attach an expNode if possible
        if (keep) {
            n.element.setAttribute("fragType","cnt");
            expNodes = _.filter(expNodes,function(e){
                return !n.element.contains(e.element);
            })
        }
            
        return keep;
    });


    // switch from local expNodes to global expNodes
    _.each(expNodes,function(n){
        n.element.setAttribute("fragType","expGlobal");
    })

    var fragments = fragClusters(cntNodes, 15, fragDistance);


    // faire un beau web service qui dépote réfléchir à un format de sortie pour le search

    // réfléchir aux points de fuite 

    function printEle(ele) {
        if (ele.attributes != null)
            ele.getAttribute("fragType") == "expLocal" ? console.log("[ " + ele.textContent + " ]") : console.log(ele.textContent); 
    }

    _.each(fragments, function(f){
        console.log("======================= \n");
        _.each(f, function(n){
            // printEle(n.element);
            var children = n.element.childNodes;
            console.log(n.element.textContent)
            // _.each(children,function(c){
            //     printEle(c);
            // })
        })   
    })

    console.log("end\n");
}

function process(domTree) {
    getFrag(domTree)
    getTitle(domTree)
}


switch(argv.mode) {
    case "url":
        afterLoad(argv.source,function(html){
            process(getDomFromHtml(html));
        }); 
        break;
    case "file":
        fs.readFile(argv.source, 'utf8', function (err, html) {
            if (err)
                return console.log(err);
            process(getDomFromHtml(html));
        });
        break;
    case "html" :
        process(getDomFromHtml(argv.source));
        break;
}