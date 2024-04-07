open! IStd

type refering = Type of Typ.t | Func of Procname.t [@@deriving compare, sexp, equal, hash]

type reference = {refering: refering; refered: Typ.t; loc: Location.t}
[@@deriving compare, sexp, equal, hash]

type t = reference list

let rec type_filter (t : Typ.t) : bool =
  match t.desc with
  | Tint _ | Tfloat _ | Tvoid | Tfun | TVar _ ->
      false
  | Tptr (ptr, _) ->
      type_filter ptr
  | Tarray {elt} ->
      type_filter elt
  | Tstruct _ ->
      true


let get_refs_for_function (pdesc : Procdesc.t) : t =
  (* get all references in sig of function *)
  let fun_name = Procdesc.get_proc_name pdesc in
  let fun_loc = Procdesc.get_loc pdesc in
  let formals = Procdesc.get_formals pdesc in
  let get_formal_refs acc formal =
    let _, typ, _ = formal in
    if type_filter typ then {refering= Func fun_name; refered= typ; loc= fun_loc} :: acc else acc
  in
  let refs_in_formals = List.fold formals ~init:[] ~f:get_formal_refs in
  let ret_typ = Procdesc.get_ret_type pdesc in
  let refs_in_sig =
    if type_filter ret_typ then
      {refering= Func fun_name; refered= ret_typ; loc= fun_loc} :: refs_in_formals
    else refs_in_formals
  in
  (* get all references in body of function *)
  let is_lvar = function Exp.Lvar _ -> true | _ -> false in
  let get_local_refs acc _ instr : t =
    match instr with
    | Sil.Store {e1; typ; loc} when is_lvar e1 && type_filter typ ->
        {refering= Func fun_name; refered= typ; loc} :: acc
    | _ ->
        acc
  in
  Procdesc.fold_instrs pdesc ~init:refs_in_sig ~f:get_local_refs


let get_refs_for_functions (pdescs : Procdesc.t list) : t =
  List.fold pdescs ~init:[] ~f:(fun acc pdesc -> acc @ get_refs_for_function pdesc)


let solve pdescs : t =
  get_refs_for_functions pdescs


let ref_to_json (ref : reference) : ATDGenerated.Dependency_t.reference =
  let language = Language.to_json !Language.curr_language in
  let refering_obj : ATDGenerated.Dependency_t.refering_obj =
    match ref.refering with
    | Type typ ->
        Type (Typ.to_json typ)
    | Func func ->
        Func (func |> Procdesc.load |> Option.value_exn |> Procdesc.to_json)
  in
  let refered_type : ATDGenerated.Dependency_t.type_name = Typ.to_json ref.refered in
  let location = Location.to_json ref.loc in
  {language; refering_obj; refered_type; location}


let to_json (refs : t) : ATDGenerated.Dependency_t.reference list =
  let length = List.length refs in
  print_endline ("reference's length is " ^ string_of_int length) ;
  List.rev_map refs ~f:ref_to_json

(* let _ = List.rev_map *)
