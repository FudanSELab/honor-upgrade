import os
import util
import logging
import ast_parser
from util import UNKNOWN
from tree_sitter import Node
from ast_parser import ASTParser

class File:
    def __init__(self, path: str, repo_path: str, content: str):
        parser = ASTParser(content, "cpp")
        self.parser = parser
        self.repo_path = repo_path
        self.path = path
        self.code = content

        self.includes: list[str] = []
        for node in parser.query(ast_parser.CPP_INCLUDE):
            self.includes.append(node[0].text.decode())

        self.functions: list[Function] = []
        for node in parser.query(ast_parser.CPP_FUNCTION):
            func = Function(node[0], self)
            if func.name != UNKNOWN:
                self.functions.append(func)

    def search_function_call(self, name: str):
        call = self.parser.query(ast_parser.CPP_CALL.format(name))
        if len(call) == 0:
            return None
        return call

    def search_includes(self, identifier: str):
        for include in self.includes:
            include_file = os.path.join(os.path.dirname(self.path), include)
            file_path = os.path.join(self.repo_path, include_file)
            if not os.path.exists(file_path):
                continue
            with open(file_path) as f:
                code = f.read()
            file = File(include_file, self.repo_path, code)
            for func in file.functions:
                if func.name == identifier:
                    return func

            if file_path.endswith(".h"):
                file_path = file_path.replace(".h", ".cpp")
            if not os.path.exists(file_path):
                continue
            include_file = include_file.replace(".h", ".cpp")
            with open(file_path) as f:
                code = f.read()
            file = File(file_path, self.repo_path, code)
            for func in file.functions:
                if func.name == identifier:
                    return func

        path = os.path.join(self.repo_path, os.path.dirname(self.path))
        if not os.path.exists(path):
            return None
        for f in os.listdir(path):
            if not f.endswith(".cpp") and not f.endswith(".h"):
                continue
            with open(os.path.join(path, f)) as file:
                code = file.read()
            file = File(f, self.repo_path, code)
            for func in file.functions:
                if func.name == identifier:
                    return func
        return None

class Function:
    def __init__(self, node: Node, file: File):
        self.node = node
        self.file = file
        declarator = node.child_by_field_name("declarator")
        name_declarator = declarator.child_by_field_name("declarator")  # type: ignore
        if declarator is not None and name_declarator is not None and declarator.type == "pointer_declarator":
            name_declarator = name_declarator.child_by_field_name("declarator")
        type_node = node.child_by_field_name("type")
        if name_declarator is None:
            if type_node is not None and type_node.text.decode() == "EXPORT":
                # https://cs.android.com/android/platform/superproject/main/+/main:frameworks/av/media/ndk/NdkMediaExtractor.cpp;drc=71c806d9f13939dda9ac9648171bb20849cf96f6;l=63?hl=zh-cn
                self.name = declarator.child_by_field_name("name").text.decode()  # type: ignore
            elif declarator is not None and declarator.type == "reference_declarator":
                # https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/media/jni/android_media_MediaCodec.h;drc=677a881f474609c0d351b68ffe0e22e7c6373075;l=188?hl=zh-cn
                self.name = ASTParser.child_by_type_name(
                    declarator, "function_declarator").child_by_field_name("declarator").text.decode()  # type: ignore
            else:
                self.name = UNKNOWN
        else:
            self.name = name_declarator.text.decode()
        self.body = node.child_by_field_name("body")
        if self.body is None:
            self.name = UNKNOWN
            return
        self.parser = ASTParser(self.body.text, "cpp")

    def search_function_call(self, name: str):
        call = self.parser.query(ast_parser.CPP_CALL.format(name))
        if len(call) == 0:
            return None
        return call

    def get_return_type(self) -> str:
        cpp_method_node = self.node
        return_type_cpp = cpp_method_node.child_by_field_name("type")
        if return_type_cpp is None:
            prev_sibling_node = cpp_method_node.prev_named_sibling
            if prev_sibling_node is not None and len(prev_sibling_node.children) == 0:
                return_type_cpp = "void"
            elif (prev_sibling_node is not None
                    and prev_sibling_node.children[len(prev_sibling_node.children) - 1].type == ";"):
                prev_sibling_node_return = prev_sibling_node.child_by_field_name(
                    "type")
                if prev_sibling_node_return is not None:
                    return_type_cpp = prev_sibling_node_return.text.decode()
                else:
                    return_type_cpp = "void"
            else:
                return_type_cpp = "void"
                logging.debug(
                    f"gen_cpp2java_calling: return_type_cpp is None, function: {self.name}, file: {self.file.path}")
        else:
            return_type_cpp = return_type_cpp.text.decode()
        return return_type_cpp

    def get_args_type(self) -> list[str]:
        cpp_method_node = self.node
        cpp_file = self.file
        cpp_method_declarator_node = cpp_method_node.child_by_field_name("declarator")
        if cpp_method_declarator_node is None:
            args_type_cpp = []
        else:
            if cpp_method_declarator_node.type == "pointer_declarator":
                cpp_method_declarator_node = cpp_method_declarator_node.child_by_field_name("declarator")
            if cpp_method_declarator_node is None:
                args_type_cpp = []
                logging.debug(
                    f"gen_cpp2java_calling: cpp_method_declarator_node is None, file: {cpp_file.path}, function: {self.name}")
            else:
                args_node = cpp_method_declarator_node.child_by_field_name("parameters")
                if args_node is None:
                    args_type_cpp = []
                    logging.debug(
                        f"gen_cpp2java_calling: args_node is None, file: {cpp_file.path}, function: {self.name}")
                else:
                    args_type_cpp = args_node.text.decode().replace("(", "").replace(")", "").split(",")
            args_type_cpp = [arg.strip() for arg in args_type_cpp]
        for i in range(len(args_type_cpp)):
            if "/*" in args_type_cpp[i]:
                args_type_cpp[i] = util.remove_comments(args_type_cpp[i]).strip()
            args_type_cpp[i] = util.convert_cpp_type(args_type_cpp[i])
        return args_type_cpp

class JNINativeMethod:
    def __init__(self, java_method: str, signature: str, cpp_method: str):
        self.java_method = java_method
        self.signature = signature
        self.cpp_method = cpp_method


if __name__ == '__main__':
    code = """
#include <android-base/macros.h>
#include <log/log_id.h>

#include <nativehelper/JNIHelp.h>
#include "jni.h"

#include "core_jni_helpers.h"
#include "eventlog_helper.h"

void test() {
	test2();
}
    """
    path = "/home/wrs/aosp/frameworks/base/core/jni/android_util_EventLog.cpp"
    with open(path) as f:
        code = f.read()
    file = File(path, path, code)
    func = file.search_includes("writeEventInteger")
    if func is not None:
        print(func.node.text.decode())
