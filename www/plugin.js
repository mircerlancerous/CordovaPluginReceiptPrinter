Plugin.receiptPrinter = {
	open: function(callback, onFail, Productid){
		if(typeof(Productid) === 'undefined'){
			cordova.exec(callback, onFail, 'SGT88iVPrinterPlugin', 'connect', []);
		}
		else{
			cordova.exec(callback, onFail, 'SGT88iVPrinterPlugin', 'openByProductId', [Productid]);
		}
	},

	close: function(callback, onFail){
		cordova.exec(callback, onFail, 'SGT88iVPrinterPlugin', 'disconnect', []);
	},

	isOpen: function(callback, onFail){
		cordova.exec(callback, onFail, 'SGT88iVPrinterPlugin', 'isConnected', []);
	},
	
	printText: function(callback, onFail, value){
		cordova.exec(callback, onFail, 'SGT88iVPrinterPlugin', 'printText', [value]);
	},

	printBarcode: function(callback, onFail, type, value){
		cordova.exec(callback, onFail, 'SGT88iVPrinterPlugin', 'printBarcode', [type, value]);
	},

	cutPaper: function(callback, onFail){
		cordova.exec(callback, onFail, 'SGT88iVPrinterPlugin', 'cutPaper', []);
	},

	openCashBox: function(callback, onFail){
		cordova.exec(callback, onFail, 'SGT88iVPrinterPlugin', 'openCashBox', []);
	},

	getUSBDevices: function(callback, onFail){
		var onAction = function(response){
			var parts = JSON.parse(response);
			callback(parts);
		};
		cordova.exec(onAction, onFail, 'SGT88iVPrinterPlugin', 'getUSBDevices', []);
	}
};