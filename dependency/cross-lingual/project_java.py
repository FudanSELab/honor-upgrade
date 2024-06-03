from __future__ import annotations
import logging
import os
import pickle
import ast_parser
from tqdm import tqdm
from util import UNKNOWN
from tree_sitter import Node
from ast_parser import ASTParser


class JavaProject:
    def __init__(self, project_path: str):
        self.project_path = project_path
        self.files: list[File] = []

        self.files_list: set[str] = set()
        self.classes_list: set[str] = set()
        self.methods_list: set[str] = set()
        self.methods_list_no_param: set[str] = set()

        all_java_files = []
        for root, dirs, files in os.walk(project_path):
            for file in files:
                if file.endswith(".java"):
                    path = os.path.join(root, file)
                    path = path.replace(project_path, "").strip("/")
                    all_java_files.append(path)
        logging.info(f"Total {len(all_java_files)} java files found")

        for jf in tqdm(all_java_files, desc="Parsing Code files"):
            with open(os.path.join(project_path, jf), "r") as f:
                try:
                    content = f.read()
                except UnicodeDecodeError:
                    logging.debug(f"UnicodeDecodeError: {jf}")
                    continue
            file = File(jf, content)
            self.files.append(file)
            self.files_list.add(file.path.replace(self.project_path, ""))
            self.classes_list.update([clazz.fullname for clazz in file.classes])
            self.methods_list.update([method.signature for clazz in file.classes for method in clazz.methods])
            self.methods_list_no_param.update([method.signature.split("(")[0]
                                              for clazz in file.classes for method in clazz.methods])

        self.methods = [method for file in self.files for clazz in file.classes for method in clazz.methods]

    def parse_file(self, jf: str):
        with open(os.path.join(self.project_path, jf), "r") as f:
            try:
                content = f.read()
            except UnicodeDecodeError:
                return None
        return File(jf, content)

    def get_file(self, path: str):
        for file in self.files:
            if file.path == path:
                return file
        return None

    def get_class(self, fullname: str):
        for file in self.files:
            for clazz in file.classes:
                if clazz.fullname == fullname:
                    return clazz
        return None

    def get_method(self, fullname: str):
        for file in self.files:
            for clazz in file.classes:
                for method in clazz.methods:
                    if method.signature == fullname:
                        return method
        return None

    def search_method(self, class_name: str, method_name: str, param_len: int):
        class_name = class_name.replace("/", ".") if "/" in class_name else class_name
        if "$" in class_name:
            pkg = class_name[:class_name.rfind(".")]
            name = class_name.split("$")[1]
            class_name = f"{pkg}.{name}"
        for file in self.files:
            for clazz in file.classes:
                if clazz.fullname == class_name:
                    for method in clazz.methods:
                        if method.name == method_name and method.param_len == param_len:
                            return method
                    for method in clazz.methods:
                        if method.name == method_name:
                            return method
                    break
        return None

    def search_class_by_method(self, method_name: str, param_len: int):
        potential_classes = []
        for file in self.files:
            for clazz in file.classes:
                for method in clazz.methods:
                    if method.name == method_name and method.param_len == param_len:
                        potential_classes.append(clazz)
        if len(potential_classes) == 1:
            return potential_classes[0].fullname
        elif len(potential_classes) > 1:
            return None
        else:
            return None


class File:
    def __init__(self, path: str, content: str):
        parser = ASTParser(content, "java")
        # self.parser = parser
        self.path = path
        self.code = content
        package = parser.query_oneshot(ast_parser.TS_QUERY_PACKAGE)
        self.package = package.text.decode() if package is not None else "<NONE>"
        self.imports = [import_node[0].text.decode() for import_node in parser.query(ast_parser.TS_IMPORT)]
        self.classes = [Class(class_node[0], self, parser) for class_node in parser.query(ast_parser.TS_CLASS)]
        self.methods = [method for clazz in self.classes for method in clazz.methods]


class Class:
    def __init__(self, node: Node, file: File, parser: ASTParser):
        self.code = node.text.decode()
        # parser = file.parser
        name_node = node.child_by_field_name("name")
        self.name = name_node.text.decode()  # type: ignore
        self.fullname = f"{file.package}.{self.name}"
        self.file = file
        self.methods: list[Method] = []
        for method_node in parser.query(ast_parser.TS_METHOD):
            cls_inf_body_node = method_node[0].parent
            if (cls_inf_body_node is None or
                    cls_inf_body_node.type not in ["class_body", "interface_body"]):
                continue
            cls_inf_node = cls_inf_body_node.parent
            if cls_inf_node is None:
                continue
            cls_inf_name = cls_inf_node.child_by_field_name("name")
            if cls_inf_name is None or cls_inf_name.text.decode() != self.name:
                continue
            self.methods.append(Method(method_node[0], self, file))


class Method:
    def __init__(self, node: Node, clazz: Class, file: File):
        name_node = node.child_by_field_name("name")
        self.name = name_node.text.decode() if name_node is not None else UNKNOWN
        self.fullname = f"{clazz.fullname}.{self.name}"
        self.clazz = clazz
        self.file = file
        self.code = node.text.decode()
        parameters_node = node.child_by_field_name("parameters")
        parameters = ASTParser.children_by_type_name(
            parameters_node, "formal_parameter") if parameters_node is not None else []
        self.param_len = len(parameters)
        type_node = node.child_by_field_name("type")
        parameter_signature = ",".join([type_node.text.decode() for param in parameters if type_node is not None])
        self.signature = f"{self.clazz.fullname}.{self.name}({parameter_signature})"
        self.start_line = node.start_point[0] + 1
        self.end_line = node.end_point[0] + 1
        self.lines: dict[int, str] = {i + self.start_line: line for i, line in enumerate(self.code.split("\n"))}

        body_node = node.child_by_field_name("body")
        if body_node is None:
            self.body_start_line = self.start_line
            self.body_end_line = self.end_line
        else:
            self.body_start_line = body_node.start_point[0] + 1
            self.body_end_line = body_node.end_point[0] + 1


if __name__ == "__main__":
    # project = JavaProject("/home/wrs/aosp")
    # print(len(project.classes_list))
    # pickle.dump(project, open("java_project.pkl", "wb"))
    project = pickle.load(open("java_project.pkl", "rb"))
    print(len(project.classes_list))
