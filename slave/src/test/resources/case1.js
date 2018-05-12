
var accountMap = db.get('accountMap') || {};

function getContractName() {
    return "jack";
}

function doProcess(bizArgs) {
    print(bizArgs);
    accountMap[bizArgs.accountNo] = bizArgs.amount;
    db.put('accountMap', accountMap);
    return { id: 1, date: new Date() };
}

// 入口
function main(method, bizArgs) {
    switch (method) {
        case 'getName':
            return getContractName();
        default:
            return doProcess();
    }
}