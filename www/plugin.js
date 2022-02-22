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

	printBarcode: function(callback, onFail, code_type, value){
		try{
			let code = parseInt(code_type);
			code_type = code;
		}
		catch(e){
			if(typeof(code_type) !== 'string'){
				onFail("invalid barcode type: must be int or string code");
				return;
			}
			switch(code_type.toUpperCase()){
				case "CODE128":
					code_type = 73;
					break;
				case "CODE39":
					code_type = 4;
					break;
				case "QRCODE":
				default:
					code_type = 102;
					break;
			}
		}
		cordova.exec(callback, onFail, 'ReceiptPrinter', 'printBarcode', [code_type, value]);
	},
	
	printImage: function(callback, onFail, b64Image){
		//remove url component if it exists
		var data = "base64,";
		var pos = b64Image.indexOf(data);
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
