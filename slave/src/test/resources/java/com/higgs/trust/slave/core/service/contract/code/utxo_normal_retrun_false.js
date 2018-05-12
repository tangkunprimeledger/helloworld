function verify() {
    var action = ctx.getAction();
    var issueActionType = ctx.getUTXOActionType('ISSUE');
    var normalActionType = ctx.getUTXOActionType('NORMAL');
    var destructionActionType = ctx.getUTXOActionType('DESTRUCTION');

    return false;
}