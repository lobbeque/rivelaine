function getEleId(ele) {
    // Get a string such as tag name + id + class name of a given element
    return ele.tagName + ' ' + ele.id + ' ' + ele.className;
}

function replaceNode(dom,html,nodeId) {
	var allNodes = dom.getElementsByTagName("*");
	for (var i=0, max=allNodes.length; i < max; i++) {
		if (allNodes[i]!=null){
			var refId = getEleId(allNodes[i]);
		    if (refId == nodeId) {
		    	var div = document.createElement('div');
				div.innerHTML = html;
				var node = div.firstChild;
				if (allNodes[i].textContent.trim() == node.textContent.trim()) {
					allNodes[i].parentNode.replaceChild(node, allNodes[i]);	
				}
		    }			
		}	
	}
}

function tagFrag(dom) {
	var allNodes = dom.getElementsByTagName("*");
	for (var i=0, max=allNodes.length; i < max; i++) {
		var nodeType = allNodes[i].getAttribute("nodetype");
		if (nodeType != null) {
			switch(nodeType) {
			    case "other":
			        allNodes[i].style.border="3px solid red";
			        break;
			    case "title":
			        allNodes[i].style.border="3px solid red";
			        break;
			    case "date":
			        allNodes[i].style.border="3px solid green";			        
			        break;
			    case "author":
			        allNodes[i].style.border="3px solid blue";			        
			        break;			        
				case "text":
			        allNodes[i].style.border="3px solid red";			        
			        break;			       
				case "expLocal":
			        allNodes[i].style.border="3px solid yellow";			        
			        break;			       
				case "expGlobal":
			        allNodes[i].style.border="3px solid yellow";			        
			        break;				        
			}	
		}	
	}
}

self.port.on("frag", function(fragments){
	JSON.parse(fragments).forEach(function(frag){
		frag.node.forEach(function(node){
			var idx = frag.node.indexOf(node);
			replaceNode(document,node,frag.nodeId[idx]);
		})
	})
	tagFrag(document)
});