var count = 0;

function add(x, y) {
    var result = x + y;
    print(result);
    return result;
}

function getCurrentBlockHeight() {
    count++;
    print("count: " + count);
    print(context);
    var height = ctx.getBlockSerivce().getCurrentBlockHeight();
    print('current block height: ' + height);
    return height;
}

function verify(height, other) {
    var currentHeight = ctx.getBlockSerivce().getCurrentBlockHeight();
    var name = ctx.sayHello('jack');
    print(name + " " + ctx.getAdmin().name);
    count++;
    print("count: " + count);
    var amount = db.getInt("amount");
    print('=========' + amount);
    db.put("amount", amount + 100);
    db.put("map", {id: 1, name: 'jack'})
    print(db.getInt("amount"));
    print(db.get("map").name);

    var data = {id: 22, address: 'kdkdkd'};
    data.list = new Array();
    db.put('data', data);
    data.address = "address";

    var list = [1, 2, 3];
    list.push(33);
    db.put("list", list);
    list = db.get('list');
    list.forEach(function(index, v) { print(v); });
    print(list);

    return height == currentHeight;
}