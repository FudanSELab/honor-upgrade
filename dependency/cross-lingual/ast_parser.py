import logging
import platform
from util import UNKNOWN
from tree_sitter import Language, Parser, Node
from tree_sitter_languages import get_language, get_parser

import warnings
warnings.simplefilter(action='ignore', category=FutureWarning)

TS_QUERY_PACKAGE = "(package_declaration (scoped_identifier) @package)(package_declaration (identifier) @package)"
TS_IMPORT = "(import_declaration (scoped_identifier) @import)"
TS_CLASS = "(class_declaration) @class (interface_declaration) @interface"
TS_FIELD = "(field_declaration) @field"
TS_METHOD = "(method_declaration) @method"
CPP_FUNCTION = "(function_definition) @function"
CPP_TOP_DECLARATION = "(translation_unit (declaration) @declaration)"
CPP_CALL = '''
((call_expression (identifier)@call.name)@call (#eq? @call.name "{0}"))
((call_expression (field_expression)@call.name)@call (#eq? @call.name "{0}"))
((call_expression (qualified_identifier)@call.name)@call (#match? @call.name "{0}"))
'''
CPP_INCLUDE = '''
(preproc_include
  path: (string_literal
    (string_content)@incude
  )
)
'''

class ASTParser:
    def __init__(self, code: str | bytes, language: str):
        # if platform.system() == "Darwin":
        #     library_path = "lib/languages-macos.so"
        # elif platform.system() == "Linux":
        #     library_path = "lib/languages-linux.so"
        # else:
        #     logging.fatal(f"Not supported system: {platform.system()}")
        #     return
        # self.LANGUAGE = Language(library_path, language)
        # self.parser = Parser()
        # self.parser.set_language(self.LANGUAGE)
        self.LANGUAGE = get_language(language)
        self.parser = get_parser(language)
        if isinstance(code, str):
            self.root = self.parser.parse(bytes(code, "utf-8")).root_node
        else:
            self.root = self.parser.parse(code).root_node

    @staticmethod
    def children_by_type_name(node: Node, type: str) -> list[Node]:
        node_list = []
        for child in node.named_children:
            if child.type == type:
                node_list.append(child)
        return node_list

    @staticmethod
    def child_by_type_name(node: Node, type: str) -> Node | None:
        for child in node.named_children:
            if child.type == type:
                return child
        return None

    def query_oneshot(self, query_str: str) -> Node | None:
        query = self.LANGUAGE.query(query_str)
        captures = query.captures(self.root)
        result = None
        for capture in captures:
            result = capture[0]
            break
        return result

    def query(self, query_str: str):
        try:
            query = self.LANGUAGE.query(query_str)
            captures = query.captures(self.root)
        except Exception as e:
            return []
        return captures




if __name__ == "__main__":
    code = """
    package Tika;

    import java.io.File;
    import java.io.IOException;
    import java.net.URL;

    import org.apache.tika.exception.TikaException;
    """

    query_str = """
    (package_declaration (scoped_identifier) @package)
    (package_declaration (identifier) @package)
    """

    res = ASTParser(bytes(code, "utf8"), language="java").query_oneshot(query_str)
    if res is not None:
        print(res.text.decode())
    else:
        print("None")
