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
		try{
			let code = parseInt(type);
			type = code;
		}
		catch(e){
			switch(type.toUpperCase()){
				case "CODE128":
					type = 73;
					break;
				case "CODE39":
					type = 4;
					break;
				case "QRCODE":
				default:
					type = 102;
					break;
			}
		}
		cordova.exec(callback, onFail, 'ReceiptPrinter', 'printBarcode', [type, value]);
	},
	
	printImage: function(callback, onFail, b64Image){
		//remove url component if it exists
		var data = "base64,";
		var pos = base64url.indexOf(data);
		if(pos >= 0){
			data = b64Image.substring(pos + data.length);
		}
		cordova.exec(callback, onFail, 'ReceiptPrinter', 'printImage', [data]);
	},

	cutPaper: function(callback, onFail){
		cordova.exec(callback, onFail, 'ReceiptPrinter', 'cutPaper', []);
	},

	openCashBox: function(callback, onFail){
		cordova.exec(callback, onFail, 'ReceiptPrinter', 'openCashBox', []);
	}
};
