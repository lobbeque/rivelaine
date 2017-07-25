
const {isDomElement, isWhitespace, min} = require('fathom-web/utils.js');
const _ = require('underscore');
const {flatten} = require('wu');

var pattern = {
    'textInline'  : /A|B|BIG|I|SMALL|TT|ABBR|ACRONYM|CITE|EM|STRONG|SCRIPT|SPAN|LABEL|TEXTAREA/i
}

function getNodeId(n) {
    return n.element.tagName + ' ' + n.element.id + ' ' + n.element.className;
}

function numStrides(left, right) {
    let num = 0;

    // Walk right from left node until we hit the right node or run out:
    let sibling = left;
    let shouldContinue = sibling && sibling !== right;
    while (shouldContinue) {
        sibling = sibling.nextSibling;
        if ((shouldContinue = sibling && sibling !== right) &&
            !isWhitespace(sibling)) {
            num += 1;
        }
    }
    if (sibling !== right) {  // Don't double-punish if left and right are siblings.
        // Walk left from right node:
        sibling = right;
        while (sibling) {
            sibling = sibling.previousSibling;
            if (sibling && !isWhitespace(sibling)) {
                num += 1;
            }
        }
    }
    return num;
}

// hack based on "distance" https://github.com/mozilla/fathom/blob/master/clusters.js

function fragDistance(fnodeA,
                  fnodeB,
                  fnodes,
                  {differentDepthCost = 2,
                   differentTagCost = 2,
                   sameTagCost = 1,
                   strideCost = 1,
                   additionalCost = (fnodeA, fnodeB) => 0} = {}) {

    var debug = false;

    if (fnodeA === fnodeB) {
        return 0;
    }

    const elementA = isDomElement(fnodeA) ? fnodeA : fnodeA.element;
    const elementB = isDomElement(fnodeB) ? fnodeB : fnodeB.element;

    // Stacks that go from the common ancestor all the way to A and B:
    const aAncestors = [elementA];
    const bAncestors = [elementB];

    let aAncestor = elementA;
    let bAncestor = elementB;

    // console.log(elementA.tagName + ' ' + elementA.id + ' ' + elementA.className + " === " + elementB.tagName + ' ' + elementB.id + ' ' + elementB.className)

    // Ascend to common parent, stacking them up for later reference:
    while (!aAncestor.contains(elementB)) {  // Note: an element does contain() itself.
        aAncestor = aAncestor.parentNode;
        aAncestors.push(aAncestor); //aAncestors = [a, b]. aAncestor = b // if a is outer: no loop here; aAncestors = [a]. aAncestor = a.
    }

    // In compareDocumentPosition()'s opinion, inside implies after. Basically,
    // before and after pertain to opening tags.
    const comparison = elementA.compareDocumentPosition(elementB);

    // If either contains the other, abort. We'd either return a misleading
    // number or else walk upward right out of the document while trying to
    // make the ancestor stack.
    if (comparison & (elementA.DOCUMENT_POSITION_CONTAINS | elementA.DOCUMENT_POSITION_CONTAINED_BY)) {
        return Number.MAX_VALUE;
    }
    // Make an ancestor stack for the right node too so we can walk
    // efficiently down to it:
    do {
        bAncestor = bAncestor.parentNode;  // Assumes we've early-returned above if A === B. This walks upward from the outer node and up out of the tree. It STARTS OUT with aAncestor === bAncestor!
        bAncestors.push(bAncestor);
    } while (bAncestor !== aAncestor);

    // Figure out which node is left and which is right, so we can follow
    // sibling links in the appropriate directions when looking for stride
    // nodes:
    let left = aAncestors;
    let right = bAncestors;
    let up = fnodeA;
    let down = fnodeB;
    let cost = 0;
    if (comparison & elementA.DOCUMENT_POSITION_FOLLOWING) {
        // A is before, so it could contain the other node. What did I mean to do if one contained the other?
        left = aAncestors;
        right = bAncestors;
        up = fnodeA;
    	down = fnodeB;
    } else if (comparison & elementA.DOCUMENT_POSITION_PRECEDING) {
        // A is after, so it might be contained by the other node.
        left = bAncestors;
        right = aAncestors;
        up = fnodeB;
    	down = fnodeA;
    }

    /*
     * Rivelaine's cost
     */

    function isBreakLine(n,next) {
    	if (next == undefined)
    		return false;

    	if (n.tagName.match(pattern["textInline"]) && !next.tagName.match(pattern["textInline"]))
    		return true;

    	return false;
    }

    function upDownCost(up,down,upDepth,downDepth) {
    	var tmp = 0;
    	var upClass = up.element.getAttribute("nodeType");
    	var downClass = down.element.getAttribute("nodeType");

    	if (upClass == 'date' || upClass == 'author' || upClass == 'title') {
            if (downClass == 'text' || downClass == 'other') {
                tmp += 0;
            } else if (upDepth <= downDepth) {
    			tmp += 0;
    		} else {
    			tmp += 50;
    		}
    	} else if (upClass == 'text' && ( downClass == 'text' || downClass == 'title' ) && upDepth <= downDepth) {
    		tmp += 0
    	} else {
    		tmp += 50;
    	}

    	return tmp;

    }

    if (fnodeA.element.textContent.match(/furtif/i) && debug)
        console.log("1 : " + cost)

    cost += elementA.style.backgroundColor != elementB.style.backgroundColor ? 20 : 0; 

    idxUp = fnodes.indexOf(up) 
    idxDown = fnodes.indexOf(down);

    if (idxUp + 1 == idxDown) {
        cost += upDownCost(up,down,left.length,right.length);

        if (fnodeA.element.textContent.match(/furtif/i) && debug)
            console.log("2 : " + cost)

    } else {
        cost += 10 * (idxDown - idxUp)

        if (fnodeA.element.textContent.match(/furtif/i) && debug)
            console.log("3 : " + cost)
    }

    // add gravitationnal force

    // Descend to both nodes in parallel, discounting the traversal
    // cost iff the nodes we hit look similar, implying the nodes dwell
    // within similar structures.
    while (left.length || right.length) {
        const l = left.pop();
        const r = right.pop();
        if (l === undefined || r === undefined) {
            // Punishment for being at different depths: same as ordinary
            // dissimilarity punishment for now
            cost += differentDepthCost;

            if (fnodeA.element.textContent.match(/furtif/i) && debug)
                console.log("4 : " + cost)

        } else {
            cost += l.tagName === r.tagName ? sameTagCost : differentTagCost;

            if (fnodeA.element.textContent.match(/furtif/i) && debug)
                console.log("5 : " + cost)
        }

        if (r != undefined) {

        	// based on VIPS algorithm

        	cost += r.tagName == "HR" ? 10 : 0;

            if (fnodeA.element.textContent.match(/furtif/i) && debug)
                console.log("6 : " + cost)

        	cost += r.tagName == "BR" ? 10  : 0; 
        	
            if (fnodeA.element.textContent.match(/furtif/i) && debug)
                console.log("7 : " + cost)

            cost += isBreakLine(r,_.first(right)) ? 10 : 0;

            if (fnodeA.element.textContent.match(/furtif/i) && debug)
                console.log("8 : " + cost)
        }

        // Optimization: strides might be a good dimension to eliminate.
        if (strideCost !== 0) {
            cost += numStrides(l, r) * strideCost;

            if (fnodeA.element.textContent.match(/furtif/i) && debug)
                console.log("9 : " + cost)            
        }
    }

    if (fnodeA.element.textContent.match(/furtif/i) && debug)
        console.log("10 : " + cost)  

    if (getNodeId(fnodeB).match(/com-auteur/i) && fnodeB.element.textContent.match(/chrisado/i)) {
        if (getNodeId(fnodeA).match(/com-content/i) && fnodeA.element.textContent.match(/furtif/i)) {
            console.log(getNodeId(fnodeB) + " & " + getNodeId(fnodeA) + " => " + cost)
            console.log(fnodeA.element.textContent)
        }
    }
    

    return cost + additionalCost(fnodeA, fnodeB);
}

/** A lower-triangular matrix of inter-cluster distances */
class DistanceMatrix {
    /**
     * @arg distance {function} Some notion of distance between 2 given nodes
     */
    constructor(elements, distance) {
        // A sparse adjacency matrix:
        // {A => {},
        //  B => {A => 4},
        //  C => {A => 4, B => 4},
        //  D => {A => 4, B => 4, C => 4}
        //  E => {A => 4, B => 4, C => 4, D => 4}}
        //
        // A, B, etc. are arrays of [arrays of arrays of...] nodes, each
        // array being a cluster. In this way, they not only accumulate a
        // cluster but retain the steps along the way.
        //
        // This is an efficient data structure in terms of CPU and memory, in
        // that we don't have to slide a lot of memory around when we delete a
        // row or column from the middle of the matrix while merging. Of
        // course, we lose some practical efficiency by using hash tables, and
        // maps in particular are slow in their early implementations.
        this._matrix = new Map();

        // Convert elements to clusters:
        const clusters = elements.map(el => [el]);

        // Init matrix:
        for (let outerCluster of clusters) {
            const innerMap = new Map();
            for (let innerCluster of this._matrix.keys()) {
                innerMap.set(innerCluster, distance(outerCluster[0],
                                                    innerCluster[0],elements));
            }
            this._matrix.set(outerCluster, innerMap);
        }
        this._numClusters = clusters.length;
    }

    // Return (distance, a: clusterA, b: clusterB) of closest-together clusters.
    // Replace this to change linkage criterion.
    closest() {
        const self = this;

        if (this._numClusters < 2) {
            throw new Error('There must be at least 2 clusters in order to return the closest() ones.');
        }

        // Return the distances between every pair of clusters.
        function clustersAndDistances() {
            const ret = [];
            for (let [outerKey, row] of self._matrix.entries()) {
                for (let [innerKey, storedDistance] of row.entries()) {
                    ret.push({a: outerKey, b: innerKey, distance: storedDistance});
                }
            }
            return ret;
        }
        // Optimizing this by inlining the loop and writing it less
        // functionally doesn't help:
        return min(clustersAndDistances(), x => x.distance);
    }

    // Look up the distance between 2 clusters in me. Try the lookup in the
    // other direction if the first one falls in the nonexistent half of the
    // triangle.
    _cachedDistance(clusterA, clusterB) {
        let ret = this._matrix.get(clusterA).get(clusterB);
        if (ret === undefined) {
            ret = this._matrix.get(clusterB).get(clusterA);
        }
        return ret;
    }

    // Merge two clusters.
    merge(clusterA, clusterB) {
        // An example showing how rows merge:
        //  A: {}
        //  B: {A: 1}
        //  C: {A: 4, B: 4},
        //  D: {A: 4, B: 4, C: 4}
        //  E: {A: 4, B: 4, C: 2, D: 4}}
        //
        // Step 2:
        //  C: {}
        //  D: {C: 4}
        //  E: {C: 2, D: 4}}
        //  AB: {C: 4, D: 4, E: 4}
        //
        // Step 3:
        //  D:  {}
        //  AB: {D: 4}
        //  CE: {D: 4, AB: 4}

        // Construct new row, finding min distances from either subcluster of
        // the new cluster to old clusters.
        //
        // There will be no repetition in the matrix because, after all,
        // nothing pointed to this new cluster before it existed.
        const newRow = new Map();
        for (let outerKey of this._matrix.keys()) {
            if (outerKey !== clusterA && outerKey !== clusterB) {
                newRow.set(outerKey, Math.min(this._cachedDistance(clusterA, outerKey),
                                              this._cachedDistance(clusterB, outerKey)));
            }
        }

        // Delete the rows of the clusters we're merging.
        this._matrix.delete(clusterA);
        this._matrix.delete(clusterB);

        // Remove inner refs to the clusters we're merging.
        for (let inner of this._matrix.values()) {
            inner.delete(clusterA);
            inner.delete(clusterB);
        }

        // Attach new row.
        this._matrix.set([clusterA, clusterB], newRow);

        // There is a net decrease of 1 cluster:
        this._numClusters -= 1;
    }

    numClusters() {
        return this._numClusters;
    }

    // Return an Array of nodes for each cluster in me.
    clusters() {
        // TODO: Can't get wu.map to work here. Don't know why.
        return Array.from(this._matrix.keys()).map(e => Array.from(flatten(false, e)));
    }
}

/**
 * Partition the given nodes into one or more clusters by position in the DOM
 * tree.
 *
 * This implements an agglomerative clustering. It uses single linkage, since
 * we're talking about adjacency here more than Euclidean proximity: the
 * clusters we're talking about in the DOM will tend to be adjacent, not
 * overlapping. We haven't tried other linkage criteria yet.
 *
 * In a later release, we may consider score or notes.
 *
 * @arg {Fnode[]|Node[]} fnodes :term:`fnodes<fnode>` or DOM nodes to group
 *     into clusters
 * @arg {number} splittingDistance The closest-nodes :func:`distance` beyond
 *     which we will not attempt to unify 2 clusters. Make this larger to make
 *     larger clusters.
 * @arg getDistance {function} A function that returns some notion of numerical
 *    distance between 2 nodes. Default: :func:`distance`
 * @return {Array} An Array of Arrays, with each Array containing all the
 *     nodes in one cluster. Note that neither the clusters nor the nodes are
 *     in any particular order. You may find :func:`domSort` helpful to remedy
 *     the latter.
 */
function fragClusters(fnodes, splittingDistance, getDistance = distance) {
    const matrix = new DistanceMatrix(fnodes, getDistance);
    let closest;

    // console.log(matrix)

    while (matrix.numClusters() > 1 && (closest = matrix.closest()).distance < splittingDistance) {
        matrix.merge(closest.a, closest.b);
    }

    return matrix.clusters();
}


module.exports = {
    fragDistance,
    fragClusters
};