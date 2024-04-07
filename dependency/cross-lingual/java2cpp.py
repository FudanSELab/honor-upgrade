import util
import radar
import logging
import inspect
from util import UNKNOWN
from tree_sitter import Node
from common import string_node_to_string
from dependency import Calling, Callee, Location, Func, Func_Type
from project_cpp import File, Function, JNINativeMethod

def search_identifier_string(file: File, func: Function, identifier: str) -> str:
    # static const char *outClassPathName = "com/android/gallery3d/jpegstream/JPEGOutputStream";
    # #define CLASS_NAME "benchmarks/MicroNative/java/NativeMethods"
    query = '''
    (declaration 
            (init_declarator 
                (pointer_declarator 
                (identifier)@id
                (#eq? @id "{0}")
            ) 
            (string_literal (string_content)@string))
    )
    (declaration 
            (init_declarator 
                (array_declarator 
                (identifier)@id
                (#eq? @id "{0}")
            ) 
            (string_literal (string_content)@string))
    )
    (declaration 
            (init_declarator 
                (identifier)@id
                (#eq? @id "{0}")
            (string_literal (string_content)@string))
    )
    '''
    query_marco = '''
    (preproc_def 
        (identifier)@id
        (#eq? @id "{0}")
        (preproc_arg)@string
    )
    '''
    result = func.parser.query(query.format(identifier))
    if len(result) == 0:
        result = file.parser.query(query.format(identifier))
        if len(result) == 0:
            result = file.parser.query(query_marco.format(identifier))
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
    logging.debug(f"[{file.path}] search_identifier_string: {identifier}")
    return UNKNOWN

def search_identifier_FindClass(file: File, func: Function, identifier: str) -> str:
    # jclass clazz = env->FindClass("android/drm/DrmManagerClient");
    # jclass clazz = env->FindClass(id);
    # clazz = env->FindClass("android/drm/DrmManagerClient");
    # clazz = env->FindClass(id);
    # gFrameSequenceClassInfo.clazz = env->FindClass(JNI_PACKAGE "/FrameSequence");
    # ScopedLocalRef<jclass> cls(env, env->FindClass(kStatsLogClass));
    query = '''
    (
        (identifier)@id
        (#eq? @id "{0}")
        (call_expression
            (field_expression (field_identifier)@fid)
            (#eq? @fid "FindClass")
            (argument_list)@arg
        )
    )
    (
        (field_expression)@id
        (#eq? @id "{0}")
        (call_expression
            (field_expression (field_identifier)@fid)
            (#eq? @fid "FindClass")
            (argument_list)@arg
        )
    )
    (init_declarator
        declarator: (identifier)@id
        (#eq? @id "{0}")
        value: (argument_list
            (call_expression
                function: (field_expression (field_identifier)@fid)
                (#eq? @fid "FindClass")
                arguments: (argument_list)@arg
            )
        )
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
            continue
    logging.debug(f"[{file.path}] search_identifier_FindClass: {identifier}")
    return UNKNOWN

def get_target_string(file: File, func: Function, node: Node) -> str:
    if node.type == "identifier":
        return search_identifier_string(file, func, node.text.decode())
    elif node.type == "call_expression":
        arguments = node.child_by_field_name("arguments")
        if arguments is not None and len(arguments.named_children) != 0:
            arg = arguments.named_children[0]
            if arg.type == "cast_expression":
                arg = arg.child_by_field_name("value")
            elif arg.type == "qualified_identifier":
                arg = arg.child_by_field_name("name")
            if arg is None:
                return UNKNOWN
            cpp_method = string_node_to_string(file, arg)
            return cpp_method
        else:
            cpp_method = UNKNOWN
            logging.debug(f"cpp_method(call_expression): {cpp_method}")
            return cpp_method
    elif node.type == "cast_expression":
        value = node.child_by_field_name("value")
        if value is None:
            return UNKNOWN
        if value.type == "qualified_identifier":
            value = value.child_by_field_name("name")
        if value is None:
            return UNKNOWN
        if value.type == "template_function":
            value = value.child_by_field_name("name")
        if value is not None:
            return value.text.decode()
        return UNKNOWN
    else:
        return string_node_to_string(file, node)

def MAKE_JNI_NATIVE_METHOD(file: File, func: Function, args: Node) -> JNINativeMethod | None:
    # https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/jni/android_util_jar_StrictJarFile.cpp;drc=71c806d9f13939dda9ac9648171bb20849cf96f6;l=150?hl=zh-cn
    if len(args.named_children) != 3:
        return
    java_name = get_target_string(file, func, args.named_children[0])
    signature = get_target_string(file, func, args.named_children[1])
    cpp_method = args.named_children[2].text.decode()
    return JNINativeMethod(java_name, signature, cpp_method)

def NATIVE_METHOD(file: File, func: Function, args: Node) -> JNINativeMethod | None:
    # https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/jni/android_util_jar_StrictJarFile.cpp;drc=71c806d9f13939dda9ac9648171bb20849cf96f6;l=150?hl=zh-cn
    if len(args.named_children) != 3:
        return
    java_name = args.named_children[1].text.decode()
    signature = get_target_string(file, func, args.named_children[2])
    cpp_method = args.named_children[0].text.decode() + "_" + args.named_children[1].text.decode()
    return JNINativeMethod(java_name, signature, cpp_method)

def NATIVE_METHOD_OVERLOAD(file: File, func: Function, args: Node) -> JNINativeMethod | None:
    # https://cs.android.com/android/platform/superproject/main/+/main:libcore/luni/src/main/native/libcore_io_Linux.cpp;drc=d3032ed89a6bd972d86e7a82ab4ae5d6f18bbcc9;l=2773
    if len(args.named_children) != 4:
        return
    java_name = args.named_children[1].text.decode()
    signature = get_target_string(file, func, args.named_children[2])
    cpp_method = args.named_children[0].text.decode(
    ) + "_" + args.named_children[1].text.decode() + args.named_children[3].text.decode()
    return JNINativeMethod(java_name, signature, cpp_method)

def MAKE_JNI_NATIVE_METHOD_AUTOSIG(file: File, func: Function, args: Node) -> JNINativeMethod | None:
    if len(args.named_children) != 2:
        return
    java_name = string_node_to_string(file, args.named_children[0])
    cpp_method = args.named_children[1].text.decode()
    return JNINativeMethod(java_name, "", cpp_method)

def MAKE_AUDIO_SYSTEM_METHOD(file: File, func: Function, args: Node) -> JNINativeMethod | None:
    if len(args.named_children) != 1:
        return
    java_name = args.named_children[0].text.decode()
    cpp_method = "android_media_AudioSystem_" + java_name
    return JNINativeMethod(java_name, "", cpp_method)

def search_maplist_macro(func: Function, identifier: str):
    file = func.file
    # https://cs.android.com/android/platform/superproject/main/+/main:libnativehelper/tests_mts/jni/jni_helper_jni.cpp;drc=c763e30ea06c76fcc99ce21daa66b4db6ded9e3c;l=99?hl=zh-cn
    query = '''
    (declaration 
        (init_declarator 
            (array_declarator 
                (identifier)@id
                (#eq? @id "{0}")
            )
            (initializer_list (call_expression (argument_list)@initializer_list)@call)
        )
    )
    (init_declarator
    	declarator: (identifier)@id
        (#eq? @id "{0}")
        value: (initializer_list
        	(call_expression
            	function: (identifier)
                arguments: (argument_list)@initializer_list
            )@call
        )
    )
    '''
    jni_methods_mapping = []
    result = func.parser.query(query.format(identifier))
    if len(result) == 0:
        result = file.parser.query(query.format(identifier))
    if len(result) == 0:
        return jni_methods_mapping

    macro_list: list[Node] = []
    args_list: list[Node] = []
    for r in result:
        if r[1] == "id":
            continue
        if r[1] == "call":
            macro_list.append(r[0])
        if r[1] == "initializer_list":
            args_list.append(r[0])

    for macro, args in zip(macro_list, args_list):
        macro_name_node = macro.child_by_field_name("function")
        if macro_name_node is None:
            logging.debug(f"[{file.path}] search_maplist_macro: macro_name_node is None")
            continue
        macro_name = macro_name_node.text.decode()
        if macro_name == "MAKE_JNI_NATIVE_METHOD":
            jnimap = MAKE_JNI_NATIVE_METHOD(file, func, args)
        elif macro_name in ["NATIVE_METHOD", "FAST_NATIVE_METHOD", "CRITICAL_NATIVE_METHOD"]:
            jnimap = NATIVE_METHOD(file, func, args)
        elif macro_name == "NATIVE_METHOD_OVERLOAD":
            jnimap = NATIVE_METHOD_OVERLOAD(file, func, args)
        elif macro_name == "MAKE_JNI_NATIVE_METHOD_AUTOSIG":
            jnimap = MAKE_JNI_NATIVE_METHOD_AUTOSIG(file, func, args)
        elif macro_name == "MAKE_AUDIO_SYSTEM_METHOD":
            jnimap = MAKE_AUDIO_SYSTEM_METHOD(file, func, args)
        else:
            logging.debug(f"[{file.path}] search_maplist_macro: {macro_name}")
            continue

        if jnimap is not None:
            jni_methods_mapping.append(jnimap)
    return jni_methods_mapping

def search_maplist(func: Function, identifier: str) -> list[JNINativeMethod]:
    file = func.file
    query = '''
    (declaration 
        (init_declarator 
            (array_declarator 
                (identifier)@id
                (#eq? @id "{0}")
            )
            (initializer_list (initializer_list)@initializer_list)
        )
    )
    (declaration 
        (init_declarator 
            (array_declarator 
                declarator: (qualified_identifier
                	name: (identifier)@id
                    (#eq? @id "{0}")
                )
            )
            (initializer_list (initializer_list)@initializer_list)
        )
    )
    (declaration 
        (init_declarator 
            declarator: (identifier)@id
            (#eq? @id "{0}")
            value: (initializer_list (initializer_list (initializer_list)@initializer_list))
        )
    )
    '''
    jni_methods_mapping = []
    result = func.parser.query(query.format(identifier))
    if len(result) == 0:
        result = file.parser.query(query.format(identifier))
    if len(result) == 0:
        return jni_methods_mapping
    for r in result:
        if r[1] != "initializer_list":
            continue
        args = r[0]
        if len(args.named_children) < 3:
            continue
        java_method = get_target_string(file, func, args.named_children[0])
        java_signature = get_target_string(file, func, args.named_children[1])
        if len(args.named_children) == 4:
            cpp_method = get_target_string(file, func, args.named_children[3])
        else:
            cpp_method = get_target_string(file, func, args.named_children[2])
        jni_methods_mapping.append(JNINativeMethod(java_method, java_signature, cpp_method))
    return jni_methods_mapping

def search_method(file: File, func: Function, identifier: str):
    query = '''
        (function_definition
        	(function_declarator 
            	(identifier)@id
            	(#eq? @id "{0}")
            )
        )@function
        (declaration
          (function_declarator 
              (identifier)@id
              (#eq? @id "{0}")
          )
        )@function
        (function_definition
        	(function_declarator 
            	declarator: (field_identifier)@id
            	(#eq? @id "{0}")
            )
        )@function
		(function_definition
        	(function_declarator 
            	declarator: (qualified_identifier
                	name: (identifier)@id
                    (#eq? @id "{0}")
                )
            )
        )@function
    '''
    result = file.parser.query(query.format(identifier))
    if len(result) == 0:
        method_node = file.search_includes(identifier)
        if method_node is not None:
            return method_node.node, method_node.file
    for r in result:
        if r[1] != "function":
            continue
        return r[0], file

def gen_java2cpp_calling_list(jni_methods_mapping: list[JNINativeMethod], class_name: str, file: File, func: Function) -> list[Calling]:
    calling_list = []
    for method_mapping in jni_methods_mapping:
        # java
        type_java = util.convert_jni_signature(method_mapping.signature)
        if type_java == "()":
            args_type_java = []
        else:
            args_type_java = type_java.replace("(", "").replace(")", "").split(",")
        return_type_java = util.convert_jni_return_type(method_mapping.signature)
        func_type_java = Func_Type(args_type_java, return_type_java, class_name.replace("/", "."))
        location_java = Location("", 0, 0)
        caller_java = Func("java", method_mapping.java_method, func_type_java, location_java)

        # cpp
        result = search_method(file, func, method_mapping.cpp_method)
        if result is None:
            logging.debug(f"[{file.path}] search cpp method fail, method: {method_mapping.cpp_method}")
            continue
        cpp_method_node, cpp_method_file = result
        cpp_type_node = cpp_method_node.child_by_field_name("type")
        if cpp_type_node is None:
            return_type_cpp = "void"
        else:
            return_type_cpp = cpp_type_node.text.decode()
        cpp_declarator_node = cpp_method_node.child_by_field_name("declarator")
        if cpp_declarator_node is None:
            args_type_cpp = []
        else:
            cpp_param_node = cpp_declarator_node.child_by_field_name("parameters")
            if cpp_param_node is None:
                args_type_cpp = []
            else:
                args_type_cpp = cpp_param_node.text.decode().replace("(", "").replace(")", "").split(",")
        args_type_cpp = [arg.strip() for arg in args_type_cpp]
        for i in range(len(args_type_cpp)):
            if "/*" in args_type_cpp[i]:
                args_type_cpp[i] = util.remove_comments(args_type_cpp[i]).strip()
            args_type_cpp[i] = util.convert_cpp_type(args_type_cpp[i])
        func_type_cpp = Func_Type(args_type_cpp, return_type_cpp, "")
        location_cpp = Location(
            cpp_method_file.path, cpp_method_node.start_point[0] + 1, cpp_method_node.start_point[1] + 1)
        callee_cpp = Func("c/c++", method_mapping.cpp_method, func_type_cpp, location_cpp)
        call_site = Location("", 0, 0)
        calling = Calling(caller_java, [Callee(callee_cpp, call_site)])
        calling_list.append(calling)
    return calling_list

def search_jnimethods_mapping(func: Function, register_name: str, register_args_nodes: Node) -> list[JNINativeMethod] | None:
    file = func.file
    if register_name == "RegisterNatives":
        methods_id_node = register_args_nodes.named_children[1]
    else:
        methods_id_node = register_args_nodes.named_children[2]
    if methods_id_node is None:
        logging.debug(f"[{file.path}] {register_name}: methods_id_node is None")
        return None

    if methods_id_node.type == "identifier":
        target_search_key = methods_id_node.text.decode()
    elif methods_id_node.type == "call_expression":
        function_node = methods_id_node.child_by_field_name("function")
        if function_node is None:
            logging.debug(f"[{file.path}] {register_name}: function_node is None")
            return None
        argument_node = function_node.child_by_field_name("argument")
        if argument_node is None:
            logging.debug(f"[{file.path}] {register_name}: argument_node is None")
            return None
        target_search_key = argument_node.text.decode()
    else:
        logging.debug(f"[{file.path}] {register_name}: methods_id_node type is {methods_id_node.type}")
        return None

    jni_methods_mapping = search_maplist(func, target_search_key)
    if len(jni_methods_mapping) == 0:
        jni_methods_mapping = search_maplist_macro(func, target_search_key)
    if len(jni_methods_mapping) == 0:
        logging.debug(f"[{file.path}] {register_name}: search methods_id fail, target_search_key: {target_search_key}")
        return None
    return jni_methods_mapping

def search_class_name(func: Function, register_name: str, register_args_nodes: Node) -> str | None:
    file = func.file
    if register_name == "RegisterNatives":
        class_node = register_args_nodes.named_children[0]
    else:
        class_node = register_args_nodes.named_children[1]
    if class_node is None:
        logging.debug(f"[{file.path}] {register_name}: class_node is None")
        return None

    class_node_text = class_node.text.decode()
    if class_node.type == "string_literal" or class_node.type == "concatenated_string":
        return string_node_to_string(file, class_node)
    elif class_node.type == "identifier":
        if register_name == "RegisterNatives":
            return search_identifier_FindClass(file, func, class_node_text)
        return search_identifier_string(file, func, class_node_text)
    elif class_node.type == "qualified_identifier":
        class_name_node = class_node.child_by_field_name("name")
        if class_name_node is None:
            logging.debug(f"[{file.path}] {register_name}: class_name_node is None")
            return None
        return search_identifier_string(file, func, class_name_node.text.decode())
    elif class_node.type == "call_expression":
        function_node = class_node.child_by_field_name("function")
        if function_node is None:
            logging.debug(f"[{file.path}] {register_name}: function_node is None")
            return None
        argument_node = function_node.child_by_field_name("argument")
        if argument_node is None or argument_node.type != "identifier":
            logging.debug(f"[{file.path}] {register_name}: argument_node is None or not identifier")
            return None
        class_name = search_identifier_string(file, func, argument_node.text.decode())
        if class_name == UNKNOWN:
            class_name = search_identifier_FindClass(file, func, argument_node.text.decode())
        return class_name
    elif register_name == "RegisterNatives":
        return search_identifier_FindClass(file, func, class_node_text)
    else:
        if func.name in radar.CallKeywords:
            return None
        logging.debug(f"[{file.path}] {register_name}: class_node type is {class_node.type}")
        return None

def search_register_call(func: Function) -> tuple[list[str], list[Node]]:
    regex = "(registerNativeMethods|RegisterNatives|jniRegisterNativeMethods|JniRegisterNativeMethods|RegisterNativeMethods|RegisterMethodsOrDie)"
    register_call_query = f'''
    (
      (call_expression
        function: (identifier)@call.name
        (#match? @call.name "{regex}")
      )@call
    )
    (
      (call_expression
        function: (field_expression
          field: (field_identifier)@call.name
          (#match? @call.name "{regex}")
        )
      )@call
    )
    (
      (call_expression
        function: (qualified_identifier
          name: (identifier)@call.name
          (#match? @call.name "{regex}")
        )
      )@call
    )
    (
      (call_expression
        (qualified_identifier)@call.name
        (#match? @call.name "{regex}")
      )@call 
    )
    '''
    register_nodes = func.parser.query(register_call_query)
    if register_nodes is None:
        return [], []
    call_names = []
    call_nodes = []
    for node, type in register_nodes:
        if type == "call.name":
            call_names.append(node.text.decode())
        elif type == "call":
            call_nodes.append(node)
    return call_names, call_nodes

def java2cpp(func: Function) -> list[Calling]:
    calling_list = []
    file = func.file
    register_call_names, register_call_nodes = search_register_call(func)
    for register_name, register_node in zip(register_call_names, register_call_nodes):
        register_arguments_node = register_node.child_by_field_name("arguments")
        if register_arguments_node is None:
            logging.debug(f"[{file.path}] {register_name}: arguments is None")
            continue
        if len(register_arguments_node.named_children) < 3:
            # logging.debug(f"[{file.path}] {register_name}: arguments len < 3")
            continue
        class_name = search_class_name(func, register_name, register_arguments_node)
        if class_name is None:
            continue
        if class_name == UNKNOWN:
            if func.name in radar.CallKeywords:
                continue
            logging.debug(f"[{file.path}] {register_name}: class_name is <<<Unknown>>>, func: {func.name}")
            continue
        jni_methods_mapping = search_jnimethods_mapping(func, register_name, register_arguments_node)
        if jni_methods_mapping is None:
            continue
        callings = gen_java2cpp_calling_list(jni_methods_mapping, class_name, file, func)
        calling_list.extend(callings)
    return calling_list

@DeprecationWarning
def registerNativeMethods(file: File, func: Function) -> list[Calling]:
    calling_list = []
    call = func.search_function_call("registerNativeMethods")
    if call is None:
        return calling_list

    for c in call:
        if c[1] != "call":
            continue
        argument_list = c[0].child_by_field_name("arguments")
        if argument_list is None:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: argument_list is None")
            continue
        if len(argument_list.named_children) == 1:
            continue
        if len(argument_list.named_children) != 4:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: argument_list len is not 4")
            continue

        # 获取 class
        clazz_node = argument_list.named_children[1]
        if clazz_node.type == "string_literal" or clazz_node.type == "concatenated_string":
            class_name = string_node_to_string(file, clazz_node)
        elif clazz_node.type == "identifier":
            class_name = search_identifier_string(file, func, clazz_node.text.decode())
        elif clazz_node.type == "qualified_identifier":
            class_name_node = clazz_node.child_by_field_name("name")
            if class_name_node is None:
                class_name = UNKNOWN
            else:
                class_name = search_identifier_string(
                    file, func, class_name_node.text.decode())
        elif clazz_node.type == "call_expression":
            function_node = clazz_node.child_by_field_name("function")
            if function_node is None:
                class_name = UNKNOWN
            else:
                argument_node = function_node.child_by_field_name("argument")
                if argument_node is not None and argument_node.type == "identifier":
                    class_name = search_identifier_string(file, func, argument_node.text.decode())
                else:
                    class_name = UNKNOWN
        else:
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: clazz_node not string literal or identifier, {clazz_node.text.decode()}")
            continue
        if class_name == UNKNOWN:
            if func.name in radar.CallKeywords:
                continue
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: class_name is <<<Unknown>>>, func: {func.name}")
            continue

        # 获取 methods
        jni_methods_mapping = []
        methods_node = argument_list.named_children[2]
        if methods_node.type == "identifier":
            jni_methods_mapping = search_maplist(func, methods_node.text.decode())
            if len(jni_methods_mapping) == 0:
                jni_methods_mapping = search_maplist_macro(func, methods_node.text.decode())
        elif methods_node.type == "qualified_identifier":
            method_name_node = methods_node.child_by_field_name("name")
            if method_name_node is not None:
                jni_methods_mapping = search_maplist(
                    func, method_name_node.text.decode())
                if len(jni_methods_mapping) == 0:
                    jni_methods_mapping = search_maplist_macro(
                        func, method_name_node.text.decode())
        elif methods_node.type == "call_expression":
            function_node = methods_node.child_by_field_name("function")
            if function_node is None:
                continue
            argument_node = function_node.child_by_field_name("argument")
            if argument_node is None:
                continue
            jni_methods_mapping = search_maplist(func, argument_node.text.decode())
            if len(jni_methods_mapping) == 0:
                jni_methods_mapping = search_maplist_macro(func, argument_node.text.decode())
        else:
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: methods_node not identifier, {methods_node.text.decode()}")
            continue
        if len(jni_methods_mapping) == 0:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: jni_methods_mapping len is 0")
            continue

        callings = gen_java2cpp_calling_list(jni_methods_mapping, class_name, file, func)
        calling_list.extend(callings)
    return calling_list

@DeprecationWarning
def RegisterNatives(file: File, func: Function) -> list[Calling]:
    # https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/drm/jni/android_drm_DrmManagerClient.cpp;drc=3815d34e5f941909ceee9e879e309991c3d2a1d0;l=772?hl=zh-cn
    calling_list = []
    call = func.search_function_call("env->RegisterNatives")
    if call is None:
        return calling_list

    for c in call:
        if c[1] != "call":
            continue
        argument_list = c[0].child_by_field_name("arguments")
        if argument_list is None:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: argument_list is None")
            continue
        if len(argument_list.named_children) != 3:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: argument_list len is not 3")
            continue
        # 获取 class
        clazz_node = argument_list.named_children[0]
        if clazz_node.type == "string_literal" or clazz_node.type == "concatenated_string":
            class_name = string_node_to_string(file, clazz_node)
        elif clazz_node.type == "identifier" or clazz_node.type == "field_expression":
            class_name = search_identifier_FindClass(file, func, clazz_node.text.decode())
        elif clazz_node.type == "qualified_identifier":
            class_name_node = clazz_node.child_by_field_name("name")
            if class_name_node is None:
                class_name = UNKNOWN
            else:
                class_name = search_identifier_string(
                    file, func, class_name_node.text.decode())
        elif clazz_node.type == "call_expression":
            function_node = clazz_node.child_by_field_name("function")
            if function_node is None:
                class_name = UNKNOWN
            else:
                argument_node = function_node.child_by_field_name("argument")
                if argument_node is not None and argument_node.type == "identifier":
                    class_name = search_identifier_string(file, func, argument_node.text.decode())
                else:
                    class_name = UNKNOWN
        else:
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: clazz_node not string literal or identifier, {clazz_node.text.decode()}")
            continue
        if class_name == UNKNOWN:
            if func.name in radar.CallKeywords:
                continue
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: class_name is <<<Unknown>>>, func: {func.name}")
            continue

        # 获取 methods
        methods_node = argument_list.named_children[1]
        if methods_node.type == "identifier":
            jni_methods_mapping = search_maplist(func, methods_node.text.decode())
            if len(jni_methods_mapping) == 0:
                jni_methods_mapping = search_maplist_macro(func, methods_node.text.decode())
        elif methods_node.type == "call_expression":
            function_node = methods_node.child_by_field_name("function")
            if function_node is None:
                continue
            argument_node = function_node.child_by_field_name("argument")
            if argument_node is None:
                continue
            jni_methods_mapping = search_maplist(func, argument_node.text.decode())
            if len(jni_methods_mapping) == 0:
                jni_methods_mapping = search_maplist_macro(func, argument_node.text.decode())
        else:
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: methods_node not identifier, {methods_node.text.decode()}")
            continue
        if len(jni_methods_mapping) == 0:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: jni_methods_mapping len is 0")
            continue

        callings = gen_java2cpp_calling_list(jni_methods_mapping, class_name, file, func)
        calling_list.extend(callings)
    return calling_list

@DeprecationWarning
def jniRegisterNativeMethods(file: File, func: Function) -> list[Calling]:
    # https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/services/core/jni/com_android_server_am_CachedAppOptimizer.cpp?q=frameworks%2Fbase%2Fservices%2Fcore%2Fjni%2Fcom_android_server_am_CachedAppOptimizer.cpp&hl=zh-cn
    calling_list = []
    call = func.search_function_call("jniRegisterNativeMethods")
    if call is None:
        return calling_list

    for c in call:
        if c[1] != "call":
            continue
        argument_list = c[0].child_by_field_name("arguments")
        if argument_list is None:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: argument_list is None")
            continue
        if len(argument_list.named_children) != 4:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: argument_list len is not 4")
            continue

        # 获取 class
        clazz_node = argument_list.named_children[1]
        if clazz_node.type == "string_literal" or clazz_node.type == "concatenated_string":
            class_name = string_node_to_string(file, clazz_node)
        elif clazz_node.type == "identifier":
            class_name = search_identifier_string(file, func, clazz_node.text.decode())
        elif clazz_node.type == "qualified_identifier":
            class_name_node = clazz_node.child_by_field_name("name")
            if class_name_node is None:
                class_name = UNKNOWN
            else:
                class_name = search_identifier_string(
                    file, func, class_name_node.text.decode())
        elif clazz_node.type == "call_expression":
            function_node = clazz_node.child_by_field_name("function")
            if function_node is None:
                class_name = UNKNOWN
            else:
                argument_node = function_node.child_by_field_name("argument")
                if argument_node is not None and argument_node.type == "identifier":
                    class_name = search_identifier_string(file, func, argument_node.text.decode())
                else:
                    class_name = UNKNOWN
        else:
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: clazz_node not string literal or identifier, {clazz_node.text.decode()}")
            continue
        if class_name == UNKNOWN:
            if func.name in radar.CallKeywords:
                continue
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: class_name is <<<Unknown>>>, func: {func.name}")
            continue

        # 获取 methods
        methods_node = argument_list.named_children[2]
        if methods_node.type == "identifier":
            jni_methods_mapping = search_maplist(func, methods_node.text.decode())
            if len(jni_methods_mapping) == 0:
                jni_methods_mapping = search_maplist_macro(func, methods_node.text.decode())
        elif methods_node.type == "call_expression":
            function_node = methods_node.child_by_field_name("function")
            if function_node is None:
                continue
            argument_node = function_node.child_by_field_name("argument")
            if argument_node is None:
                continue
            jni_methods_mapping = search_maplist(func, argument_node.text.decode())
            if len(jni_methods_mapping) == 0:
                jni_methods_mapping = search_maplist_macro(func, argument_node.text.decode())
        else:
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: methods_node not identifier, {methods_node.text.decode()}")
            continue
        if len(jni_methods_mapping) == 0:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: jni_methods_mapping len is 0")
            continue

        callings = gen_java2cpp_calling_list(jni_methods_mapping, class_name, file, func)
        calling_list.extend(callings)
    return calling_list

@DeprecationWarning
def JniRegisterNativeMethods(file: File, func: Function) -> list[Calling]:
    # vendor/honor/system/base/frameworks/base/packages/HwWifiNetworkService/WiFiEnhance/jni/DcJni.cpp
    calling_list = []
    call = func.search_function_call("JniRegisterNativeMethods")
    if call is None:
        return calling_list

    for c in call:
        if c[1] != "call":
            continue
        argument_list = c[0].child_by_field_name("arguments")
        if argument_list is None:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: argument_list is None")
            continue
        if len(argument_list.named_children) != 4:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: argument_list len is not 4")
            continue

        # 获取 class
        clazz_node = argument_list.named_children[1]
        if clazz_node.type == "string_literal" or clazz_node.type == "concatenated_string":
            class_name = string_node_to_string(file, clazz_node)
        elif clazz_node.type == "identifier":
            class_name = search_identifier_string(file, func, clazz_node.text.decode())
        elif clazz_node.type == "qualified_identifier":
            class_name_node = clazz_node.child_by_field_name("name")
            if class_name_node is None:
                class_name = UNKNOWN
            else:
                class_name = search_identifier_string(
                    file, func, class_name_node.text.decode())
        elif clazz_node.type == "call_expression":
            function_node = clazz_node.child_by_field_name("function")
            if function_node is None:
                class_name = UNKNOWN
            else:
                argument_node = function_node.child_by_field_name("argument")
                if argument_node is not None and argument_node.type == "identifier":
                    class_name = search_identifier_string(file, func, argument_node.text.decode())
                else:
                    class_name = UNKNOWN
        else:
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: clazz_node not string literal or identifier, {clazz_node.text.decode()}")
            continue
        if class_name == UNKNOWN:
            if func.name in radar.CallKeywords:
                continue
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: class_name is <<<Unknown>>>, func: {func.name}")
            continue

        # 获取 methods
        methods_node = argument_list.named_children[2]
        if methods_node.type == "identifier":
            jni_methods_mapping = search_maplist(func, methods_node.text.decode())
            if len(jni_methods_mapping) == 0:
                jni_methods_mapping = search_maplist_macro(func, methods_node.text.decode())
        elif methods_node.type == "call_expression":
            function_node = methods_node.child_by_field_name("function")
            if function_node is None:
                continue
            argument_node = function_node.child_by_field_name("argument")
            if argument_node is None:
                continue
            jni_methods_mapping = search_maplist(func, argument_node.text.decode())
            if len(jni_methods_mapping) == 0:
                jni_methods_mapping = search_maplist_macro(func, argument_node.text.decode())
        else:
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: methods_node not identifier, {methods_node.text.decode()}")
            continue
        if len(jni_methods_mapping) == 0:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: jni_methods_mapping len is 0")
            continue

        callings = gen_java2cpp_calling_list(jni_methods_mapping, class_name, file, func)
        calling_list.extend(callings)
    return calling_list

@DeprecationWarning
def RegisterMethodsOrDie(file: File, func: Function) -> list[Calling]:
    calling_list = []
    call = func.search_function_call("RegisterMethodsOrDie")
    if call is None:
        return calling_list

    for c in call:
        if c[1] != "call":
            continue
        argument_list = c[0].child_by_field_name("arguments")
        if argument_list is None:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: argument_list is None")
            continue
        if len(argument_list.named_children) != 4:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: argument_list len is not 4")
            continue

        # 获取 class
        clazz_node = argument_list.named_children[1]
        if clazz_node.type == "string_literal" or clazz_node.type == "concatenated_string":
            class_name = string_node_to_string(file, clazz_node)
        elif clazz_node.type == "identifier":
            class_name = search_identifier_string(file, func, clazz_node.text.decode())
        elif clazz_node.type == "qualified_identifier":
            class_name_node = clazz_node.child_by_field_name("name")
            if class_name_node is None:
                class_name = UNKNOWN
            else:
                class_name = search_identifier_string(
                    file, func, class_name_node.text.decode())
        elif clazz_node.type == "call_expression":
            function_node = clazz_node.child_by_field_name("function")
            if function_node is None:
                class_name = UNKNOWN
            else:
                argument_node = function_node.child_by_field_name("argument")
                if argument_node is not None and argument_node.type == "identifier":
                    class_name = search_identifier_string(file, func, argument_node.text.decode())
                else:
                    class_name = UNKNOWN
        else:
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: clazz_node not string literal or identifier, {clazz_node.text.decode()}"
            )
            continue
        if class_name == UNKNOWN:
            if func.name in radar.CallKeywords:
                continue
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: class_name is <<<Unknown>>>, func: {func.name}")
            continue

        # 获取 methods
        methods_node = argument_list.named_children[2]
        if methods_node.type == "identifier":
            jni_methods_mapping = search_maplist(func, methods_node.text.decode())
            if len(jni_methods_mapping) == 0:
                jni_methods_mapping = search_maplist_macro(func, methods_node.text.decode())
        elif methods_node.type == "call_expression":
            function_node = methods_node.child_by_field_name("function")
            if function_node is None:
                continue
            argument_node = function_node.child_by_field_name("argument")
            if argument_node is None:
                continue
            jni_methods_mapping = search_maplist(func, argument_node.text.decode())
            if len(jni_methods_mapping) == 0:
                jni_methods_mapping = search_maplist_macro(func, argument_node.text.decode())
        else:
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: methods_node not identifier, {methods_node.text.decode()}")
            continue
        if len(jni_methods_mapping) == 0:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: jni_methods_mapping len is 0")
            continue

        callings = gen_java2cpp_calling_list(jni_methods_mapping, class_name, file, func)
        calling_list.extend(callings)
    return calling_list

@DeprecationWarning
def RegisterNativeMethods(file: File, func: Function) -> list[Calling]:
    calling_list = []
    call = func.search_function_call("RegisterNativeMethods")
    if call is None:
        return calling_list

    for c in call:
        if c[1] != "call":
            continue
        argument_list = c[0].child_by_field_name("arguments")
        if argument_list is None:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: argument_list is None")
            continue
        if len(argument_list.named_children) != 4:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: argument_list len is not 4")
            continue

        # 获取 class
        clazz_node = argument_list.named_children[1]
        if clazz_node.type == "string_literal" or clazz_node.type == "concatenated_string":
            class_name = string_node_to_string(file, clazz_node)
        elif clazz_node.type == "identifier":
            class_name = search_identifier_string(file, func, clazz_node.text.decode())
        elif clazz_node.type == "qualified_identifier":
            class_name_node = clazz_node.child_by_field_name("name")
            if class_name_node is None:
                class_name = UNKNOWN
            else:
                class_name = search_identifier_string(
                    file, func, class_name_node.text.decode())
        elif clazz_node.type == "call_expression":
            function_node = clazz_node.child_by_field_name("function")
            if function_node is None:
                class_name = UNKNOWN
            else:
                argument_node = function_node.child_by_field_name("argument")
                if argument_node is not None and argument_node.type == "identifier":
                    class_name = search_identifier_string(file, func, argument_node.text.decode())
                else:
                    class_name = UNKNOWN
        else:
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: clazz_node not string literal or identifier, {clazz_node.text.decode()}"
            )
            continue
        if class_name == UNKNOWN:
            if func.name in radar.CallKeywords:
                continue
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: class_name is <<<Unknown>>>, func: {func.name}")
            continue

        # 获取 methods
        methods_node = argument_list.named_children[2]
        if methods_node.type == "identifier":
            jni_methods_mapping = search_maplist(func, methods_node.text.decode())
            if len(jni_methods_mapping) == 0:
                jni_methods_mapping = search_maplist_macro(func, methods_node.text.decode())
        elif methods_node.type == "call_expression":
            function_node = methods_node.child_by_field_name("function")
            if function_node is None:
                continue
            argument_node = function_node.child_by_field_name("argument")
            if argument_node is None:
                continue
            jni_methods_mapping = search_maplist(func, argument_node.text.decode())
            if len(jni_methods_mapping) == 0:
                jni_methods_mapping = search_maplist_macro(func, argument_node.text.decode())
        else:
            logging.debug(
                f"[{file.path}] {inspect.stack()[0][3]}: methods_node not identifier, {methods_node.text.decode()}")
            continue
        if len(jni_methods_mapping) == 0:
            logging.debug(f"[{file.path}] {inspect.stack()[0][3]}: jni_methods_mapping len is 0")
            continue

        callings = gen_java2cpp_calling_list(jni_methods_mapping, class_name, file, func)
        calling_list.extend(callings)
    return calling_list
