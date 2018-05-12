function main () {
    var obj = db.get("obj") || {};
    var runCount = db.get("runCount") || 0;
    obj.name = "zhangs" + (new Date()).valueOf();
    obj.address = "sichuan chengdu";
    db.put('obj', obj);
    print(obj.name);
    db.put('name', 'trust');

    for(var i =0; i < 10; i++) {
        runCount++;
    }

    db.put('runCount', runCount);
}