module TypenameHash = Caml.Hashtbl.Make (Typ.Name)

type t = Typ.name list TypenameHash.t

(* TODO: keep consistence with incremental analysis *)
let subtyping_relations : t option ref = ref None

let revert_super _ =
  let new_rel = TypenameHash.create 1000 in
  let update_rel (name : Typ.name) (tstruct : Struct.t) =
    List.iter tstruct.supers ~f:(fun super ->
        let origins = match TypenameHash.find_opt new_rel super with Some l -> l | None -> [] in
        TypenameHash.replace new_rel super (name :: origins) )
  in
  ( match Tenv.load_global () with
  | Some tenv ->
      (* Tenv.load_global only for java *)
      Tenv.iter update_rel tenv
  | None ->
      (* TODO: replace the follows with API (Tenv.merge_per_file) *)
      (* Merge all tenv for CXX *)
      let all_sources = SourceFiles.get_all ~filter:(fun _ -> true) () in
      let all_tenv = List.map all_sources ~f:(fun source -> Tenv.load source) in
      let global_tenv =
        List.fold all_tenv ~init:(Tenv.create ()) ~f:(fun acc tenv_opt ->
            match tenv_opt with
            | Some tenv ->
                Tenv.merge ~src:tenv ~dst:acc ;
                acc
            | None ->
                acc )
      in
      Tenv.iter update_rel global_tenv ) ;
  new_rel


let pp fmt =
  match !subtyping_relations with
  | Some rel ->
      TypenameHash.iter
        (fun typ subty ->
          Format.fprintf fmt "@[<v>%a has directed subtypes:@.%a@.@]" Typ.Name.pp typ
            (Format.pp_print_list (fun fmt n -> Format.fprintf fmt "  %a;" Typ.Name.pp n))
            subty )
        rel
  | None ->
      ()


let subtypes_of typ =
  let is_visited = TypenameHash.create 1000 in
  let rec dfs rel ty =
    let visited =
      match TypenameHash.find_opt is_visited ty with Some is_vis -> is_vis | None -> false
    in
    if visited then []
    else
      match TypenameHash.find_opt rel ty with
      | Some subs ->
          List.fold subs ~init:[ty] ~f:(fun acc sub -> dfs rel sub @ acc)
      | None ->
          Format.fprintf Format.std_formatter "there is no subtypes of %a" Typ.Name.pp ty ;
          [ty]
  in
  match !subtyping_relations with
  | Some rel ->
      dfs rel typ
  | None ->
      let new_rel = revert_super () in
      subtyping_relations := Some new_rel ;
      dfs new_rel typ


let ( <: ) t1 t2 = List.exists (subtypes_of t2) ~f:(Typ.Name.equal t1)
