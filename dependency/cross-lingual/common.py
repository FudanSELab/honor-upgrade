import logging
from tree_sitter import Node
from project_cpp import File
from util import UNKNOWN

def search_define(file: File, identifier: str) -> str:
    query = '''
    (
        preproc_def 
        (identifier)@id
        (preproc_arg)@arg
        (#eq? @id "{0}")
    )
    '''
    result = file.parser.query(query.format(identifier))
    if len(result) == 0:
        return UNKNOWN
    for r in result:
        if r[1] != "arg":
            continue
        return r[0].text.decode()
    logging.debug(f"[{file.path}] search_define: {identifier}")
    return UNKNOWN

def string_node_to_string(file: File, node: Node):
    if node.type == "string_literal":
        return node.named_children[0].text.decode()
    elif node.type == "concatenated_string":
        string = ""
        for sub in node.named_children:
            if sub.type == "string_literal":
                string += sub.named_children[0].text.decode()
            elif sub.type == "identifier":
                string += search_define(file, sub.text.decode())
        string = string.replace('"', "").replace(" ", "").replace("\n", "").strip()
        return string
    elif (node.type == "ERROR" and node.next_named_sibling is not None
          and node.next_named_sibling.type == "concatenated_string"):
        string = node.named_children[0].named_children[0].text.decode(
        ) + string_node_to_string(file, node.next_named_sibling)
        return string
    elif node.type == "identifier":
        return node.text.decode()
    logging.debug(f"[{file.path}] string_node_to_string: {node.text.decode()}")
    return node.text.decode()