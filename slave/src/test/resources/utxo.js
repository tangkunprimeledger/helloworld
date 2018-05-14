function verify() {
	var action = ctx.getAction();
	var actionType = action.utxoActionType;
	var issueActionType = ctx.getUTXOActionType('ISSUE');
	var normalActionType = ctx.getUTXOActionType('NORMAL');
	var destructionActionType = ctx.getUTXOActionType('DESTRUCTION');

	//issue utxo  action
	if (actionType == issueActionType) {
		return true;
	}

	//notmal utxo  action
	if (actionType == normalActionType) {
		if (action.inputList.length == 0 || action.getOutputList().length == 0) {
			return false;
		}
		var utxoList = ctx.queryUTXOList(action.inputList) || [];

		if (utxoList.length === 0) {
			return false;
		}

		var inputsAmount = 0;
		var outputsAmount = 0;

		for(var i =0; i < utxoList.length; i++) {
			var input = utxoList[i];
			inputsAmount += input.getState().amount;

			var outputs = action.getOutputList() || [];
			for(var j = 0; j < outputs.length; j++) {
				var output = outputs[j];
				outputsAmount += output.getState().amount;

				if (input.getState().billId != output.getState().billId) {
					return false;
				}
				if (input.getState().finalPayerId != output.getState().finalPayerId) {
					return false;
				}
				if (input.getState().dueDate != output.getState().dueDate) {
					return false;
				}
			}
		}
		return inputsAmount == outputsAmount;
	}

	//destruction utxo  action
	return actionType == destructionActionType;
}