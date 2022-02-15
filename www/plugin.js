Plugin.receiptPrinter = {
	open: function(callback, onFail){
		cordova.exec(callback, onFail, 'ReceiptPrinter', 'connect', []);
	},

	close: function(callback, onFail){
		cordova.exec(callback, onFail, 'ReceiptPrinter', 'disconnect', []);
	},

	isOpen: function(callback, onFail){
		cordova.exec(callback, onFail, 'ReceiptPrinter', 'isConnected', []);
	},
	
	printText: function(callback, onFail, value){
		cordova.exec(callback, onFail, 'ReceiptPrinter', 'printText', [value]);
	},
	
	command: function(callback, onFail, value){
		cordova.exec(callback, onFail, 'ReceiptPrinter', 'command', [value]);
	},

	printBarcode: function(callback, onFail, type, value){
		cordova.exec(callback, onFail, 'ReceiptPrinter', 'printBarcode', [type, value]);
	},

	cutPaper: function(callback, onFail){
		cordova.exec(callback, onFail, 'ReceiptPrinter', 'cutPaper', []);
	},

	openCashBox: function(callback, onFail){
		cordova.exec(callback, onFail, 'ReceiptPrinter', 'openCashBox', []);
	}
};
