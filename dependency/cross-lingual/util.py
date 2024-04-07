import re
import logging

UNKNOWN = '<<<Unknown>>>'

def convert_jni_return_type(jni_signature: str) -> str:
    if jni_signature == UNKNOWN:
        logging.debug("convert_jni_return_type is <<<Unknown>>>")
        return 'void'
    if jni_signature == "":
        return 'void'

    jni_signature = jni_signature.strip('"')
    return_sig = jni_signature.split(')')[-1]
    if return_sig.startswith('L'):
        return return_sig[1:-1].replace('/', '.')
    if return_sig.startswith('['):
        return convert_jni_return_type(return_sig[1:]) + "[]"
    return_type_mapping = {
        'Z': 'boolean',
        'B': 'byte',
        'C': 'char',
        'S': 'short',
        'I': 'int',
        'J': 'long',
        'F': 'float',
        'D': 'double',
        'V': 'void'
    }
    if return_sig not in return_type_mapping:
        logging.debug(f"return_sig not in return_type_mapping: {return_sig}")
        return ''
    return return_type_mapping[return_sig]

def convert_jni_signature(jni_signature: str) -> str:
    if jni_signature == UNKNOWN:
        logging.debug("convert_jni_signature is <<<Unknown>>>")
        return '()'
    if jni_signature == '':
        return '()'

    param_type_mapping = {
        'Z': 'boolean',
        'B': 'byte',
        'C': 'char',
        'S': 'short',
        'I': 'int',
        'J': 'long',
        'F': 'float',
        'D': 'double'
    }

    converted_signature = '('
    try:
        param_index = jni_signature.index('(') + 1  # 获取参数部分的起始索引
    except ValueError:
        logging.debug(f"convert_jni_signature ValueError: {jni_signature}")
        return '()'
    while jni_signature[param_index] != ')':  # 遍历参数部分，直到遇到参数结束符号 ')'
        param_type = jni_signature[param_index]
        if param_type in param_type_mapping:  # 原始类型
            converted_signature += param_type_mapping[param_type]
        elif param_type == 'L':  # 对象类型
            end_index = jni_signature.index(
                ';', param_index + 1)
            class_name = jni_signature[param_index + 1:end_index].replace(
                '/', '.')
            param_index += end_index - param_index
            converted_signature += class_name
        elif param_type == '[':  # 数组类型
            param_index += 1
            param_type = jni_signature[param_index]
            if jni_signature[param_index] in param_type_mapping:
                converted_signature += param_type_mapping[param_type]
                converted_signature += "[]"
            elif jni_signature[param_index] == 'L':
                end_index = jni_signature.index(
                    ';', param_index + 1)
                class_name = jni_signature[param_index + 1:end_index].replace(
                    '/', '.')
                param_index += end_index - param_index
                converted_signature += class_name + "[]"

        param_index += 1

        if jni_signature[param_index] != ')':  # 如果还有参数，添加逗号分隔符
            converted_signature += ','

    return converted_signature + ')'

def remove_comments(string):
    pattern = r"(\".*?\"|\'.*?\')|(/\*.*?\*/|//[^\r\n]*$)"
    # first group captures quoted strings (double or single)
    # second group captures comments (//single-line or /* multi-line */)
    regex = re.compile(pattern, re.MULTILINE | re.DOTALL)

    def _replacer(match):
        # if the 2nd group (capturing comments) is not None,
        # it means we have captured a non-quoted (real) comment string.
        if match.group(2) is not None:
            return ""  # so we will return empty to remove the comment
        else:  # otherwise, we will return the 1st group
            return match.group(1)  # captured quoted-string
    return regex.sub(_replacer, string)

def convert_cpp_type(string):
    # JNIEnv *env -> JNIEnv*
    # jlong ptr -> jlong
    # const jlong ptr -> const jlong
    index = string.rfind(' ')
    if index == -1:
        return string
    if "*" in string[index:]:
        string = string[:index] + "*"
    else:
        string = string[:index]
    if " " in string:
        string = string.split(" ")[-1]
    return string

if __name__ == '__main__':
    print(convert_cpp_type("JNIEnv *env"))
