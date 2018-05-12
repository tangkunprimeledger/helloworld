
var contractName = "hello-contract";

function sayHello() {
    print('hello');
}

var methods = {
    sayHello: sayHello,

    add: function(x, y) {
        var result = x + y;
        db.put('lastAddResult', result);
    },

    getContractName: function() {
        return contractName;
    },

    getNumberOfExecution: function() {
        return db.get('numberOfExecution') || 0;
    }
}

function main(method, bizArgs) {
    var numberOfExecution = db.get('numberOfExecution') || 0;
    numberOfExecution = numberOfExecution + 1;

    var result = methods[method].apply(null, bizArgs);

    db.put('numberOfExecution', 1);
    return result;
}