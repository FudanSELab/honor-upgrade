type kind = CHA | RTA | VTA | K_CFA

val resolve_method : kind -> Tenv.t -> Procname.t -> Typ.name -> Procname.t list
