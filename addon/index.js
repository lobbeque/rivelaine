var buttons         = require('sdk/ui/button/action');
var tabs            = require("sdk/tabs");
var self            = require("sdk/self");
var Request         = require("sdk/request").Request;
const _             = require('underscore');

// handle new page url
var focusUrl = "";

function onOpen(tab) {
  focusUrl = tab.url;
}

tabs.on('pageshow', onOpen);

// handle main button action
function handleClick(state) {

	if (focusUrl != "") {


		var req = Request({
		  url: "http://localhost:2200/getFragment?addon=true&source=" + focusUrl,
		  onComplete: function (response) {

			var frag = JSON.parse(response.text);

			var worker = tabs.activeTab.attach({
		      contentScriptFile: self.data.url("content-script.js")
		    });

		    worker.port.emit("frag",JSON.stringify(frag));			
		  
		  }
		});

		req.get();

	}

}

var button = buttons.ActionButton({
  id: "fragment-link",
  label: "Fragment the page",
  icon: {
    "16": "./frag-16.png",
    "32": "./frag-32.png",
    "64": "./frag-64.png"
  },
  onClick: handleClick
});
