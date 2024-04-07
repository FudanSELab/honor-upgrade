# jni-prober

## 系统要求

- Linux
- Python 3.10 (or later)

## 安装相关依赖

```bash
pip install tqdm tree_sitter
```

## 使用

```bash
python main.py -r <源代码目录> -c <compile_commands.json 路径>
```

## 结果说明

结果位于 `results` 目录下:

- cpp2java.json: C/C++ 调用 Java
- java2cpp.json: Java 调用 C/C++