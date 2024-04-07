open! IStd

type t = (Typ.t * Typ.t list * Location.t) list

let get_inhs tname (strukt : Struct.t) =
  let typ_t = Typ.mk_struct tname in
  let supers =
    let open List in
    strukt.supers >>| Typ.mk_struct
  in
  (* TODO: dummy location *)
  let loc = match strukt.source_file with Some sl -> Location.none sl | None -> Location.dummy in
  (typ_t, supers, loc)


let solve tenv : t =
  let inhs = ref [] in
  let save_inhs tname strukt =
    let old_inhs = !inhs in
    inhs := get_inhs tname strukt :: old_inhs
  in
  Tenv.iter save_inhs tenv ;
  !inhs


let to_json = assert false