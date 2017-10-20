function isDescendant(parent, child) {
     var node = child.parentNode;
     while (node != null) {
         if (node == parent) {
             return true;
         }
         node = node.parentNode;
     }
     return false;
}


function tagFrag(dom) {
	var allNodes = dom.getElementsByTagName("*");
	var doneNodes = [];
	for (var i=0, max=allNodes.length; i < max; i++) {
		var nodeType = allNodes[i].getAttribute("nodetype");
		var isFrag   = allNodes[i].getAttribute("isFrag");
		if (nodeType != null && isFrag != null && (isFrag == "true")) {
			switch(nodeType) {
			    case "other":
			    	var ok = true;
					for (var j=0; j < doneNodes.length; j ++) {
						if (doneNodes[j].contains(allNodes[i])) {
							ok = false;
						}	
					}
					if (ok)
			       		allNodes[i].style.border="3px solid red";
			        break;
			    case "title":
			        allNodes[i].style.border="3px solid black";
			        break;
			    case "date":
			        allNodes[i].style.border="3px solid green";			        
			        break;
			    case "author":
			        allNodes[i].style.border="3px solid blue";			        
			        break;			        
				case "text":
					var ok = true;
					for (var j=0; j < doneNodes.length; j ++) {
						if (doneNodes[j].contains(allNodes[i])) {
							ok = false;
						}	
					}
					if (ok)
			        	allNodes[i].style.border="3px solid red";		        
			        break;			       
				case "expLocal":
			        allNodes[i].style.border="3px solid yellow";			        
			        break;			       
				case "expGlobal":
			        allNodes[i].style.border="3px solid yellow";			        
			        break;				        
			}
			doneNodes.push(allNodes[i]);	
		}	
	}
}

self.port.on("frag", function(fragments){

	var newDom = JSON.parse(fragments).dom;

	document.documentElement.innerHTML = newDom;

	tagFrag(document)
});