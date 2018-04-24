function verify() {
    var action = ctx.getAction();
	var actionType = action.utxoActionType;
	var issueActionType = ctx.getUTXOActionType('ISSUE');
	var normalActionType = ctx.getUTXOActionType('NORMAL');
	var destructionActionType = ctx.getUTXOActionType('DESTRUCTION');

	//issue utxo  action
	if(actionType == issueActionType){
		return true;
	}

    //notmal utxo  action
    if(actionType == normalActionType){
	  if(action.inputList.length == 0 || action.getOutputList().length == 0){
	 	return false;
	  }
	var utxoList = ctx.queryUTXOList(action.inputList);
	var inputsAmount = 0;
	var outputsAmount = 0;
	utxoList.forEach(function (input) {inputsAmount += input.getState().amount;});
	action.getOutputList().forEach(function (input) {outputsAmount += input.getState().amount;});
	return inputsAmount == outputsAmount;
	}

	 //destruction utxo  action
	if(actionType == destructionActionType){

		return true;
	}

	return false;
}