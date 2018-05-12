function main(bizArgs) {
    var count = db.get('count') || 0;

    if (count > 20) {
        return;
    }

    if (count % 2 == 0) {
        // 解冻
    }

    db.put('count', count++);
}