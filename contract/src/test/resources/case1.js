function isNum(str) {
    var test = str * 1;
    return !isNaN(test);
}

function verify(bizArgs) {
    var num = bizArgs.num;
    return isNum(num);
}

function say(name) {
    print(name);
    return name;
}

function printAge() {
    admin.age = admin.age + 1;
    print(admin.age)
    for (var i = 0; i < 20; i++) {

    }
    println(admin.age);
}

// var result = verify({num: 1});
// print(result);