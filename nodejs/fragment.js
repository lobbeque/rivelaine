/*
 * Using fathom to extract web fragments from a dom tree
 */

const {rule, ruleset, dom, out, conserveScore, note, props, score, type} = require('fathom-web');
const {linkDensity,inlineLength,inlineTextLength,domSort,walk}           = require('fathom-web/utils');

const {clusters}     = require('fathom-web/clusters.js');
const {Fnode}        = require('fathom-web/fnode.js');
const _              = require('underscore');
const {pattern}      = require("./utils.js");
const {fragDistance,fragClusters} = require("./cluster.js");

function getTitle(domTree) {
    // Get the title of a given web page
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
    // Get a string such as tag name + id + class name of a given element
    return ele.tagName + ' ' + ele.id + ' ' + ele.className;
}

function getChildren(n,rest) {
    // Get all the children of a given fNode in a list of fNodes
    return _.filter(rest, function(r){
        return n.element.contains(r.element);
    });
}

function isMajorNode(n) {
    // Is this node an interesting node ?
    return n.element.getAttribute("nodeType") == 'date' || n.element.getAttribute("nodeType") == 'author' || n.element.getAttribute("nodeType") == 'title';
}

function hasNodeType(eleId,type) {
    // Does an eleId match a nodeType pattern such as Title ? 
    return eleId.match(pattern[type])
}

function hasHref(ele) {
    // Does an element have any inline href ?
    return ele.href != null;
}

function hasMajorChildren(n,rest) {
    // Does a node have interesting children such as title, author ... ?
    var children = getChildren(n,rest);
    return _.reduce( 
        _.map(children, function(c){
            return isMajorNode(c);
        }), function(mem,is) {
            return mem || is;
        },false
    );
}

function HasTextChildren(n) {
    // Does a node have text like children ?
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

function scoreByText(n,a=2) {
    // Increase node score by inline text content
    var length = inlineTextLength(n.element) * a;
    if (length == 'undefined' || isNaN(length) )
        length = 1
    return { score:length, note: {inlineLength: length} };
}

function scoreByNodeType(n,a=1) {
    // Increase node score by node type
    var ele = n.element;
    var id = getEleId(ele)
    var score = n.scoreFor('node');
    var nodeType  = 'other'; 
    if (hasNodeType(id,'title')) {
        score = score + a;
        nodeType = 'title';
    } else if (hasNodeType(id,'author')) {
        score = score + a;
        nodeType = 'author';
    } else if (hasNodeType(id,'date')) {
        score = score + a;
        nodeType = 'date';
    } else if (hasNodeType(id,'text')) {
        score = score + a;
        nodeType = 'text';
    }
    ele.setAttribute("nodeType",nodeType);
    return {score : score, type: 'cntNode', note : id};    
}

function scoreByDensity(n,a=1.5) {
    // Increase node score by linkDensity
    var score = n.scoreFor('node')
    var density = linkDensity(n,n.noteFor('node').inlineLength)
    if (isNaN(density))
        density = 1
    return {score : score + (1 - density) * a};
}

function scoreByCrap(n,a=-1000) {
    // Decrease node score if it's a crapy node
    var id = getEleId(n.element)
    var score = 1
    if (id.match(pattern['crap']) != null)
        score = a;
    return {score : score};
}

function getFragment(domTree, end, addon=false) {

    var splitLength = 15;

    // Extract web fragments from nodes

    // A web fragment is a set of nodes. Nodes can have intersting contents (cntNode) or be interesting expression mediums (expNode => expLocal & expGlobal)

    const rules = ruleset(
        
        /*
         * Get content nodes
         */

        // 1. select element by tag
        // 2. score by textInline
        // 3. score by crap
        // 4. score by href
        // 5. score by density
        // 6. score by node type

        rule(dom(pattern['cntNode']),props(scoreByText).type('node')),
        rule(type('node'),props(scoreByCrap).typeIn('node')),
        rule(type('node'),score(n => hasHref(n.element) ? n.scoreFor('node') + 1 : n.scoreFor('node'))),
        rule(type('node'),props(scoreByDensity).typeIn('node')),
        rule(type('node'),props(scoreByNodeType).typeIn('cntNode')),
        rule(type('cntNode'),out('cntNodes').allThrough(domSort)),
        
        /*
         * Get expression nodes
         */
        
         // 1. select element by class

        rule(dom(pattern['expNode']),type('expNode')),
        rule(type('expNode'),out('expNodes').allThrough(domSort))      
    );

    var facts    = rules.against(domTree);
    var cntNodes = facts.get('cntNodes');
    var expNodes = facts.get('expNodes');

    // Remove clones & init all expNodes as expLocal
    expNodes = _.filter(expNodes, function(n){
        n.element.setAttribute("nodeType","expLocal");
        var i = expNodes.indexOf(n)
        return !(expNodes[i + 1] != null && n.element.contains(expNodes[i + 1].element));
    });  

    var nodeCache; 

    // Clean cntNode list
    cntNodes = _.filter(cntNodes, function(n){

        var keep = true;
        var i = cntNodes.indexOf(n)

        // Save parent & remove cloned children
        if (nodeCache != null && nodeCache.element.contains(n.element)) {
            keep = false;
        }

        // Remove frag < 0
        if (keep && n.scoreFor('cntNode') < 0) {
            keep = false;
            if (cntNodes[i + 1] != null && !hasMajorChildren(n,cntNodes.slice(i + 1)))
                nodeCache = n;
        }
            
        // Remove wrap up frag
        if (keep && cntNodes[i +1] != null && n.element.textContent == cntNodes[i +1].element.textContent) {
            keep = false;
        }
            
        // Save or remove parent based on nodeType rules
        if (keep && cntNodes[i + 1] != null && n.element.contains(cntNodes[i + 1].element)) {

            if (!isMajorNode(n) && hasMajorChildren(n,cntNodes.slice(i + 1)) && !HasTextChildren(n)) {
                // Remove parent and may keep children
                keep = false;
            } else {
                // Save parent     
                nodeCache = n;
            }
        }

        // Link an expNode if possible & remove it from the list
        if (keep) {
            expNodes = _.filter(expNodes,function(e){
                return !n.element.contains(e.element);
            })
        }
            
        return keep;
    });

    // Switch the remaining expNodes from expLocal to expGlobal
    _.each(expNodes,function(n){
        n.element.setAttribute("nodeType","expGlobal");
    });

    // Cluster nodes into fragments
    var fragments = fragClusters(cntNodes, splitLength, fragDistance);

    function nodeToSolrFields(ele,res,child = false) {
        
        var nodeType = ele.getAttribute("nodeType") 
        var eleId    = getEleId(ele);
        
        if (nodeType != null) {
        
            res.type.push(nodeType);
            if (nodeType == "author")
                res.author.push(ele.textContent.replace(pattern["cleanAuthor"],"").trim());
            if (nodeType == "date")
                res.date.push(ele.textContent.trim());
        
        } else if (hasNodeType(eleId,'author')) {
        
            res.type.push('author');
            res.author.push(ele.textContent.replace(pattern["cleanAuthor"],"").trim());
        
        } else if (hasNodeType(eleId,'date')) {
        
            res.type.push('date');
            res.date.push(ele.textContent.trim());
        
        } else if (hasNodeType(eleId,'title')) {
        
            res.type.push('title');
        
        }
        
        if (ele.href != null)
            res.href.push(ele.href);
        
        if (!child) {
            res.node.push(ele.outerHTML);
            res.nodeId.push(eleId);
            if (nodeType != "expLocal" && nodeType != "expGlobal") {
                res.text  += " " + ele.textContent.trim();
                res.ratio += ele.outerHTML.length;
            }
        }
        return res;
    }

    if (addon) {

        // if rivelaine is used as an addon return the full dom

        _.each(fragments,function(f){
            _.each(f,function(n){
                n.element.setAttribute("isFrag","true");
                var children = n.element.children;
                for (var i = 0; i < children.length; i++) {
                    children[i].setAttribute("isFrag","true");
                }
            })
        })

        _.each(expNodes,function(e){
            e.element.setAttribute("isFrag","true");
        })

        var res = {'dom' : domTree.documentElement.innerHTML};

    } else {

        var res = _.map(fragments, function(f){

            var offset = fragments.indexOf(f);

            f = domSort(f);

            _.each(expNodes,function(e){
                f.push(e);
            })
            
            var tmp = {
                'type'   : [],
                'author' : [],
                'date'   : [],
                'href'   : [],
                'ratio'  :  0,
                'node'   : [],
                'nodeId' : [],
                'offset' :  0,
                'text'   : "",
            }

            tmp.offset = offset;

            _.each(f,function(n){
                tmp = nodeToSolrFields(n.element,tmp,false);
                if (n.element.getAttribute("nodeType") != "expLocal" && n.element.getAttribute("nodeType") != "expGlobal") {
                    var children = n.element.children;
                    for (var i = 0; i < children.length; ++i) {
                        tmp = nodeToSolrFields(children[i],tmp,true);
                    }
                }
            })

            tmp.ratio = tmp.ratio / domTree.documentElement.outerHTML.length; 

            // Add expression nodes 

            return tmp;
        });

    }

    if (end != null) {
        end(null,res); 
    } else {
        return res;
    }   

}

module.exports = {
    getFragment
};