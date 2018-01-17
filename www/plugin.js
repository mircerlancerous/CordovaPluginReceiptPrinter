Plugin.receiptPrinter = {
	open: function(callback, onFail){
		cordova.exec(callback, onFail, 'SGT88iVPrinterPlugin', 'connect', []);
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
	}
};