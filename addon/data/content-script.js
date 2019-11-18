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
		var isHead   = allNodes[i].getAttribute("headFrag");
		if (nodeType != null && isFrag != null && (isFrag == "true")) {

			if (isHead != null) {
				allNodes[i].style.border="4px solid red";
			} else {
				switch(nodeType) {
				    case "other":
				    	var ok = true;
						for (var j=0; j < doneNodes.length; j ++) {
							if (doneNodes[j].contains(allNodes[i])) {
								ok = false;
							}	
						}
						if (ok)
				       		allNodes[i].style.border="2px dotted red";
				        break;
				    case "title":
				        allNodes[i].style.border="2px dotted black";
				        break;
				    case "date":
				        allNodes[i].style.border="2px dotted green";			        
				        break;
				    case "author":
				        allNodes[i].style.border="2px dotted blue";			        
				        break;			        
					case "text":
						var ok = true;
						for (var j=0; j < doneNodes.length; j ++) {
							if (doneNodes[j].contains(allNodes[i])) {
								ok = false;
							}	
						}
						if (ok)
				        	allNodes[i].style.border="2px dotted red";		        
				        break;			       
					case "expLocal":
				        allNodes[i].style.border="2px dotted yellow";			        
				        break;			       
					case "expGlobal":
				        allNodes[i].style.border="2px dotted yellow";			        
				        break;				        
				}
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