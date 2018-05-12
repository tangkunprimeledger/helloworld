var methods = {
    add: function (x, y) {
        print('arguments');
        print(arguments[0]);
        var result = x + y;
        print(result);
        return result;
    }
}

function main(method, bizArgs) {
    print(bizArgs);
    methods[method].apply(null, bizArgs);
}
