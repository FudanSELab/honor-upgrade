import os
import json
import radar
import pickle
import shutil
import logging
import argparse
import java2cpp
import cpp2java
import project_cpp
import multiprocessing
import concurrent.futures
from tqdm import tqdm
from dependency import Calling, Location
from project_java import JavaProject, File, Class, Method

def init_logger(logger: logging.Logger, log_level: int, log_file_path: str):
    logger.setLevel(log_level)
    formatter = logging.Formatter(
        "[%(asctime)s][%(levelname)s]: %(message)s", datefmt="%Y-%m-%d %H:%M:%S"
    )
    fh = logging.FileHandler(log_file_path, encoding="utf-8", mode="w")
    fh.setLevel(logging.DEBUG)
    fh.setFormatter(formatter)

    ch = logging.StreamHandler()
    ch.setLevel(logging.INFO)
    ch.setFormatter(formatter)

    logger.addHandler(ch)
    logger.addHandler(fh)

def java2cpp_scan_process(file_path: str, repo_path: str) -> None | list[Calling]:
    calling_list_all: list[Calling] = []
    with open(os.path.join(repo_path, file_path), "r") as f:
        try:
            content = f.read()
        except UnicodeDecodeError:
            logging.debug(f"UnicodeDecodeError: {file_path}")
            return None
    file = project_cpp.File(file_path, repo_path, content)
    for func in file.functions:
        calling_list_all.extend(java2cpp.java2cpp(func))
        # calling_list_all.extend(java2cpp.registerNativeMethods(file, func))
        # calling_list_all.extend(java2cpp.RegisterNatives(file, func))
        # calling_list_all.extend(java2cpp.jniRegisterNativeMethods(file, func))
        # calling_list_all.extend(java2cpp.JniRegisterNativeMethods(file, func))
        # calling_list_all.extend(java2cpp.RegisterMethodsOrDie(file, func))
        # calling_list_all.extend(java2cpp.RegisterNativeMethods(file, func))
    return calling_list_all

def java2cpp_java_loc(calling_list_all: list[Calling], java_project: JavaProject) -> list[Calling]:
    for call in calling_list_all:
        caller_java = call.caller
        method_name = caller_java.func_name
        class_name = caller_java.func_type.class_name
        type_len = len(caller_java.func_type.args_type)
        method_java = java_project.search_method(class_name, method_name, type_len)
        if method_java is not None:
            location_java = Location(method_java.file.path, method_java.start_line, 0)
            caller_java.location = location_java
            call.callees[0].call_site = location_java
    return calling_list_all

def cpp2java_java_loc(calling_list_all: list[Calling], java_project: JavaProject) -> list[Calling]:
    for call in calling_list_all:
        callees = call.callees
        for callee in callees:
            callee_java = callee.called_func
            method_name = callee_java.func_name
            class_name = callee_java.func_type.class_name
            type_len = len(callee_java.func_type.args_type)
            method_java = java_project.search_method(class_name, method_name, type_len)
            if method_java is not None:
                location_java = Location(method_java.file.path, method_java.start_line, 0)
                callee_java.location = location_java
    return calling_list_all

def java2cpp_scan(repo_path: str, file_list: list[str], java_project: JavaProject, threads: int | None) -> list[Calling]:
    calling_list_all: list[Calling] = []
    with concurrent.futures.ProcessPoolExecutor(max_workers=threads, mp_context=multiprocessing.get_context("fork")) as executor:
        results = [executor.submit(java2cpp_scan_process, file, repo_path) for file in file_list]
        for result in tqdm(concurrent.futures.as_completed(results), desc="Scanning Java Call C++", total=len(results)):
            calling_list = result.result()
            if calling_list is not None:
                calling_list_all.extend(calling_list)
    logging.info(f"calling list java2cpp len: {len(calling_list_all)}")
    calling_list_all = java2cpp_java_loc(calling_list_all, java_project)
    return calling_list_all

def cpp2java_scan_process(file_path: str, repo_path: str, func_class_pair: dict[str, str]) -> list[Calling] | None:
    calling_list: list[Calling] = []
    with open(os.path.join(repo_path, file_path), "r") as f:
        try:
            content = f.read()
        except UnicodeDecodeError:
            logging.debug(f"UnicodeDecodeError: {file_path}")
            return None
    file = project_cpp.File(file_path, repo_path, content)
    for func in file.functions:
        calling = cpp2java.cpp2java(func, func_class_pair)
        if calling is None:
            continue
        calling_list.append(calling)
    return calling_list

def cpp2java_scan(repo_path: str, file_list: list[str], java_project: JavaProject, func_class_pair: dict[str, str], threads: int | None) -> list[Calling]:
    calling_list_all: list[Calling] = []
    with concurrent.futures.ProcessPoolExecutor(max_workers=threads, mp_context=multiprocessing.get_context("fork")) as executor:
        results = [executor.submit(cpp2java_scan_process, file, repo_path, func_class_pair) for file in file_list]
        for result in tqdm(concurrent.futures.as_completed(results), desc="Scanning C++ Call Java", total=len(results)):
            calling_list = result.result()
            if calling_list is not None:
                calling_list_all.extend(calling_list)

    logging.info(f"calling list cpp2java len: {len(calling_list_all)}")
    calling_list_all = cpp2java_java_loc(calling_list_all, java_project)
    return calling_list_all

def main(repo_path: str, compdb: str, threads: int | None, overwrite: bool = False):
    logging.info(f"Repo path: {repo_path}")

    logging.info(f"Loading Java Source Code")
    if os.path.exists('java_project.pkl') and not overwrite:
        java_project: JavaProject = pickle.load(open("java_project.pkl", "rb"))
    else:
        java_project = JavaProject(repo_path)
        with open("java_project.pkl", "wb") as f:
            pickle.dump(java_project, f)

    logging.info(f"Probe JNI Source Files")
    file_list = radar.radar(repo_path, overwrite=True)

    logging.info(f"Scanning Java Call C++")
    calling_list_java2cpp = java2cpp_scan(repo_path, file_list, java_project, threads=threads)
    # calculate the number of java2cpp paris
    len = 0
    for call in calling_list_java2cpp:
        len += call.callees.__len__()
    logging.info(f"calling pairs list java2cpp len: {len}")
    os.makedirs('results', exist_ok=True)
    with open(os.path.join('results', 'java2cpp.json'), 'w') as f:
        json.dump(calling_list_java2cpp, f, ensure_ascii=False, indent=4, default=lambda obj: obj.__dict__)

    with open(os.path.join('results', 'java2cpp.json'), 'r') as f:
        calling_list_java2cpp = json.load(f)
    func_class_pair: dict[str, str] = {}
    for call in calling_list_java2cpp:
        caller: dict = call["caller"]
        callees: list[dict] = call["callees"]
        for callee in callees:
            called_func = callee["called_func"]
            key = called_func["location"]["source_file"] + ":" + called_func["func_name"]
            value = caller["func_type"]["class_name"]
            func_class_pair[key] = value

    logging.info(f"Scanning C++ Call Java")
    calling_list_cpp2java = cpp2java_scan(repo_path, file_list, java_project, func_class_pair, threads=threads)
    # calculate the number of cpp2java paris
    len = 0
    for call in calling_list_cpp2java:
        len += call.callees.__len__()
    logging.info(f"calling pairs list cpp2java len: {len}")
    os.makedirs('results', exist_ok=True)
    with open(os.path.join('results', 'cpp2java.json'), 'w') as f:
        json.dump(calling_list_cpp2java, f, ensure_ascii=False, indent=4, default=lambda obj: obj.__dict__)


def report(repo_path: str, logdir: str):
    class DebugInfo:
        def __init__(self, file: str, code: str, reports):
            self.file = file
            self.code = code
            self.reports = reports

    def gen_code_tree(logdir: str):
        with open(f"{logdir}/j2c_debug_info.json", "r") as f:
            j2c_debug_info = json.load(f)
        with open(f"{logdir}/c2j_debug_info.json", "r") as f:
            c2j_debug_info = json.load(f)
        shutil.rmtree(f'{logdir}/code', ignore_errors=True)
        os.makedirs(f"{logdir}/code", exist_ok=True)
        for info in j2c_debug_info:
            file_dir = os.path.dirname(info["file"])
            os.makedirs(f"{logdir}/code/{file_dir}", exist_ok=True)
            with open(f"{logdir}/code/{info['file']}", "w") as f:
                f.write(info["code"])
        for info in c2j_debug_info:
            file_dir = os.path.dirname(info["file"])
            os.makedirs(f"{logdir}/code/{file_dir}", exist_ok=True)
            with open(f"{logdir}/code/{info['file']}", "w") as f:
                f.write(info["code"])

    with open(f"{logdir}/log.log", "r") as f:
        lines = f.readlines()
    target_lines_j2c = []
    target_lines_c2j = []
    file_lines_pairs_j2c: dict[str, list[str]] = {}
    file_lines_pairs_c2j: dict[str, list[str]] = {}
    target_lines = target_lines_j2c
    file_lines_pairs = file_lines_pairs_j2c

    for line in lines:
        if "Scanning C++ Call Java" in line:
            target_lines = target_lines_c2j
            file_lines_pairs = file_lines_pairs_c2j
        start = 31
        end = line.rfind("]")
        if line[start:end] == "":
            continue
        file_name = line[start:end]
        info = line[start - 1:-1]
        target_lines.append(info)
        if file_name not in file_lines_pairs:
            file_lines_pairs[file_name] = []
        file_lines_pairs[file_name].append(info)

    j2c_debug_info = []
    c2j_debug_info = []
    fail_num = 0
    with open(f"{logdir}/j2c_top.log", "w") as f:
        for key in sorted(file_lines_pairs_j2c, key=lambda x: len(file_lines_pairs_j2c[x]), reverse=True):
            f.write(f"{key}: {len(file_lines_pairs_j2c[key])}\n")
            for line in file_lines_pairs_j2c[key]:
                f.write(line + "\n")
            f.write("\n")
            with open(os.path.join(repo_path, key), "r") as fr:
                code = fr.read()
            j2c_debug_info.append(DebugInfo(key, code, file_lines_pairs_j2c[key]))
            fail_num += len(file_lines_pairs_j2c[key])
    logging.info(f"j2c fail num: {fail_num}")

    fail_num = 0
    with open(f"{logdir}/c2j_top.log", "w") as f:
        for key in sorted(file_lines_pairs_c2j, key=lambda x: len(file_lines_pairs_c2j[x]), reverse=True):
            f.write(f"{key}: {len(file_lines_pairs_c2j[key])}\n")
            for line in file_lines_pairs_c2j[key]:
                f.write(line + "\n")
            f.write("\n")
            with open(os.path.join(repo_path, key), "r") as fr:
                code = fr.read()
            c2j_debug_info.append(DebugInfo(key, code, file_lines_pairs_c2j[key]))
            fail_num += len(file_lines_pairs_c2j[key])
    logging.info(f"c2j fail num: {fail_num}")

    # dump json
    with open(f"{logdir}/j2c_debug_info.json", "w") as f:
        json.dump(j2c_debug_info, f, ensure_ascii=False, indent=4, default=lambda obj: obj.__dict__)
    with open(f"{logdir}/c2j_debug_info.json", "w") as f:
        json.dump(c2j_debug_info, f, ensure_ascii=False, indent=4, default=lambda obj: obj.__dict__)
    gen_code_tree(logdir)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("-r", "--repo", dest="repo", help="path to repo", type=str,
                        default="/home/wrs/aosp")
    parser.add_argument("-c", "--compdb", dest="compdb", help="compile_commands.json path", type=str,
                        default="/home/wrs/aosp")
    parser.add_argument("-j", "--threads", dest="threads", help="threads", type=int, default=None)
    parser.add_argument("--overwrite", dest="overwrite", help="overwrite", default=False)
    parser.add_argument("--log-level", dest="loglevel", help="log level", type=int,
                        default=logging.DEBUG)
    args = parser.parse_args()
    log_dir = "log"
    if not os.path.exists(log_dir):
        os.makedirs(log_dir, exist_ok=True)
    init_logger(logging.getLogger(), args.loglevel, f"{log_dir}/log.log")
    main(args.repo, args.compdb, args.threads, args.overwrite)
    report(args.repo, log_dir)
