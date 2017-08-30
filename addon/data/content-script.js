function tagFrag(dom) {
	var allNodes = dom.getElementsByTagName("*");
	for (var i=0, max=allNodes.length; i < max; i++) {
		var nodeType = allNodes[i].getAttribute("nodetype");
		var isFrag   = allNodes[i].getAttribute("isFrag");
		if (nodeType != null && isFrag != null && (isFrag == "true")) {
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

	var newDom = JSON.parse(fragments).dom;

	document.documentElement.innerHTML = newDom;

	tagFrag(document)
});