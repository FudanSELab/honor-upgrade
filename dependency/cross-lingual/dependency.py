class Location:
    def __init__(self, source_file: str, line: int, column: int):
        self.source_file = source_file
        self.line = line
        self.column = column

class Func_Type:
    def __init__(self, args_type: list[str], return_type: str, class_name: str):
        self.args_type = args_type
        self.return_type = return_type
        self.class_name = class_name

class Func:
    def __init__(self, language: str, func_name: str, func_type: Func_Type, location: Location):
        self.language = language  # c/c++ or java
        self.func_name = func_name
        self.func_type = func_type
        self.location = location

class Callee:
    def __init__(self, called_func: Func, call_site: Location):
        self.called_func = called_func
        self.call_site = call_site

class Calling:
    def __init__(self, caller: Func, callees: list[Callee]):
        # self.language = language  # c/c++ or java
        self.caller = caller
        self.callees = callees
