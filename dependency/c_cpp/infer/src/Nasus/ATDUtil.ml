exception TranslateError of string

exception UnhandledCond

exception MissingSummary

let rec typ_to_string (typ : IR.Typ.t) : string =
  match typ.desc with
  | Tint ikind -> (
    match ikind with
    | IChar ->
        "char"
    | ISChar ->
        "signed_char"
    | IUChar ->
        "unsigned_char"
    | IBool ->
        "bool"
    | IInt ->
        "int"
    | IUInt ->
        "unsigned_int"
    | IShort ->
        "short"
    | IUShort ->
        "unsigned_short"
    | ILong ->
        "long"
    | IULong ->
        "unsigned_long"
    | ILongLong ->
        "long_long"
    | IULongLong ->
        "unsigned_long_long"
    | I128 ->
        "int128"
    | IU128 ->
        "unsigned_int128" )
  | Tfloat fkind -> (
    match fkind with FFloat -> "float" | FDouble -> "double" | FLongDouble -> "long_double" )
  | Tvoid ->
      "void"
  | Tfun ->
      "<fun>"
  | Tptr (typ, _) ->
      typ_to_string typ ^ "*"
  | Tstruct (CStruct name) | Tstruct (CUnion name) | Tstruct (CppClass {name}) ->
      QualifiedCppName.to_qual_string name
  | Tstruct _ ->
      raise (TranslateError "only support C/C++")
  | TVar _ ->
      raise UnhandledCond
  | Tarray _ ->
      typ_to_string typ ^ "[]"


let sourcefile_to_string (file : SourceFile.t) : string = SourceFile.to_abs_path file

let location_to_location (loc : Location.t) : ATDGenerated.Dependency_t.location =
  {source_file= sourcefile_to_string loc.file; line= loc.line; column= loc.col}


let procdesc_to_location (procdesc : Procdesc.t) : ATDGenerated.Dependency_t.location =
  location_to_location (Procdesc.get_attributes procdesc).loc


(* translate [procdesc] to [func_name] *)
let procdesc_to_funcname (procdesc : Procdesc.t) : string =
  let attributes = Procdesc.get_attributes procdesc in
  match attributes.proc_name with
  | C {name} -> (
    match IR__QualifiedCppName.to_list name with
    | [] ->
        raise (TranslateError "does not have function name")
    | funcname :: _ ->
        funcname )
  | ObjC_Cpp {method_name} ->
      method_name
  | _ ->
      raise UnhandledCond


(* translate [procdesc] to [func_type] *)
let procdesc_to_functype (procdesc : Procdesc.t) : ATDGenerated.Dependency_t.func_type =
  let attributes = Procdesc.get_attributes procdesc in
  let args_type = List.map attributes.formals ~f:(fun (_, typ, _) -> typ_to_string typ) in
  let return_type = typ_to_string attributes.ret_type in
  let class_name =
    match attributes.proc_name with
    | C {name} -> (
      match IR__QualifiedCppName.to_list name with
      | [] ->
          raise (TranslateError "does not have function name")
      | [_] ->
          None
      | _ :: classname ->
          Some (String.concat ~sep:"::" (List.rev classname)) )
    | ObjC_Cpp {class_name} ->
        Some (Typ.Name.to_string class_name)
    | _ ->
        raise UnhandledCond
  in
  {args_type; return_type; class_name}


let procdesc_to_func (procdesc : Procdesc.t) : ATDGenerated.Dependency_t.func =
  let attri = Procdesc.get_attributes procdesc in
  let _is_abs = attri.is_abstract in
  let _kind = attri.clang_method_kind in
  if _is_abs then print_endline "_is_abs" else print_endline "not_spec" ;
  ( match _kind with
  | CPP_INSTANCE ->
      print_endline "CPP_INSTANCE"
  | OBJC_INSTANCE ->
      print_endline "OBJC_INSTANCE"
  | CPP_CLASS ->
      print_endline "CPP_CLASS"
  | OBJC_CLASS ->
      print_endline "OBJC_CLASS"
  | BLOCK ->
      print_endline "BLOCK"
  | C_FUNCTION ->
      print_endline "C_FUNCTION" ) ;
  { func_name= procdesc_to_funcname procdesc
  ; func_type= procdesc_to_functype procdesc
  ; location= procdesc_to_location procdesc }


let procdesc_to_language (procdesc : Procdesc.t) : ATDGenerated.Dependency_t.language =
  let attri = Procdesc.get_attributes procdesc in
  match attri.proc_name with C _ | ObjC_Cpp _ -> C_CXX | Java _ -> Java | _ -> raise UnhandledCond

(* 
let sum_to_calling (sum : summary) : ATDGenerated.Dependency_t.calling =
  let caller = procdesc_to_func sum.caller in
  let callees =
    let callee_list = CalleeSet.elements sum.callees in
    List.map callee_list ~f:(fun (procdesc, call_site) : ATDGenerated.Dependency_t.callee ->
        {called_func= procdesc_to_func procdesc; call_site= location_to_location call_site} )
  in
  let language = procdesc_to_language sum.caller in
  {language; caller; callees} *)
