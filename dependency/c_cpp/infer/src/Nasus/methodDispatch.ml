exception TODO

type kind = CHA | RTA | VTA | K_CFA

(* TODO: 
   1. replace of tenv with global_tenv *)
let resolve_method_bycha tenv procname receiver =
  let subtypes = Subtyping.subtypes_of receiver in
  Format.fprintf Format.std_formatter "\n%a has subtypes: %d" Typ.Name.pp receiver (List.length subtypes) ;
  List.filter_map subtypes ~f:(fun typ_name ->
      match Tenv.lookup tenv typ_name with
      | Some tstruct ->
          List.find tstruct.methods ~f:(Procname.is_override procname)
      | None ->
          None )


let resolve_method_bykcfa _tenv _procname _receiver = raise TODO

let resolve_method kind tenv procname receiver =
  match kind with
  | CHA ->
      resolve_method_bycha tenv procname receiver
  | K_CFA ->
      resolve_method_bykcfa tenv procname receiver
  | _ ->
      raise TODO
