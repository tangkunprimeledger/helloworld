import json

def is_num(string):
    try:
        if string.replace('.', '', 1).isdigit():
            return True
    except Exception, e:
        return False

def convert_num(string):
    try:
        num = int(string)
        return num
    except Exception, e:
        try:
            num = float(string)
            return num
        except Exception, e:
            raise Exception("string is not digit")



file_handler = file("monitor_file.txt")
monitor_json_list = []
for line in file_handler:
    item_json = {}
    line_data_list = line.strip().split()
    item_name = line_data_list[0].strip()
    method = line_data_list[1].strip()
    compare = line_data_list[2].strip()
    threshold = line_data_list[3].strip()
    item_json["target_name"] = item_name
    item_json["method"] = method
    item_json["compare"] = compare
    if is_num(threshold):
        item_json["threshold"] = convert_num(threshold)
    else:
        item_json["threshold"] = threshold
    monitor_json_list.append(item_json)

file_json_handler = open("trust.json", "w")
file_json_handler.write(json.dumps(monitor_json_list, indent=4))