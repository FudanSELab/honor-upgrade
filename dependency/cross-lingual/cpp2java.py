import util
import logging
from util import UNKNOWN
from tree_sitter import Node
from common import string_node_to_string
from project_cpp import File, Function
from dependency import Calling, Callee, Location, Func, Func_Type

def getEnclosingFunction(node: Node, file: File) -> Function | None:
    cur_node = node
    while cur_node is not None:
        if cur_node.type == "function_definition":
            return Function(cur_node, file)
        cur_node = cur_node.parent
    return None

def getCaller(file: File, callee_name: str) -> Function | None:
    captures = file.search_function_call(callee_name)
    if captures is None:
        return None
    for c in captures:
        if c[1] == "call":
            return getEnclosingFunction(c[0], file)

def search_identifier_string(file: File, func: Function, identifier: str):
    # static const char *outClassPathName = "com/android/gallery3d/jpegstream/JPEGOutputStream";
    # #define CLASS_NAME "benchmarks/MicroNative/java/NativeMethods"
    query = f'''
    (declaration 
            (init_declarator 
                (pointer_declarator 
                (identifier)@id
                (#eq? @id "{identifier}")
            ) 
            (string_literal (string_content)@string))
    )
    (declaration 
            (init_declarator 
                (array_declarator 
                (identifier)@id
                (#eq? @id "{identifier}")
            ) 
            (string_literal (string_content)@string))
    )
    (declaration 
            (init_declarator 
                (identifier)@id
                (#eq? @id "{identifier}")
            (string_literal (string_content)@string))
    )
    '''
    query_marco = f'''
    (preproc_def 
        (identifier)@id
        (#eq? @id "{identifier}")
        (preproc_arg)@string
    )
    '''
    result = func.parser.query(query)
    if len(result) == 0:
        result = file.parser.query(query)
        if len(result) == 0:
            result = file.parser.query(query_marco)
            for r in result:
                if r[1] != "string":
                    continue
                return r[0].text.decode().replace('"', "").strip()
    if len(result) == 0:
        return UNKNOWN
    for r in result:
        if r[1] != "string":
            continue
        return r[0].text.decode()
    logging.debug(f"[{file.path}] search_identifier_string fail: {identifier}")
    return UNKNOWN

def search_identifier_ScopedLocalRef(file: File, func: Function, identifier: str) -> str:
    # ScopedLocalRef<jclass> clazz(env, env->FindClass("java/nio/ByteBuffer"));
    query = f'''
    (init_declarator
      declarator: (identifier)@id
      (#eq? @id "{identifier}")
      value: (argument_list
        (call_expression
          arguments: (argument_list
          	(string_literal (string_content)@class)
          )
        )
      )
    )
    '''
    result = func.parser.query(query)
    if len(result) == 0:
        result = file.parser.query(query)
    if len(result) == 0:
        return UNKNOWN
    for r in result:
        if r[1] != "class":
            continue
        return r[0].text.decode()
    return UNKNOWN

def search_identifier_NewGlobalRef(file: File, func: Function, identifier: str) -> str:
    # class_gnssMeasurementsEventBuilder = (jclass)env->NewGlobalRef(gnssMeasurementsEventBuilderClass);
    # gExternalCaptureStateTrackerClassId = (jclass) env->NewGlobalRef(FindClassOrDie(env, CLASSNAME));
    query = f'''
    (assignment_expression
        left: (identifier)@id (#eq? @id "{identifier}")
        right: (cast_expression
            value: (call_expression
                function: (field_expression
                    field: (field_identifier)@fid (#eq? @fid "NewGlobalRef")
                )
                arguments: (argument_list)@args
            )
        )
    )
    '''
    result = func.parser.query(query)
    if len(result) == 0:
        result = file.parser.query(query)
    if len(result) == 0:
        return UNKNOWN
    for r in result:
        if r[1] != "args":
            continue
        arg = r[0].named_children[0]
        if arg.type == "string_literal" or arg.type == "concatenated_string":
            return string_node_to_string(file, arg)
        elif arg.type == "identifier":
            return search_identifier_FindClass(file, func, arg.text.decode())
        elif arg.type == "call_expression":
            FindClassOrDie_node = arg.child_by_field_name("function")
            if FindClassOrDie_node is None or FindClassOrDie_node.text.decode() != "FindClassOrDie":
                return UNKNOWN
            args = arg.child_by_field_name("arguments")
            clz = args.named_children[1] if args is not None else None
            if clz is None:
                return UNKNOWN
            if clz.type == "string_literal" or clz.type == "concatenated_string":
                return string_node_to_string(file, clz)
            elif clz.type == "identifier":
                return search_identifier_string(file, func, clz.text.decode())
        else:
            return UNKNOWN
    logging.debug(f"[{file.path}] search_identifier_NewGlobalRef fail: {identifier}")
    return UNKNOWN

def search_identifier_MakeGlobalRefOrDie(file: File, func: Function, identifier: str) -> str:
    # gAppFuseClass = MakeGlobalRefOrDie(env, FindClassOrDie(env, CLASS_NAME));
    # gAppFuseOnMount = GetMethodIDOrDie(env, gAppFuseClass, "onMount", "(I)V");
    query = f'''
    (
        (identifier)@id
        (#eq? @id "{identifier}")
        (call_expression
            (identifier)@fid
            (#eq? @fid "MakeGlobalRefOrDie")
            (argument_list (_)@fcls .)
        )
    )
    '''
    result = func.parser.query(query)
    if len(result) == 0:
        result = file.parser.query(query)
    if len(result) == 0:
        return UNKNOWN
    for r in result:
        if r[1] != "fcls":
            continue
        fcls = r[0]
        if fcls.type == "string_literal" or fcls.type == "concatenated_string":
            return string_node_to_string(file, fcls)
        elif fcls.type == "identifier":
            return search_identifier_FindClass(file, func, fcls.text.decode())
        elif fcls.type == "call_expression":
            fcls_node = fcls.child_by_field_name("function")
            if fcls_node is None:
                return UNKNOWN
            if fcls_node.text.decode() == "FindClassOrDie":
                args = fcls.child_by_field_name("arguments")
                if args is None:
                    return UNKNOWN
                id = args.named_children[1]
                if id.type == "string_literal" or id.type == "concatenated_string":
                    return string_node_to_string(file, id)
                elif id.type == "identifier":
                    return search_identifier_string(file, func, id.text.decode())
                else:
                    return UNKNOWN
            else:
                return UNKNOWN
        else:
            return UNKNOWN
    logging.debug(f"[{file.path}] search_identifier_MakeGlobalRefOrDie fail: {identifier}")
    return UNKNOWN

def search_identifier_FindClassOrDie(file: File, func: Function, identifier: str) -> str:
    # jclass listenerClass = FindClassOrDie(env, listenerClassName);
    query = f'''
    (
        (identifier)@id
        (#eq? @id "{identifier}")
        (call_expression
            (identifier)@fid
            (#eq? @fid "FindClassOrDie")
            (argument_list) @arg
        )
    )
    '''
    result = func.parser.query(query)
    if len(result) == 0:
        result = file.parser.query(query)
    if len(result) == 0:
        return UNKNOWN

    for r in result:
        if r[1] != "arg":
            continue
        arg = r[0].named_children[1]
        if arg.type == "string_literal" or arg.type == "concatenated_string":
            return string_node_to_string(file, arg)
        elif arg.type == "identifier":
            return search_identifier_string(file, func, arg.text.decode())
        else:
            return UNKNOWN
    logging.debug(f"[{file.path}] search_identifier_FindClassOrDie fail: {identifier}")
    return UNKNOWN

def search_identifier_FIND_CLASS(file: File, func: Function, identifier: str) -> str:
    query = f"""
    (call_expression
        function: (identifier)@func (#eq? @func "FIND_CLASS")
        arguments: (argument_list
            (identifier)@id (#eq? @id "{identifier}")
            (string_literal (string_content)@cls)
        )
    )
    (call_expression
        function: (identifier)@func (#eq? @func "FIND_CLASS")
        arguments: (argument_list
            (field_expression
                field: (field_identifier)@fid (#eq? @fid "{identifier}")
            )
            (string_literal (string_content)@cls)
        )
    )
    """
    result = func.parser.query(query)

    if len(result) == 0:
        result = file.parser.query(query)
    if len(result) == 0:
        return UNKNOWN
    for r in result:
        if r[1] != "cls":
            continue
        return r[0].text.decode()
    logging.debug(f"[{file.path}] search_identifier_FIND_CLASS fail: {identifier}")
    return UNKNOWN

def search_identifier_FindClass(file: File, func: Function, identifier: str) -> str:
    # jclass clazz = env->FindClass("android/drm/DrmManagerClient");
    # jclass clazz = env->FindClass(id);
    # clazz = env->FindClass("android/drm/DrmManagerClient");
    # clazz = env->FindClass(id);
    # gFrameSequenceClassInfo.clazz = env->FindClass(JNI_PACKAGE "/FrameSequence");
    query = '''
    (
        (identifier)@id
        (#eq? @id "{0}")
        (call_expression
            (field_expression (field_identifier)@fid)
            (#eq? @fid "FindClass")
            (argument_list) @arg)
    )
    (
        (field_expression)@id
        (#eq? @id "{0}")
        (call_expression
            (field_expression (field_identifier)@fid)
            (#eq? @fid "FindClass")
            (argument_list) @arg)
    )
    '''
    result = func.parser.query(query.format(identifier))
    if len(result) == 0:
        return UNKNOWN
    for r in result:
        if r[1] != "arg":
            continue
        arg = r[0].named_children[0]
        if arg.type == "string_literal" or arg.type == "concatenated_string":
            return string_node_to_string(file, arg)
        elif arg.type == "identifier":
            return search_identifier_string(file, func, arg.text.decode())
        else:
            return UNKNOWN
    logging.debug(f"[{file.path}] search_identifier_FindClass fail: {identifier}")
    return UNKNOWN

def gen_cpp2java_callee(cpp_func: Function, cpp_file: File, call_node: Node, java_clazz: str, java_method_name: str, java_method_sig: str) -> Callee:
    type_java = util.convert_jni_signature(java_method_sig)
    if type_java == "()":
        args_type_java = []
    else:
        args_type_java = type_java.replace("(", "").replace(")", "").split(",")
    return_type_java = util.convert_jni_return_type(java_method_sig)
    func_type_java = Func_Type(args_type_java, return_type_java, java_clazz.replace("/", "."))
    location_java = Location("", 0, 0)
    callee_java = Func("java", java_method_name, func_type_java, location_java)
    call_start_line = cpp_func.node.start_point[0] + 1 + call_node.start_point[0] + 1
    location_call = Location(cpp_file.path, call_start_line, call_node.start_point[1] + 1)
    callees = Callee(callee_java, location_call)
    return callees

def search_getmethod_call(func: Function, identifier: str, call_node: Node, func_class_pair: dict[str, str]):
    file = func.file
    query_str = """
    (init_declarator
        declarator: (identifier)@id (#eq? @id "{0}")
        value: (call_expression
            function: (field_expression
                field: (field_identifier)@func (#match? @func "Get.*MethodID")
            )
            arguments: (argument_list)@args
        )
    )
    (assignment_expression
        (identifier)@id
        (#eq? @id "{0}")
        (call_expression 
            (field_expression 
                (field_identifier)@fid
                (#match? @fid "GetMethodID.*")
            )
            (argument_list)@args
        )
    )
    (assignment_expression
        (field_expression 
            (field_identifier)@id
            (#eq? @id "{0}")
        )
        (call_expression 
            (field_expression 
                (field_identifier)@fid
                (#match? @fid "Get.*MethodID.*")
            )
            (argument_list)@args
        )
    )
   	(assignment_expression
        (identifier)@id
        (#eq? @id "{0}")
        (call_expression 
            (identifier)@fid
            (#match? @fid "Get.*MethodID.*")
            (argument_list)@args
        )
    )
   	(assignment_expression
		(field_expression 
            (field_identifier)@id
            (#eq? @id "{0}")
        )
        (call_expression 
            (identifier)@fid
            (#match? @fid "Get.*MethodID.*")
            (argument_list)@args
        )
    )
    (assignment_expression
        (identifier)@id
        (#eq? @id "{0}")
        (call_expression 
        	function: (field_expression
            	field: (field_identifier)@fid
                (#match? @fid "Get.*MethodID.*")
            )
            arguments: (argument_list)@args
        )
    )
    (call_expression
    	function: (identifier)@call (#eq? @call "GET_METHOD_ID")
        arguments: (argument_list
        	.(identifier)@id (#eq? @id "{0}")
        )@args
    )
    (call_expression
    	function: (identifier)@call (#eq? @call "GET_METHOD_ID")
        arguments: (argument_list
        	.(field_expression
            	field: (field_identifier)@id (#eq? @id "{0}")
            )
        )@args
    )
    (call_expression
    	function: (identifier)@call (#eq? @call "GET_STATIC_METHOD_ID")
        arguments: (argument_list
        	.(identifier)@id (#eq? @id "{0}")
        )@args
    )
    (call_expression
    	function: (identifier)@call (#eq? @call "GET_STATIC_METHOD_ID")
        arguments: (argument_list
        	.(field_expression
            	field: (field_identifier)@id (#eq? @id "{0}")
            )
        )@args
    )
    (init_declarator
        declarator: (identifier)@id (#eq? @id "{0}")
        value: (call_expression
            function: (identifier)@func (#eq? @func "FindMethod")
            arguments: (argument_list)@args
        )
    )
   	(assignment_expression
        (identifier)@id
        (#eq? @id "{0}")
        (call_expression 
            (identifier)@fid
            (#match? @fid "GetMethod")
            (argument_list)@args
        )
    )
    """
    query = query_str.format(identifier)
    captures = func.parser.query(query)
    if len(captures) == 0:
        captures = file.parser.query(query)
    if len(captures) == 0 and identifier.startswith("method_"):
        identifier = identifier[7:]
        query = query_str.format(identifier)
        captures = func.parser.query(query)
        if len(captures) == 0:
            captures = file.parser.query(query)
    if len(captures) == 0:
        query = f"""
        (assignment_expression
            left: (identifier)@left
            (#eq? @left "{identifier}")
            right: (identifier)@right
        )
        (assignment_expression
            left: (field_expression
                field: (field_identifier)@left
                (#eq? @left "{identifier}")
            )
            right: (identifier)@right
        )
        (init_declarator
        	declarator: (identifier)@left
            (#eq? @left "{identifier}")
            value: (field_expression
            	field: (field_identifier)@right
            )
        )
        """
        captures = func.parser.query(query)
        if len(captures) == 0:
            captures = file.parser.query(query)
        if len(captures) != 0:
            for c in captures:
                if c[1] == "right":
                    identifier = c[0].text.decode()
                    break
            query = query_str.format(identifier)
            captures = func.parser.query(query)
            if len(captures) == 0:
                captures = file.parser.query(query)
    if len(captures) == 0:
        logging.debug(
            f"[{file.path}] no get method id call found, function: {func.name}, identifier: {identifier}")
        return
    args_nodes = [c[0] for c in captures if c[1] == "args"]
    # sort close to call node
    args_nodes = sorted(args_nodes, key=lambda x: abs(
        x.start_point[0] - call_node.start_point[0]) if x.start_point[0] <= call_node.start_point[0] else 1000000)
    return args_nodes

def parse_GetMethodID_args(func: Function, args_node: Node, call_node: Node, func_class_pair: dict[str, str]):
    file = func.file
    enclosingFunction = getEnclosingFunction(args_node, file)
    if enclosingFunction is None:
        enclosingFunction = func

    method_name = UNKNOWN
    pre_slibing = args_node.prev_named_sibling
    if len(args_node.named_children) == 3 and pre_slibing is not None and pre_slibing.type == "identifier" and pre_slibing.text.decode() == "GET_METHOD_ID":
        clazz = args_node.named_children[1]
        method_name = args_node.named_children[0].text.decode()
        signature = args_node.named_children[2]
    elif len(args_node.named_children) == 3:
        clazz = args_node.named_children[0]
        method_name = args_node.named_children[1]
        signature = args_node.named_children[2]
    elif len(args_node.named_children) == 4:
        clazz = args_node.named_children[1]
        method_name = args_node.named_children[2]
        signature = args_node.named_children[3]
    else:
        logging.debug(
            f"[{file.path}] args number not 3 or 4, functifile=on: {enclosingFunction.name}")
        return

    # get class name
    clazz_string = UNKNOWN
    if clazz.type == "string_literal" or clazz.type == "concatenated_string":
        clazz_string = string_node_to_string(file, clazz)
    elif clazz.type == "identifier" or clazz.type == "field_expression" or clazz.type == "call_expression":
        if clazz.type == "field_expression":
            clazz = clazz.child_by_field_name("field")
        elif clazz.type == "call_expression":
            clazz_func = clazz.child_by_field_name("function")
            if clazz_func is not None and clazz_func.type == "field_expression":
                clazz = clazz_func.child_by_field_name("argument")
        if clazz is None:
            logging.debug(
                f"[{file.path}] clazz is None, function: {enclosingFunction.name}")
            return
        clazz_string = search_identifier_FindClass(file, enclosingFunction, clazz.text.decode())
        if clazz_string == UNKNOWN:
            clazz_string = search_identifier_FindClassOrDie(file, enclosingFunction, clazz.text.decode())
        if clazz_string == UNKNOWN:
            clazz_string = search_identifier_MakeGlobalRefOrDie(file, enclosingFunction, clazz.text.decode())
        if clazz_string == UNKNOWN:
            clazz_string = search_identifier_FIND_CLASS(file, enclosingFunction, clazz.text.decode())
        if clazz_string == UNKNOWN:
            clazz_string = search_identifier_NewGlobalRef(file, enclosingFunction, clazz.text.decode())
    else:
        # logging.debug(f"[{file.path}] clazz type not string or identifier, function: {enclosingFunction.name}")
        pass
    if clazz_string == UNKNOWN:
        key = file.path + ":" + enclosingFunction.name
        if key in func_class_pair:
            clazz_string = func_class_pair[key]
        else:
            caller = getCaller(file, enclosingFunction.name)
            if caller is not None:
                key = caller.file.path + ":" + caller.name
                if key in func_class_pair:
                    clazz_string = func_class_pair[key]
                else:
                    clazz_string = UNKNOWN
    if clazz_string == UNKNOWN:
        logging.debug(
            f"[{file.path}] clazz_string is UNKNOWN, enclosingFunction: {enclosingFunction.name}")
        return
    if " " in clazz_string:
        clazz_string = clazz_string.split(" ")[1].strip("/")

    # get method name
    if isinstance(method_name, str):
        method_name = method_name
    elif method_name.type == "string_literal" or method_name.type == "concatenated_string":
        method_name = string_node_to_string(file, method_name)
    elif method_name.type == "identifier":
        method_name = search_identifier_string(file, enclosingFunction, method_name.text.decode())
    else:
        logging.debug(
            f"[{file.path}] method_name type not string or identifier, function: {enclosingFunction.name}")
        return
    if method_name == UNKNOWN:
        logging.debug(
            f"[{file.path}] java method_name is UNKNOWN, enclosingFunction: {enclosingFunction.name}")
        return

    # get method signature
    if signature.type == "string_literal" or signature.type == "concatenated_string":
        signature = string_node_to_string(file, signature)
    elif signature.type == "identifier":
        signature = search_identifier_string(file, enclosingFunction, signature.text.decode())
    else:
        logging.debug(
            f"[{file.path}] signature type not string or identifier, function: {enclosingFunction.name}")
        return
    return gen_cpp2java_callee(func, file, call_node, clazz_string, method_name, signature)

def search_class_name(func: Function, enclosingFunction: Function, class_node: Node, func_class_pair: dict[str, str]):
    file = func.file
    class_string = UNKNOWN
    target_seatch_node = class_node
    if class_node.type == "string_literal" or class_node.type == "concatenated_string":
        return string_node_to_string(file, class_node)
    elif class_node.type == "identifier":
        target_seatch_node = class_node
    elif class_node.type == "field_expression":
        target_seatch_node = class_node.child_by_field_name("field")
    elif class_node.type == "call_expression":
        function_node = class_node.child_by_field_name("function")
        if function_node is None:
            return
        if function_node.type == "field_expression":
            target_seatch_node = function_node.child_by_field_name("argument")
        else:
            logging.debug(
                f"[{file.path}] class_node is not field_expression, function: {enclosingFunction.name}")
    else:
        logging.debug(
            f"[{file.path}] class_node type is {class_node.type}, function: {enclosingFunction.name}")
        return
    if target_seatch_node is None:
        return
    target_seatch_node_text = target_seatch_node.text.decode()
    class_string = search_identifier_FindClass(file, enclosingFunction, target_seatch_node_text)
    if class_string == UNKNOWN:
        class_string = search_identifier_FindClassOrDie(file, enclosingFunction, target_seatch_node_text)
    if class_string == UNKNOWN:
        class_string = search_identifier_MakeGlobalRefOrDie(file, enclosingFunction, target_seatch_node_text)
    if class_string == UNKNOWN:
        class_string = search_identifier_FIND_CLASS(file, enclosingFunction, target_seatch_node_text)
    if class_string == UNKNOWN:
        class_string = search_identifier_NewGlobalRef(file, enclosingFunction, target_seatch_node_text)
    if class_string == UNKNOWN:
        class_string = search_identifier_ScopedLocalRef(file, enclosingFunction, target_seatch_node_text)

    if class_string == UNKNOWN:
        key = file.path + ":" + enclosingFunction.name
        if key in func_class_pair:
            class_string = func_class_pair[key]
        else:
            caller = getCaller(file, enclosingFunction.name)
            if caller is not None:
                key = caller.file.path + ":" + caller.name
                if key in func_class_pair:
                    class_string = func_class_pair[key]
                else:
                    class_string = UNKNOWN
    if class_string == UNKNOWN:
        logging.debug(
            f"[{file.path}] clazz_string is UNKNOWN, enclosingFunction: {enclosingFunction.name}")
        return
    if " " in class_string:
        class_string = class_string.split(" ")[1].strip("/")
    return class_string

def search_method_name(func: Function, enclosingFunction: Function, method_name: str | Node):
    file = func.file
    if isinstance(method_name, str):
        return method_name
    elif method_name.type == "string_literal" or method_name.type == "concatenated_string":
        return string_node_to_string(file, method_name)
    elif method_name.type == "identifier":
        return search_identifier_string(file, enclosingFunction, method_name.text.decode())
    else:
        logging.debug(
            f"[{file.path}] method_name type is {method_name.type}, function: {enclosingFunction.name}")
        return

def search_method_signature(func: Function, enclosingFunction: Function, signature: Node):
    file = func.file
    if signature.type == "string_literal" or signature.type == "concatenated_string":
        return string_node_to_string(file, signature)
    elif signature.type == "identifier":
        return search_identifier_string(file, enclosingFunction, signature.text.decode())
    else:
        logging.debug(
            f"[{file.path}] method signature type is {signature.type}, function: {enclosingFunction.name}")
        return

def search_callmethod(func: Function):
    query = """
    (call_expression 
        (field_expression 
            (field_identifier)@id
            (#match? @id "^Call.+Method")
        )
        (argument_list)@args
    )@call
    """
    call = func.parser.query(query)
    if len(call) == 0:
        return None
    call_nodes = [c[0] for c in call if c[1] == "call"]
    callid_nodes = [c[0] for c in call if c[1] == "id"]
    args_nodes: list[Node] = [c[0] for c in call if c[1] == "args"]
    return call_nodes, callid_nodes, args_nodes

def handle_callmethod(func: Function, args_node: Node, func_class_pair: dict[str, str]):
    file = func.file
    enclosingFunction = getEnclosingFunction(args_node, file)
    if enclosingFunction is None:
        enclosingFunction = func

    # Check args number
    pre_slibing = args_node.prev_named_sibling
    if len(args_node.named_children) == 3 and pre_slibing is not None and pre_slibing.type == "identifier" and pre_slibing.text.decode() == "GET_METHOD_ID":
        clazz = args_node.named_children[1]
        method_name = args_node.named_children[0].text.decode()
        signature = args_node.named_children[2]
    elif len(args_node.named_children) == 3:
        clazz = args_node.named_children[0]
        method_name = args_node.named_children[1]
        signature = args_node.named_children[2]
    elif len(args_node.named_children) == 4:
        clazz = args_node.named_children[1]
        method_name = args_node.named_children[2]
        signature = args_node.named_children[3]
    else:
        logging.debug(f"[{file.path}] args number not 3 or 4, function: {enclosingFunction.name}")
        return

    # get class name
    class_string = search_class_name(func, enclosingFunction, clazz, func_class_pair)
    if class_string is None or class_string == UNKNOWN:
        return

    # get method name
    method_name = search_method_name(func, enclosingFunction, method_name)
    if method_name is None or method_name == UNKNOWN:
        return

    # get method signature
    signature = search_method_signature(func, enclosingFunction, signature)
    if signature is None or signature == UNKNOWN:
        return

    return class_string, method_name, signature

def cpp2java(func: Function, func_class_pair: dict[str, str]) -> Calling | None:
    file = func.file

    # 定位 CallMethod
    result = search_callmethod(func)
    if result is None:
        return None
    call_nodes, callid_nodes, args_nodes = result

    # 构造 Caller
    func_name = func.name
    func_args_type = func.get_args_type()
    func_return_type = func.get_return_type()
    func_type = Func_Type(func_args_type, func_return_type, "")
    caller_location = Location(file.path, func.node.start_point[0] + 1, func.node.start_point[1] + 1)
    caller = Func("c/c++", func_name, func_type, caller_location)
    calling = Calling(caller, [])

    for call_node, args_node, callid_node in zip(call_nodes, args_nodes, callid_nodes):
        if len(args_node.named_children) < 2:
            logging.debug(f"[{file.path}] CallMethod: args number < 2, function: {func.name}")
            continue

        if callid_node.text.decode() == "CallNonvirtualVoidMethod" and len(args_node.named_children) > 2:
            method_id = args_node.named_children[2]
        method_id = args_node.named_children[1]

        if method_id.type == "identifier":
            method_id_name = method_id.text.decode()
        elif method_id.type == "field_expression":
            field_identifier = method_id.child_by_field_name("field")
            if field_identifier is None:
                logging.debug(f"[{file.path}] CallMethod: None, function: {func.name}")
                continue
            method_id_name = field_identifier.text.decode()
        elif method_id.type == "qualified_identifier":
            name_node = method_id.child_by_field_name("name")
            if name_node is None:
                logging.debug(f"[{file.path}] CallMethod: name_node is None in function: {func.name}")
                continue
            if name_node.type == "qualified_identifier":
                name_node = name_node.child_by_field_name("name")
            if name_node is None:
                logging.debug(f"[{file.path}] CallMethod: name_node is None in function: {func.name}")
                continue
            method_id_name = name_node.text.decode()
        elif method_id.type == "call_expression":
            function_node = method_id.child_by_field_name("function")
            if function_node is None:
                continue
            function_field_node = function_node.child_by_field_name("field")
            if function_field_node is None:
                continue
            call_args_node = method_id.child_by_field_name("arguments")
            if call_args_node is None:
                continue
            if function_field_node.text.decode() == "GetStaticMethodID":
                callee = parse_GetMethodID_args(func, call_args_node, call_node, func_class_pair)
                if callee is None:
                    continue
                calling.callees.append(callee)
            else:
                result = handle_callmethod(func, call_args_node, func_class_pair)
                if result is None:
                    continue
                class_string, method_name, signature = result
                callee = gen_cpp2java_callee(func, file, call_node, class_string, method_name, signature)
                calling.callees.append(callee)
            continue
        else:
            logging.debug(
                f"[{file.path}] CallMethod: method_id type is {method_id.type} in function: {func.name}")
            continue

        getmethod_args_nodes = search_getmethod_call(func, method_id_name, call_node, func_class_pair)
        if getmethod_args_nodes is None:
            continue
        callee = None
        for args_node in getmethod_args_nodes:
            result = handle_callmethod(func, args_node, func_class_pair)
            if result is None:
                continue
            class_string, method_name, signature = result
            callee = gen_cpp2java_callee(func, file, call_node, class_string, method_name, signature)
            break
        if callee is None:
            continue
        calling.callees.append(callee)

    if len(calling.callees) == 0:
        return None
    return calling
